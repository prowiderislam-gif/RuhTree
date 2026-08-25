package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.cloud.AccountManager
import com.example.data.cloud.CloudVaultService
import com.example.data.cloud.SyncStatus
import com.example.data.cloud.UserAccount
import com.example.data.local.AppDatabase
import com.example.data.model.FamilyMember
import com.example.data.model.Gender
import com.example.data.model.SpouseRelation
import com.example.data.repository.FamilyRepository
import com.example.data.security.SecurityManager
import com.example.util.DateUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FamilyTreeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FamilyRepository
    val securityManager: SecurityManager = SecurityManager(application)
    val accountManager: AccountManager = AccountManager(application)
    val cloudVaultService: CloudVaultService = CloudVaultService(application)

    val currentAccount = MutableStateFlow<UserAccount?>(accountManager.getCurrentAccount())
    val syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncMessage = MutableStateFlow("")
    val lastSyncTimestamp = MutableStateFlow(accountManager.getCurrentAccount()?.lastSyncTime ?: 0L)
    val isCloudDialogOpen = MutableStateFlow(false)
    val isOnboardingDialogOpen = MutableStateFlow(!accountManager.isFirstTimeSetupCompleted() && accountManager.getCurrentAccount() == null)

    private var autoSyncJob: Job? = null

    fun dismissOnboarding() {
        accountManager.setFirstTimeSetupCompleted(true)
        isOnboardingDialogOpen.value = false
    }

    init {
        val db = AppDatabase.getDatabase(application)
        repository = FamilyRepository(db.familyDao())
        checkAndSeedInitialData()
    }

    val members: StateFlow<List<FamilyMember>> = repository.allMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val spouses: StateFlow<List<SpouseRelation>> = repository.allSpouseRelations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchQuery = MutableStateFlow("")

    val filteredMembers: StateFlow<List<FamilyMember>> = combine(members, searchQuery) { list, query ->
        if (query.isBlank()) list
        else {
            val q = query.trim().lowercase()
            list.filter { member ->
                val nameMatch = member.name.lowercase().contains(q)
                val descMatch = member.description?.lowercase()?.contains(q) == true
                val dobRaw = member.dateOfBirth?.lowercase() ?: ""
                val dobDisplay = DateUtils.formatDisplayDate(member.dateOfBirth).lowercase()
                val dobMatch = dobRaw.contains(q) || dobDisplay.contains(q)
                val ageStr = DateUtils.formatAgeString(member.dateOfBirth, member.isDeceased, member.dateOfDeath).lowercase()
                val ageMatch = ageStr.contains(q) || (q.toIntOrNull()?.let { targetAge -> ageStr.contains("$targetAge") } == true)
                nameMatch || descMatch || dobMatch || ageMatch
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dialog & UI Navigation States
    val selectedMemberForDetail = MutableStateFlow<FamilyMember?>(null)
    val focusMemberId = MutableStateFlow<Long?>(null)
    val isFormDialogOpen = MutableStateFlow(false)
    val editingMember = MutableStateFlow<FamilyMember?>(null)
    val suggestedFatherId = MutableStateFlow<Long?>(null)
    val suggestedMotherId = MutableStateFlow<Long?>(null)

    fun setFocusMember(memberId: Long?) {
        focusMemberId.value = memberId
    }

    fun clearFocusMember() {
        focusMemberId.value = null
    }

    val isCompareDialogOpen = MutableStateFlow(false)
    val compareMemberAId = MutableStateFlow<Long?>(null)
    val compareMemberBId = MutableStateFlow<Long?>(null)

    val isPinAuthDialogOpen = MutableStateFlow(false)
    val pinAuthTitle = MutableStateFlow("")
    private var pendingAuthorizedAction: (() -> Unit)? = null

    val isSettingsDialogOpen = MutableStateFlow(false)

    fun openAddMember(fatherId: Long? = null, motherId: Long? = null) {
        editingMember.value = null
        suggestedFatherId.value = fatherId
        suggestedMotherId.value = motherId
        isFormDialogOpen.value = true
    }

    fun requestEditMember(member: FamilyMember) {
        if (securityManager.isPinEnabled()) {
            pinAuthTitle.value = "Edit '${member.name}'"
            pendingAuthorizedAction = {
                editingMember.value = member
                suggestedFatherId.value = member.fatherId
                suggestedMotherId.value = member.motherId
                selectedMemberForDetail.value = null
                isFormDialogOpen.value = true
            }
            isPinAuthDialogOpen.value = true
        } else {
            editingMember.value = member
            suggestedFatherId.value = member.fatherId
            suggestedMotherId.value = member.motherId
            selectedMemberForDetail.value = null
            isFormDialogOpen.value = true
        }
    }

    fun requestDeleteMember(member: FamilyMember) {
        if (securityManager.isPinEnabled()) {
            pinAuthTitle.value = "Delete '${member.name}'"
            pendingAuthorizedAction = {
                viewModelScope.launch {
                    repository.deleteMember(member)
                    selectedMemberForDetail.value = null
                    scheduleRealtimeCloudSync()
                }
            }
            isPinAuthDialogOpen.value = true
        } else {
            viewModelScope.launch {
                repository.deleteMember(member)
                selectedMemberForDetail.value = null
                scheduleRealtimeCloudSync()
            }
        }
    }

    fun onPinSuccess() {
        isPinAuthDialogOpen.value = false
        pendingAuthorizedAction?.invoke()
        pendingAuthorizedAction = null
    }

    fun saveMember(member: FamilyMember, spouseIds: List<Long> = emptyList()) {
        viewModelScope.launch {
            if (member.id == 0L) {
                val newId = repository.insertMember(member)
                spouseIds.forEach { spId ->
                    repository.insertSpouseRelation(SpouseRelation(memberId1 = newId, memberId2 = spId))
                }
            } else {
                repository.updateMember(member)
                spouseIds.forEach { spId ->
                    repository.insertSpouseRelation(SpouseRelation(memberId1 = member.id, memberId2 = spId))
                }
            }
            isFormDialogOpen.value = false
            editingMember.value = null
            scheduleRealtimeCloudSync()
        }
    }

    fun addSpouse(member: FamilyMember, spouseId: Long, isDivorced: Boolean = false) {
        viewModelScope.launch {
            repository.insertSpouseRelation(
                SpouseRelation(
                    memberId1 = member.id,
                    memberId2 = spouseId,
                    isDivorced = isDivorced
                )
            )
            scheduleRealtimeCloudSync()
        }
    }

    fun openCompare(memberAId: Long? = null, memberBId: Long? = null) {
        compareMemberAId.value = memberAId ?: members.value.firstOrNull()?.id
        compareMemberBId.value = memberBId ?: members.value.getOrNull(1)?.id
        isCompareDialogOpen.value = true
    }

    fun clearAllTree() {
        if (securityManager.isPinEnabled()) {
            pinAuthTitle.value = "Clear Full Tree"
            pendingAuthorizedAction = {
                viewModelScope.launch {
                    repository.clearAll()
                    selectedMemberForDetail.value = null
                    scheduleRealtimeCloudSync()
                }
            }
            isPinAuthDialogOpen.value = true
        } else {
            viewModelScope.launch {
                repository.clearAll()
                selectedMemberForDetail.value = null
                scheduleRealtimeCloudSync()
            }
        }
    }

    private fun checkAndSeedInitialData() {
        viewModelScope.launch {
            val current = repository.getAllMembersDirect()
            if (current.isEmpty()) {
                seedSampleFamilyTreeInternal()
            }
        }
    }

    fun seedSampleFamilyTree() {
        viewModelScope.launch {
            repository.clearAll()
            seedSampleFamilyTreeInternal()
            scheduleRealtimeCloudSync()
        }
    }

    private suspend fun seedSampleFamilyTreeInternal() {
        // Generation 0: Patriarch & Matriarch (Adam & Eve)
        val adamId = repository.insertMember(
            FamilyMember(
                name = "Adam",
                gender = Gender.MALE.name,
                dateOfBirth = "1960-01-01",
                timeOfBirth = "07:30",
                isDeceased = false,
                description = "Family patriarch."
            )
        )

        val eveId = repository.insertMember(
            FamilyMember(
                name = "Eve",
                gender = Gender.FEMALE.name,
                dateOfBirth = "1963-05-15",
                timeOfBirth = "12:00",
                isDeceased = false,
                description = "Family matriarch."
            )
        )

        repository.insertSpouseRelation(
            SpouseRelation(memberId1 = adamId, memberId2 = eveId)
        )

        // Generation 1: Children of Adam & Eve (Ash, Rock, Lili)
        val ashId = repository.insertMember(
            FamilyMember(
                name = "Ash",
                gender = Gender.MALE.name,
                dateOfBirth = "1985-03-10",
                timeOfBirth = "09:20",
                fatherId = adamId,
                motherId = eveId,
                description = "Eldest son of Adam & Eve."
            )
        )

        val mayaId = repository.insertMember(
            FamilyMember(
                name = "Maya",
                gender = Gender.FEMALE.name,
                dateOfBirth = "1987-08-20",
                timeOfBirth = "15:45",
                description = "Spouse of Ash."
            )
        )

        repository.insertSpouseRelation(
            SpouseRelation(memberId1 = ashId, memberId2 = mayaId)
        )

        val rockId = repository.insertMember(
            FamilyMember(
                name = "Rock",
                gender = Gender.MALE.name,
                dateOfBirth = "1989-11-12",
                timeOfBirth = "18:10",
                fatherId = adamId,
                motherId = eveId,
                description = "Second son of Adam & Eve."
            )
        )

        val liliId = repository.insertMember(
            FamilyMember(
                name = "Lili",
                gender = Gender.FEMALE.name,
                dateOfBirth = "1993-04-05",
                timeOfBirth = "11:30",
                fatherId = adamId,
                motherId = eveId,
                description = "Daughter of Adam & Eve."
            )
        )

        // Generation 2: Children of Ash & Maya (Lee, Nini, Ron)
        val leeId = repository.insertMember(
            FamilyMember(
                name = "Lee",
                gender = Gender.MALE.name,
                dateOfBirth = "2012-06-18",
                timeOfBirth = "08:15",
                fatherId = ashId,
                motherId = mayaId,
                description = "First child of Ash & Maya."
            )
        )

        val niniId = repository.insertMember(
            FamilyMember(
                name = "Nini",
                gender = Gender.FEMALE.name,
                dateOfBirth = "2015-09-22",
                timeOfBirth = "14:00",
                fatherId = ashId,
                motherId = mayaId,
                description = "Daughter of Ash & Maya."
            )
        )

        val ronId = repository.insertMember(
            FamilyMember(
                name = "Ron",
                gender = Gender.MALE.name,
                dateOfBirth = "2018-12-04",
                timeOfBirth = "19:50",
                fatherId = ashId,
                motherId = mayaId,
                description = "Youngest son of Ash & Maya."
            )
        )
    }

    // ==========================================
    // CLOUD VAULT & REAL-TIME BACKUP OPERATIONS
    // ==========================================

    private fun scheduleRealtimeCloudSync() {
        val account = currentAccount.value ?: return
        autoSyncJob?.cancel()
        autoSyncJob = viewModelScope.launch {
            delay(800) // Debounce rapid edits
            performCloudSyncInternal(account, isManual = false)
        }
    }

    fun syncToCloudNow() {
        val account = currentAccount.value ?: return
        viewModelScope.launch {
            performCloudSyncInternal(account, isManual = true)
        }
    }

    private suspend fun performCloudSyncInternal(account: UserAccount, isManual: Boolean) {
        syncStatus.value = SyncStatus.SYNCING
        syncMessage.value = "Encrypting & uploading to Cloud Vault..."

        val currentMembers = repository.getAllMembersDirect()
        val currentSpouses = repository.getAllSpouseRelationsDirect()

        val result = cloudVaultService.uploadVault(
            userId = account.userId,
            passwordHash = account.passwordHash,
            vaultTitle = account.displayName.ifBlank { "Ruh Tree Vault" },
            members = currentMembers,
            spouses = currentSpouses,
            securityPin = securityManager.getPin(),
            pinEnabled = securityManager.isPinEnabled()
        )

        if (result.success) {
            syncStatus.value = SyncStatus.SUCCESS
            syncMessage.value = "Synced to Cloud Vault just now"
            lastSyncTimestamp.value = result.timestamp
            accountManager.updateLastSyncTime(result.timestamp)
            currentAccount.value = accountManager.getCurrentAccount()
        } else {
            syncStatus.value = SyncStatus.ERROR
            syncMessage.value = result.message
        }
    }

    fun createVaultAccount(userId: String, rawPassword: String, vaultName: String) {
        viewModelScope.launch {
            val result = accountManager.registerAccount(userId, rawPassword, vaultName)
            result.onSuccess { account ->
                currentAccount.value = account
                accountManager.setFirstTimeSetupCompleted(true)
                isOnboardingDialogOpen.value = false
                isCloudDialogOpen.value = false
                // Immediately backup current local tree to the newly initialized cloud vault
                performCloudSyncInternal(account, isManual = true)
            }.onFailure { error ->
                syncStatus.value = SyncStatus.ERROR
                syncMessage.value = error.localizedMessage ?: "Failed to create account"
            }
        }
    }

    fun signInAndRestoreVault(userId: String, rawPassword: String) {
        viewModelScope.launch {
            syncStatus.value = SyncStatus.SYNCING
            syncMessage.value = "Authenticating with Cloud Vault..."

            val computedHash = accountManager.hashPassword(rawPassword, userId.trim().lowercase())
            val restoreResult = cloudVaultService.downloadVault(userId, computedHash)

            if (restoreResult.success && restoreResult.payload != null) {
                val payload = restoreResult.payload
                // Activate account session
                val signInResult = accountManager.signInAccount(userId, rawPassword)
                signInResult.onSuccess { acc ->
                    currentAccount.value = acc
                    accountManager.setFirstTimeSetupCompleted(true)
                    isOnboardingDialogOpen.value = false
                    isCloudDialogOpen.value = false
                }

                // Restore Room DB with cloud payload
                repository.clearAll()
                payload.members.forEach { member ->
                    repository.insertMember(member)
                }
                payload.spouses.forEach { spouse ->
                    repository.insertSpouseRelation(spouse)
                }

                // Restore PIN settings if present
                if (payload.securityPin.isNotBlank()) {
                    securityManager.setPin(payload.securityPin)
                    securityManager.setPinEnabled(payload.pinEnabled)
                }

                syncStatus.value = SyncStatus.SUCCESS
                syncMessage.value = "Restored ${payload.members.size} members from cloud!"
                lastSyncTimestamp.value = payload.backupTimestamp
                accountManager.updateLastSyncTime(payload.backupTimestamp)
                selectedMemberForDetail.value = null
            } else {
                // If it fails on cloud download (e.g. invalid credentials or not found)
                syncStatus.value = SyncStatus.ERROR
                syncMessage.value = restoreResult.message
            }
        }
    }

    fun downloadAndRestoreFromActiveCloud() {
        val account = currentAccount.value ?: return
        viewModelScope.launch {
            syncStatus.value = SyncStatus.SYNCING
            syncMessage.value = "Downloading from Cloud Vault..."

            val restoreResult = cloudVaultService.downloadVault(account.userId, account.passwordHash)
            if (restoreResult.success && restoreResult.payload != null) {
                val payload = restoreResult.payload
                repository.clearAll()
                payload.members.forEach { member ->
                    repository.insertMember(member)
                }
                payload.spouses.forEach { spouse ->
                    repository.insertSpouseRelation(spouse)
                }
                if (payload.securityPin.isNotBlank()) {
                    securityManager.setPin(payload.securityPin)
                    securityManager.setPinEnabled(payload.pinEnabled)
                }

                syncStatus.value = SyncStatus.SUCCESS
                syncMessage.value = "Successfully refreshed ${payload.members.size} members from Cloud Vault"
                lastSyncTimestamp.value = payload.backupTimestamp
                accountManager.updateLastSyncTime(payload.backupTimestamp)
                selectedMemberForDetail.value = null
            } else {
                syncStatus.value = SyncStatus.ERROR
                syncMessage.value = restoreResult.message
            }
        }
    }

    fun logoutAccount() {
        accountManager.logout()
        currentAccount.value = null
        syncStatus.value = SyncStatus.IDLE
        syncMessage.value = ""
    }
}

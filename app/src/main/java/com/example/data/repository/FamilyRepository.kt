package com.example.data.repository

import com.example.data.local.FamilyDao
import com.example.data.model.FamilyMember
import com.example.data.model.SpouseRelation
import kotlinx.coroutines.flow.Flow

class FamilyRepository(private val familyDao: FamilyDao) {

    val allMembers: Flow<List<FamilyMember>> = familyDao.getAllMembers()
    val allSpouseRelations: Flow<List<SpouseRelation>> = familyDao.getAllSpouseRelations()

    fun getMemberById(id: Long): Flow<FamilyMember?> = familyDao.getMemberById(id)

    suspend fun getMemberByIdDirect(id: Long): FamilyMember? = familyDao.getMemberByIdDirect(id)

    suspend fun getAllMembersDirect(): List<FamilyMember> = familyDao.getAllMembersDirect()

    suspend fun getAllSpouseRelationsDirect(): List<SpouseRelation> = familyDao.getAllSpouseRelationsDirect()

    suspend fun insertMember(member: FamilyMember): Long = familyDao.insertMember(member)

    suspend fun updateMember(member: FamilyMember) = familyDao.updateMember(member)

    suspend fun deleteMember(member: FamilyMember) {
        familyDao.deleteSpouseRelationsForMember(member.id)
        familyDao.deleteMember(member)
    }

    suspend fun deleteMemberById(id: Long) {
        familyDao.deleteSpouseRelationsForMember(id)
        familyDao.deleteMemberById(id)
    }

    suspend fun insertSpouseRelation(relation: SpouseRelation): Long = familyDao.insertSpouseRelation(relation)

    suspend fun deleteSpouseRelation(relation: SpouseRelation) = familyDao.deleteSpouseRelation(relation)

    suspend fun clearAll() {
        familyDao.clearAllSpouses()
        familyDao.clearAllMembers()
    }
}

package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FamilyMember
import com.example.data.model.SpouseRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyDao {
    @Query("SELECT * FROM family_members ORDER BY generationLevel ASC, createdAt ASC")
    fun getAllMembers(): Flow<List<FamilyMember>>

    @Query("SELECT * FROM family_members WHERE id = :id")
    fun getMemberById(id: Long): Flow<FamilyMember?>

    @Query("SELECT * FROM family_members WHERE id = :id")
    suspend fun getMemberByIdDirect(id: Long): FamilyMember?

    @Query("SELECT * FROM family_members")
    suspend fun getAllMembersDirect(): List<FamilyMember>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: FamilyMember): Long

    @Update
    suspend fun updateMember(member: FamilyMember)

    @Delete
    suspend fun deleteMember(member: FamilyMember)

    @Query("DELETE FROM family_members WHERE id = :id")
    suspend fun deleteMemberById(id: Long)

    @Query("SELECT * FROM spouse_relations")
    fun getAllSpouseRelations(): Flow<List<SpouseRelation>>

    @Query("SELECT * FROM spouse_relations")
    suspend fun getAllSpouseRelationsDirect(): List<SpouseRelation>

    @Query("SELECT * FROM spouse_relations WHERE memberId1 = :memberId OR memberId2 = :memberId")
    suspend fun getSpousesForMember(memberId: Long): List<SpouseRelation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpouseRelation(relation: SpouseRelation): Long

    @Delete
    suspend fun deleteSpouseRelation(relation: SpouseRelation)

    @Query("DELETE FROM spouse_relations WHERE memberId1 = :memberId OR memberId2 = :memberId")
    suspend fun deleteSpouseRelationsForMember(memberId: Long)

    @Query("DELETE FROM family_members")
    suspend fun clearAllMembers()

    @Query("DELETE FROM spouse_relations")
    suspend fun clearAllSpouses()
}

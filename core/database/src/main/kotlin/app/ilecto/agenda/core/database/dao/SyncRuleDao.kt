package app.ilecto.agenda.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.ilecto.agenda.core.database.entity.SyncRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncRuleDao {
    @Query("SELECT * FROM sync_rules")
    fun observeAll(): Flow<List<SyncRuleEntity>>

    @Query("SELECT * FROM sync_rules WHERE enabled = 1")
    suspend fun getEnabled(): List<SyncRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rule: SyncRuleEntity)

    @Delete
    suspend fun delete(rule: SyncRuleEntity)

    @Query("DELETE FROM sync_rules WHERE id = :ruleId")
    suspend fun deleteById(ruleId: String)
}

package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ActivationCode
import com.example.data.model.VpnServer
import kotlinx.coroutines.flow.Flow

@Dao
interface VpnDao {
    @Query("SELECT * FROM activation_codes ORDER BY createdDate DESC")
    fun getAllCodes(): Flow<List<ActivationCode>>

    @Query("SELECT * FROM activation_codes WHERE code = :code LIMIT 1")
    suspend fun getCodeByValue(code: String): ActivationCode?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCode(code: ActivationCode)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCodes(codes: List<ActivationCode>)

    @Update
    suspend fun updateCode(code: ActivationCode)

    @Query("DELETE FROM activation_codes WHERE code = :code")
    suspend fun deleteCode(code: String)

    @Query("SELECT * FROM vpn_servers WHERE isVisible = 1 ORDER BY pingMs ASC")
    fun getVisibleServers(): Flow<List<VpnServer>>

    @Query("SELECT * FROM vpn_servers ORDER BY name ASC")
    fun getAllServers(): Flow<List<VpnServer>>

    @Query("SELECT * FROM vpn_servers WHERE id = :id LIMIT 1")
    suspend fun getServerById(id: String): VpnServer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: VpnServer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServers(servers: List<VpnServer>)

    @Update
    suspend fun updateServer(server: VpnServer)

    @Query("DELETE FROM vpn_servers WHERE id = :id")
    suspend fun deleteServer(id: String)
}

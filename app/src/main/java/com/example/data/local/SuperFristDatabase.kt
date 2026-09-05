package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.ActivationCode
import com.example.data.model.CodeStatus
import com.example.data.model.ServerStatus
import com.example.data.model.VpnProtocol
import com.example.data.model.VpnServer

class Converters {
    @TypeConverter
    fun fromCodeStatus(status: CodeStatus): String = status.name

    @TypeConverter
    fun toCodeStatus(value: String): CodeStatus = try {
        CodeStatus.valueOf(value)
    } catch (e: Exception) {
        CodeStatus.ACTIVE
    }

    @TypeConverter
    fun fromVpnProtocol(protocol: VpnProtocol): String = protocol.name

    @TypeConverter
    fun toVpnProtocol(value: String): VpnProtocol = try {
        VpnProtocol.valueOf(value)
    } catch (e: Exception) {
        VpnProtocol.WIREGUARD
    }

    @TypeConverter
    fun fromServerStatus(status: ServerStatus): String = status.name

    @TypeConverter
    fun toServerStatus(value: String): ServerStatus = try {
        ServerStatus.valueOf(value)
    } catch (e: Exception) {
        ServerStatus.ONLINE
    }
}

@Database(entities = [ActivationCode::class, VpnServer::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class SuperFristDatabase : RoomDatabase() {
    abstract fun vpnDao(): VpnDao

    companion object {
        @Volatile
        private var INSTANCE: SuperFristDatabase? = null

        fun getInstance(context: Context): SuperFristDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SuperFristDatabase::class.java,
                    "super_frist_vpn.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

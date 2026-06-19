package com.tide.app.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.IllegalArgumentException
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ShieldDao_Impl(
  __db: RoomDatabase,
) : ShieldDao {
  private val __db: RoomDatabase

  private val __deleteAdapterOfShieldEntity: EntityDeleteOrUpdateAdapter<ShieldEntity>

  private val __upsertAdapterOfShieldEntity: EntityUpsertAdapter<ShieldEntity>
  init {
    this.__db = __db
    this.__deleteAdapterOfShieldEntity = object : EntityDeleteOrUpdateAdapter<ShieldEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `shields` WHERE `packageName` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ShieldEntity) {
        statement.bindText(1, entity.packageName)
      }
    }
    this.__upsertAdapterOfShieldEntity = EntityUpsertAdapter<ShieldEntity>(object : EntityInsertAdapter<ShieldEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `shields` (`packageName`,`appName`,`mode`,`dailyLimitMinutes`,`enabled`,`graceDate`,`graceUsedToday`,`graceExtensionUntil`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ShieldEntity) {
        statement.bindText(1, entity.packageName)
        statement.bindText(2, entity.appName)
        statement.bindText(3, __ShieldMode_enumToString(entity.mode))
        statement.bindLong(4, entity.dailyLimitMinutes.toLong())
        val _tmp: Int = if (entity.enabled) 1 else 0
        statement.bindLong(5, _tmp.toLong())
        statement.bindText(6, entity.graceDate)
        statement.bindLong(7, entity.graceUsedToday.toLong())
        statement.bindLong(8, entity.graceExtensionUntil)
        statement.bindLong(9, entity.createdAt)
      }
    }, object : EntityDeleteOrUpdateAdapter<ShieldEntity>() {
      protected override fun createQuery(): String = "UPDATE `shields` SET `packageName` = ?,`appName` = ?,`mode` = ?,`dailyLimitMinutes` = ?,`enabled` = ?,`graceDate` = ?,`graceUsedToday` = ?,`graceExtensionUntil` = ?,`createdAt` = ? WHERE `packageName` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ShieldEntity) {
        statement.bindText(1, entity.packageName)
        statement.bindText(2, entity.appName)
        statement.bindText(3, __ShieldMode_enumToString(entity.mode))
        statement.bindLong(4, entity.dailyLimitMinutes.toLong())
        val _tmp: Int = if (entity.enabled) 1 else 0
        statement.bindLong(5, _tmp.toLong())
        statement.bindText(6, entity.graceDate)
        statement.bindLong(7, entity.graceUsedToday.toLong())
        statement.bindLong(8, entity.graceExtensionUntil)
        statement.bindLong(9, entity.createdAt)
        statement.bindText(10, entity.packageName)
      }
    })
  }

  public override suspend fun delete(shield: ShieldEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfShieldEntity.handle(_connection, shield)
  }

  public override suspend fun upsert(shield: ShieldEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfShieldEntity.upsert(_connection, shield)
  }

  public override fun observeAll(): Flow<List<ShieldEntity>> {
    val _sql: String = "SELECT * FROM shields ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("shields")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfPackageName: Int = getColumnIndexOrThrow(_stmt, "packageName")
        val _columnIndexOfAppName: Int = getColumnIndexOrThrow(_stmt, "appName")
        val _columnIndexOfMode: Int = getColumnIndexOrThrow(_stmt, "mode")
        val _columnIndexOfDailyLimitMinutes: Int = getColumnIndexOrThrow(_stmt, "dailyLimitMinutes")
        val _columnIndexOfEnabled: Int = getColumnIndexOrThrow(_stmt, "enabled")
        val _columnIndexOfGraceDate: Int = getColumnIndexOrThrow(_stmt, "graceDate")
        val _columnIndexOfGraceUsedToday: Int = getColumnIndexOrThrow(_stmt, "graceUsedToday")
        val _columnIndexOfGraceExtensionUntil: Int = getColumnIndexOrThrow(_stmt, "graceExtensionUntil")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<ShieldEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ShieldEntity
          val _tmpPackageName: String
          _tmpPackageName = _stmt.getText(_columnIndexOfPackageName)
          val _tmpAppName: String
          _tmpAppName = _stmt.getText(_columnIndexOfAppName)
          val _tmpMode: ShieldMode
          _tmpMode = __ShieldMode_stringToEnum(_stmt.getText(_columnIndexOfMode))
          val _tmpDailyLimitMinutes: Int
          _tmpDailyLimitMinutes = _stmt.getLong(_columnIndexOfDailyLimitMinutes).toInt()
          val _tmpEnabled: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfEnabled).toInt()
          _tmpEnabled = _tmp != 0
          val _tmpGraceDate: String
          _tmpGraceDate = _stmt.getText(_columnIndexOfGraceDate)
          val _tmpGraceUsedToday: Int
          _tmpGraceUsedToday = _stmt.getLong(_columnIndexOfGraceUsedToday).toInt()
          val _tmpGraceExtensionUntil: Long
          _tmpGraceExtensionUntil = _stmt.getLong(_columnIndexOfGraceExtensionUntil)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = ShieldEntity(_tmpPackageName,_tmpAppName,_tmpMode,_tmpDailyLimitMinutes,_tmpEnabled,_tmpGraceDate,_tmpGraceUsedToday,_tmpGraceExtensionUntil,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun `get`(packageName: String): ShieldEntity? {
    val _sql: String = "SELECT * FROM shields WHERE packageName = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, packageName)
        val _columnIndexOfPackageName: Int = getColumnIndexOrThrow(_stmt, "packageName")
        val _columnIndexOfAppName: Int = getColumnIndexOrThrow(_stmt, "appName")
        val _columnIndexOfMode: Int = getColumnIndexOrThrow(_stmt, "mode")
        val _columnIndexOfDailyLimitMinutes: Int = getColumnIndexOrThrow(_stmt, "dailyLimitMinutes")
        val _columnIndexOfEnabled: Int = getColumnIndexOrThrow(_stmt, "enabled")
        val _columnIndexOfGraceDate: Int = getColumnIndexOrThrow(_stmt, "graceDate")
        val _columnIndexOfGraceUsedToday: Int = getColumnIndexOrThrow(_stmt, "graceUsedToday")
        val _columnIndexOfGraceExtensionUntil: Int = getColumnIndexOrThrow(_stmt, "graceExtensionUntil")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: ShieldEntity?
        if (_stmt.step()) {
          val _tmpPackageName: String
          _tmpPackageName = _stmt.getText(_columnIndexOfPackageName)
          val _tmpAppName: String
          _tmpAppName = _stmt.getText(_columnIndexOfAppName)
          val _tmpMode: ShieldMode
          _tmpMode = __ShieldMode_stringToEnum(_stmt.getText(_columnIndexOfMode))
          val _tmpDailyLimitMinutes: Int
          _tmpDailyLimitMinutes = _stmt.getLong(_columnIndexOfDailyLimitMinutes).toInt()
          val _tmpEnabled: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfEnabled).toInt()
          _tmpEnabled = _tmp != 0
          val _tmpGraceDate: String
          _tmpGraceDate = _stmt.getText(_columnIndexOfGraceDate)
          val _tmpGraceUsedToday: Int
          _tmpGraceUsedToday = _stmt.getLong(_columnIndexOfGraceUsedToday).toInt()
          val _tmpGraceExtensionUntil: Long
          _tmpGraceExtensionUntil = _stmt.getLong(_columnIndexOfGraceExtensionUntil)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result = ShieldEntity(_tmpPackageName,_tmpAppName,_tmpMode,_tmpDailyLimitMinutes,_tmpEnabled,_tmpGraceDate,_tmpGraceUsedToday,_tmpGraceExtensionUntil,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun setEnabled(packageName: String, enabled: Boolean) {
    val _sql: String = "UPDATE shields SET enabled = ? WHERE packageName = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (enabled) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _argIndex = 2
        _stmt.bindText(_argIndex, packageName)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun recordGrace(
    packageName: String,
    date: String,
    usedToday: Int,
    extensionUntil: Long,
  ) {
    val _sql: String = "UPDATE shields SET graceDate = ?, graceUsedToday = ?, graceExtensionUntil = ? WHERE packageName = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, date)
        _argIndex = 2
        _stmt.bindLong(_argIndex, usedToday.toLong())
        _argIndex = 3
        _stmt.bindLong(_argIndex, extensionUntil)
        _argIndex = 4
        _stmt.bindText(_argIndex, packageName)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  private fun __ShieldMode_enumToString(_value: ShieldMode): String = when (_value) {
    ShieldMode.LIMIT -> "LIMIT"
    ShieldMode.BLOCK -> "BLOCK"
  }

  private fun __ShieldMode_stringToEnum(_value: String): ShieldMode = when (_value) {
    "LIMIT" -> ShieldMode.LIMIT
    "BLOCK" -> ShieldMode.BLOCK
    else -> throw IllegalArgumentException("Can't convert value to enum, unknown value: " + _value)
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}

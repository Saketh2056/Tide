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
public class ScheduleDao_Impl(
  __db: RoomDatabase,
) : ScheduleDao {
  private val __db: RoomDatabase

  private val __deleteAdapterOfScheduleEntity: EntityDeleteOrUpdateAdapter<ScheduleEntity>

  private val __upsertAdapterOfScheduleEntity: EntityUpsertAdapter<ScheduleEntity>

  private val __converters: Converters = Converters()
  init {
    this.__db = __db
    this.__deleteAdapterOfScheduleEntity = object : EntityDeleteOrUpdateAdapter<ScheduleEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `schedules` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ScheduleEntity) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__upsertAdapterOfScheduleEntity = EntityUpsertAdapter<ScheduleEntity>(object : EntityInsertAdapter<ScheduleEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `schedules` (`id`,`name`,`startMinute`,`endMinute`,`daysMask`,`packageNames`,`enabled`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ScheduleEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindLong(3, entity.startMinute.toLong())
        statement.bindLong(4, entity.endMinute.toLong())
        statement.bindLong(5, entity.daysMask.toLong())
        val _tmp: String = __converters.fromPackageList(entity.packageNames)
        statement.bindText(6, _tmp)
        val _tmp_1: Int = if (entity.enabled) 1 else 0
        statement.bindLong(7, _tmp_1.toLong())
        statement.bindLong(8, entity.createdAt)
      }
    }, object : EntityDeleteOrUpdateAdapter<ScheduleEntity>() {
      protected override fun createQuery(): String = "UPDATE `schedules` SET `id` = ?,`name` = ?,`startMinute` = ?,`endMinute` = ?,`daysMask` = ?,`packageNames` = ?,`enabled` = ?,`createdAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ScheduleEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindLong(3, entity.startMinute.toLong())
        statement.bindLong(4, entity.endMinute.toLong())
        statement.bindLong(5, entity.daysMask.toLong())
        val _tmp: String = __converters.fromPackageList(entity.packageNames)
        statement.bindText(6, _tmp)
        val _tmp_1: Int = if (entity.enabled) 1 else 0
        statement.bindLong(7, _tmp_1.toLong())
        statement.bindLong(8, entity.createdAt)
        statement.bindLong(9, entity.id)
      }
    })
  }

  public override suspend fun delete(schedule: ScheduleEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfScheduleEntity.handle(_connection, schedule)
  }

  public override suspend fun upsert(schedule: ScheduleEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfScheduleEntity.upsert(_connection, schedule)
  }

  public override fun observeAll(): Flow<List<ScheduleEntity>> {
    val _sql: String = "SELECT * FROM schedules ORDER BY startMinute ASC"
    return createFlow(__db, false, arrayOf("schedules")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfStartMinute: Int = getColumnIndexOrThrow(_stmt, "startMinute")
        val _columnIndexOfEndMinute: Int = getColumnIndexOrThrow(_stmt, "endMinute")
        val _columnIndexOfDaysMask: Int = getColumnIndexOrThrow(_stmt, "daysMask")
        val _columnIndexOfPackageNames: Int = getColumnIndexOrThrow(_stmt, "packageNames")
        val _columnIndexOfEnabled: Int = getColumnIndexOrThrow(_stmt, "enabled")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<ScheduleEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ScheduleEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpStartMinute: Int
          _tmpStartMinute = _stmt.getLong(_columnIndexOfStartMinute).toInt()
          val _tmpEndMinute: Int
          _tmpEndMinute = _stmt.getLong(_columnIndexOfEndMinute).toInt()
          val _tmpDaysMask: Int
          _tmpDaysMask = _stmt.getLong(_columnIndexOfDaysMask).toInt()
          val _tmpPackageNames: List<String>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfPackageNames)
          _tmpPackageNames = __converters.toPackageList(_tmp)
          val _tmpEnabled: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfEnabled).toInt()
          _tmpEnabled = _tmp_1 != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = ScheduleEntity(_tmpId,_tmpName,_tmpStartMinute,_tmpEndMinute,_tmpDaysMask,_tmpPackageNames,_tmpEnabled,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun setEnabled(id: Long, enabled: Boolean) {
    val _sql: String = "UPDATE schedules SET enabled = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (enabled) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}

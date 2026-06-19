package com.tide.app.`data`.db

import androidx.room.EntityInsertAdapter
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
public class FocusSessionDao_Impl(
  __db: RoomDatabase,
) : FocusSessionDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfFocusSessionEntity: EntityInsertAdapter<FocusSessionEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfFocusSessionEntity = object : EntityInsertAdapter<FocusSessionEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `focus_sessions` (`id`,`startedAt`,`endedAt`,`targetMillis`,`completed`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: FocusSessionEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.startedAt)
        statement.bindLong(3, entity.endedAt)
        statement.bindLong(4, entity.targetMillis)
        val _tmp: Int = if (entity.completed) 1 else 0
        statement.bindLong(5, _tmp.toLong())
      }
    }
  }

  public override suspend fun insert(session: FocusSessionEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfFocusSessionEntity.insert(_connection, session)
  }

  public override fun observeRecent(limit: Int): Flow<List<FocusSessionEntity>> {
    val _sql: String = "SELECT * FROM focus_sessions ORDER BY startedAt DESC LIMIT ?"
    return createFlow(__db, false, arrayOf("focus_sessions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "startedAt")
        val _columnIndexOfEndedAt: Int = getColumnIndexOrThrow(_stmt, "endedAt")
        val _columnIndexOfTargetMillis: Int = getColumnIndexOrThrow(_stmt, "targetMillis")
        val _columnIndexOfCompleted: Int = getColumnIndexOrThrow(_stmt, "completed")
        val _result: MutableList<FocusSessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: FocusSessionEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpEndedAt: Long
          _tmpEndedAt = _stmt.getLong(_columnIndexOfEndedAt)
          val _tmpTargetMillis: Long
          _tmpTargetMillis = _stmt.getLong(_columnIndexOfTargetMillis)
          val _tmpCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfCompleted).toInt()
          _tmpCompleted = _tmp != 0
          _item = FocusSessionEntity(_tmpId,_tmpStartedAt,_tmpEndedAt,_tmpTargetMillis,_tmpCompleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeFocusedMillisSince(since: Long): Flow<Long> {
    val _sql: String = "SELECT COALESCE(SUM(endedAt - startedAt), 0) FROM focus_sessions WHERE startedAt >= ? AND completed = 1"
    return createFlow(__db, false, arrayOf("focus_sessions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, since)
        val _result: Long
        if (_stmt.step()) {
          val _tmp: Long
          _tmp = _stmt.getLong(0)
          _result = _tmp
        } else {
          _result = 0L
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeCompletedCount(): Flow<Int> {
    val _sql: String = "SELECT COUNT(*) FROM focus_sessions WHERE completed = 1"
    return createFlow(__db, false, arrayOf("focus_sessions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}

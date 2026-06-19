package com.tide.app.`data`.db

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class DailyUsageDao_Impl(
  __db: RoomDatabase,
) : DailyUsageDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfDailyUsageEntity: EntityInsertAdapter<DailyUsageEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfDailyUsageEntity = object : EntityInsertAdapter<DailyUsageEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `daily_usage` (`date`,`packageName`,`usageMillis`,`updatedAt`) VALUES (?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DailyUsageEntity) {
        statement.bindText(1, entity.date)
        statement.bindText(2, entity.packageName)
        statement.bindLong(3, entity.usageMillis)
        statement.bindLong(4, entity.updatedAt)
      }
    }
  }

  public override suspend fun upsertAll(entries: List<DailyUsageEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfDailyUsageEntity.insert(_connection, entries)
  }

  public override suspend fun since(fromDate: String): List<DailyUsageEntity> {
    val _sql: String = "SELECT * FROM daily_usage WHERE date >= ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, fromDate)
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfPackageName: Int = getColumnIndexOrThrow(_stmt, "packageName")
        val _columnIndexOfUsageMillis: Int = getColumnIndexOrThrow(_stmt, "usageMillis")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<DailyUsageEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DailyUsageEntity
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpPackageName: String
          _tmpPackageName = _stmt.getText(_columnIndexOfPackageName)
          val _tmpUsageMillis: Long
          _tmpUsageMillis = _stmt.getLong(_columnIndexOfUsageMillis)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item = DailyUsageEntity(_tmpDate,_tmpPackageName,_tmpUsageMillis,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun datesWithData(): List<String> {
    val _sql: String = "SELECT DISTINCT date FROM daily_usage"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: MutableList<String> = mutableListOf()
        while (_stmt.step()) {
          val _item: String
          _item = _stmt.getText(0)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun dailyTotals(limit: Int): List<DailyTotal> {
    val _sql: String = "SELECT date, SUM(usageMillis) AS totalMillis FROM daily_usage GROUP BY date ORDER BY date DESC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfDate: Int = 0
        val _columnIndexOfTotalMillis: Int = 1
        val _result: MutableList<DailyTotal> = mutableListOf()
        while (_stmt.step()) {
          val _item: DailyTotal
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpTotalMillis: Long
          _tmpTotalMillis = _stmt.getLong(_columnIndexOfTotalMillis)
          _item = DailyTotal(_tmpDate,_tmpTotalMillis)
          _result.add(_item)
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

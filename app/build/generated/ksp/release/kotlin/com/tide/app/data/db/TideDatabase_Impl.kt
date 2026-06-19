package com.tide.app.`data`.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class TideDatabase_Impl : TideDatabase() {
  private val _shieldDao: Lazy<ShieldDao> = lazy {
    ShieldDao_Impl(this)
  }

  private val _scheduleDao: Lazy<ScheduleDao> = lazy {
    ScheduleDao_Impl(this)
  }

  private val _focusSessionDao: Lazy<FocusSessionDao> = lazy {
    FocusSessionDao_Impl(this)
  }

  private val _dailyUsageDao: Lazy<DailyUsageDao> = lazy {
    DailyUsageDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1, "cc27023c25efd3fed1b46d210821ebaf", "cc6b4b7c911f6cec7d57f33a935b04a0") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `shields` (`packageName` TEXT NOT NULL, `appName` TEXT NOT NULL, `mode` TEXT NOT NULL, `dailyLimitMinutes` INTEGER NOT NULL, `enabled` INTEGER NOT NULL, `graceDate` TEXT NOT NULL, `graceUsedToday` INTEGER NOT NULL, `graceExtensionUntil` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`packageName`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `schedules` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `startMinute` INTEGER NOT NULL, `endMinute` INTEGER NOT NULL, `daysMask` INTEGER NOT NULL, `packageNames` TEXT NOT NULL, `enabled` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `focus_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `startedAt` INTEGER NOT NULL, `endedAt` INTEGER NOT NULL, `targetMillis` INTEGER NOT NULL, `completed` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `daily_usage` (`date` TEXT NOT NULL, `packageName` TEXT NOT NULL, `usageMillis` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`date`, `packageName`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'cc27023c25efd3fed1b46d210821ebaf')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `shields`")
        connection.execSQL("DROP TABLE IF EXISTS `schedules`")
        connection.execSQL("DROP TABLE IF EXISTS `focus_sessions`")
        connection.execSQL("DROP TABLE IF EXISTS `daily_usage`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsShields: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsShields.put("packageName", TableInfo.Column("packageName", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsShields.put("appName", TableInfo.Column("appName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsShields.put("mode", TableInfo.Column("mode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsShields.put("dailyLimitMinutes", TableInfo.Column("dailyLimitMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsShields.put("enabled", TableInfo.Column("enabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsShields.put("graceDate", TableInfo.Column("graceDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsShields.put("graceUsedToday", TableInfo.Column("graceUsedToday", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsShields.put("graceExtensionUntil", TableInfo.Column("graceExtensionUntil", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsShields.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysShields: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesShields: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoShields: TableInfo = TableInfo("shields", _columnsShields, _foreignKeysShields, _indicesShields)
        val _existingShields: TableInfo = read(connection, "shields")
        if (!_infoShields.equals(_existingShields)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |shields(com.tide.app.data.db.ShieldEntity).
              | Expected:
              |""".trimMargin() + _infoShields + """
              |
              | Found:
              |""".trimMargin() + _existingShields)
        }
        val _columnsSchedules: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSchedules.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSchedules.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSchedules.put("startMinute", TableInfo.Column("startMinute", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSchedules.put("endMinute", TableInfo.Column("endMinute", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSchedules.put("daysMask", TableInfo.Column("daysMask", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSchedules.put("packageNames", TableInfo.Column("packageNames", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSchedules.put("enabled", TableInfo.Column("enabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSchedules.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSchedules: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSchedules: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoSchedules: TableInfo = TableInfo("schedules", _columnsSchedules, _foreignKeysSchedules, _indicesSchedules)
        val _existingSchedules: TableInfo = read(connection, "schedules")
        if (!_infoSchedules.equals(_existingSchedules)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |schedules(com.tide.app.data.db.ScheduleEntity).
              | Expected:
              |""".trimMargin() + _infoSchedules + """
              |
              | Found:
              |""".trimMargin() + _existingSchedules)
        }
        val _columnsFocusSessions: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsFocusSessions.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("startedAt", TableInfo.Column("startedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("endedAt", TableInfo.Column("endedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("targetMillis", TableInfo.Column("targetMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("completed", TableInfo.Column("completed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysFocusSessions: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesFocusSessions: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoFocusSessions: TableInfo = TableInfo("focus_sessions", _columnsFocusSessions, _foreignKeysFocusSessions, _indicesFocusSessions)
        val _existingFocusSessions: TableInfo = read(connection, "focus_sessions")
        if (!_infoFocusSessions.equals(_existingFocusSessions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |focus_sessions(com.tide.app.data.db.FocusSessionEntity).
              | Expected:
              |""".trimMargin() + _infoFocusSessions + """
              |
              | Found:
              |""".trimMargin() + _existingFocusSessions)
        }
        val _columnsDailyUsage: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDailyUsage.put("date", TableInfo.Column("date", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDailyUsage.put("packageName", TableInfo.Column("packageName", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDailyUsage.put("usageMillis", TableInfo.Column("usageMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDailyUsage.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDailyUsage: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesDailyUsage: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoDailyUsage: TableInfo = TableInfo("daily_usage", _columnsDailyUsage, _foreignKeysDailyUsage, _indicesDailyUsage)
        val _existingDailyUsage: TableInfo = read(connection, "daily_usage")
        if (!_infoDailyUsage.equals(_existingDailyUsage)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |daily_usage(com.tide.app.data.db.DailyUsageEntity).
              | Expected:
              |""".trimMargin() + _infoDailyUsage + """
              |
              | Found:
              |""".trimMargin() + _existingDailyUsage)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "shields", "schedules", "focus_sessions", "daily_usage")
  }

  public override fun clearAllTables() {
    super.performClear(false, "shields", "schedules", "focus_sessions", "daily_usage")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(ShieldDao::class, ShieldDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ScheduleDao::class, ScheduleDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(FocusSessionDao::class, FocusSessionDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(DailyUsageDao::class, DailyUsageDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun shieldDao(): ShieldDao = _shieldDao.value

  public override fun scheduleDao(): ScheduleDao = _scheduleDao.value

  public override fun focusSessionDao(): FocusSessionDao = _focusSessionDao.value

  public override fun dailyUsageDao(): DailyUsageDao = _dailyUsageDao.value
}

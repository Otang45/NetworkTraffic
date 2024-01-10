package otang.app.network.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import otang.app.network.util.PrefUtils

class DatabaseHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    private val prefUtils: PrefUtils

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    init {
        prefUtils = PrefUtils(context)
    }

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Suppress("LocalVariableName")
    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_USERS_TABLE =
            ("CREATE TABLE " + TABLE_USAGES + "(" + KEY_USAGE_ID + " INTEGER PRIMARY KEY,"
                    + KEY_USAGE_DAY + " TEXT," + KEY_USAGE_MOBILE + " INTEGER," + KEY_USAGE_WIFI + " INTEGER,"
                    + KEY_USAGE_TOTAL + " INTEGER" + ")")
        db.execSQL(CREATE_USERS_TABLE)
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS $TABLE_USAGES")
            onCreate(db)
        }
    }

    // Insert or update a user in the database
    // Since SQLite doesn't support "upsert" we need to fall back on an attempt to UPDATE (in case the
    // user already exists) optionally followed by an INSERT (in case the user does not already exist).
    // Unfortunately, there is a bug with the insertOnConflict method
    // (https://code.google.com/p/android/issues/detail?id=13045) so we need to fall back to the more
    // verbose option of querying for the user's primary key if we did an update.
    fun addOrUpdateUser(usage: Usage) {
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        val db = writableDatabase
        db.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_USAGE_DAY, usage.day)
            values.put(KEY_USAGE_MOBILE, usage.mobile)
            values.put(KEY_USAGE_WIFI, usage.wifi)
            values.put(KEY_USAGE_TOTAL, usage.total)
            // First try to update the user in case the user already exists in the database
            // This assumes userNames are unique
            val rows = db.update(TABLE_USAGES, values, "$KEY_USAGE_DAY= ?", arrayOf(usage.day))
            // Check if update succeeded
            if (rows == 1) {
                // Get the primary key of the user we just updated
                val usersSelectQuery = String.format(
                    "SELECT %s FROM %s WHERE %s = ?", KEY_USAGE_ID, TABLE_USAGES,
                    KEY_USAGE_DAY
                )
                val cursor = db.rawQuery(usersSelectQuery, arrayOf(usage.day.toString()))
                try {
                    if (cursor!!.moveToFirst()) {
                        db.setTransactionSuccessful()
                    }
                } finally {
                    if (cursor != null && !cursor.isClosed) {
                        cursor.close()
                    }
                }
            } else {
                // user with this userName did not already exist, so insert new user
                prefUtils.saveAs("mobile", 0L)
                prefUtils.saveAs("wifi", 0L)
                prefUtils.saveAs("total", 0L)
                values.put(KEY_USAGE_MOBILE, 0)
                values.put(KEY_USAGE_WIFI, 0)
                values.put(KEY_USAGE_TOTAL, 0)
                db.insertOrThrow(TABLE_USAGES, null, values)
                db.setTransactionSuccessful()
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error while trying to add or update user")
        } finally {
            db.endTransaction()
        }
    }

    @SuppressLint("Recycle")
    fun getTodayUsage(day: String?): Usage {
        val db = this.writableDatabase
        val usage = Usage()
        val sql = "SELECT * FROM $TABLE_USAGES WHERE $KEY_USAGE_DAY = '$day'"
        val c = db.rawQuery(sql, null)
        if (c.moveToFirst()) {
            usage.day = c.getString(1)
            usage.mobile = c.getLong(2)
            usage.wifi = c.getLong(3)
            usage.total = c.getLong(4)
        }
        return usage
    }

    val usageList: ArrayList<Usage>
        @SuppressLint("Recycle")
        get() {
            val db = this.writableDatabase
            val usageList = ArrayList<Usage>()
            val sql = "SELECT * FROM $TABLE_USAGES ORDER BY id DESC limit 30"
            val c = db.rawQuery(sql, null)
            if (c.moveToFirst()) {
                do {
                    val usage = Usage()
                    usage.day = c.getString(1)
                    usage.mobile = c.getLong(2)
                    usage.wifi = c.getLong(3)
                    usage.total = c.getLong(4)
                    usageList.add(usage)
                } while (c.moveToNext())
            }
            return usageList
        }

    companion object {
        private const val TAG = "DB"

        // Database Info
        private const val DATABASE_NAME = "data"
        private const val DATABASE_VERSION = 1

        // Table Names
        private const val TABLE_USAGES = "usage"

        // User Table Columns
        private const val KEY_USAGE_ID = "id"
        private const val KEY_USAGE_DAY = "day"
        private const val KEY_USAGE_MOBILE = "mobile"
        private const val KEY_USAGE_WIFI = "wifi"
        private const val KEY_USAGE_TOTAL = "total"

        // Instance
        private var sInstance: DatabaseHelper? = null

        @Synchronized
        fun getInstance(context: Context): DatabaseHelper? {
            // Use the application context, which will ensure that you
            // don't accidentally leak an Activity's context.
            // See this article for more information: http://bit.ly/6LRzfx
            if (sInstance == null) {
                sInstance = DatabaseHelper(context.applicationContext)
            }
            return sInstance
        }
    }
}
package otang.network.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import otang.network.util.PrefUtils;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String TAG = "DB";
	// Database Info
	private static final String DATABASE_NAME = "data";
	private static final int DATABASE_VERSION = 1;
	// Table Names
	private static final String TABLE_USAGES = "usage";
	// User Table Columns
	private static final String KEY_USAGE_ID = "id";
	private static final String KEY_USAGE_DAY = "day";
	private static final String KEY_USAGE_MOBILE = "mobile";
	private static final String KEY_USAGE_WIFI = "wifi";
	private static final String KEY_USAGE_TOTAL = "total";
	// Instance
	private static DatabaseHelper sInstance;
	private PrefUtils prefUtils;

	public static synchronized DatabaseHelper getInstance(Context context) {
		// Use the application context, which will ensure that you
		// don't accidentally leak an Activity's context.
		// See this article for more information: http://bit.ly/6LRzfx
		if (sInstance == null) {
			sInstance = new DatabaseHelper(context.getApplicationContext());
		}
		return sInstance;
	}

	/**
	* Constructor should be private to prevent direct instantiation.
	* Make a call to the static method "getInstance()" instead.
	*/
	private DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.prefUtils = new PrefUtils(context);

	}

	// Called when the database connection is being configured.
	// Configure database settings for things like foreign key support, write-ahead logging, etc.
	@Override
	public void onConfigure(SQLiteDatabase db) {
		super.onConfigure(db);
		db.setForeignKeyConstraintsEnabled(true);
	}

	// Called when the database is created for the FIRST time.
	// If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USAGES + "(" + KEY_USAGE_ID + " INTEGER PRIMARY KEY,"
				+ KEY_USAGE_DAY + " TEXT," + KEY_USAGE_MOBILE + " INTEGER," + KEY_USAGE_WIFI + " INTEGER,"
				+ KEY_USAGE_TOTAL + " INTEGER" + ")";
		db.execSQL(CREATE_USERS_TABLE);
	}

	// Called when the database needs to be upgraded.
	// This method will only be called if a database already exists on disk with the same DATABASE_NAME,
	// but the DATABASE_VERSION is different than the version of the database that exists on disk.
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion != newVersion) {
			// Simplest implementation is to drop all old tables and recreate them
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_USAGES);
			onCreate(db);
		}
	}

	// Insert or update a user in the database
	// Since SQLite doesn't support "upsert" we need to fall back on an attempt to UPDATE (in case the
	// user already exists) optionally followed by an INSERT (in case the user does not already exist).
	// Unfortunately, there is a bug with the insertOnConflict method
	// (https://code.google.com/p/android/issues/detail?id=13045) so we need to fall back to the more
	// verbose option of querying for the user's primary key if we did an update.
	public void addOrUpdateUser(Usage usage) {
		// The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			values.put(KEY_USAGE_DAY, usage.day);
			values.put(KEY_USAGE_MOBILE, usage.mobile);
			values.put(KEY_USAGE_WIFI, usage.wifi);
			values.put(KEY_USAGE_TOTAL, usage.total);
			// First try to update the user in case the user already exists in the database
			// This assumes userNames are unique
			int rows = db.update(TABLE_USAGES, values, KEY_USAGE_DAY + "= ?", new String[] { usage.day });
			// Check if update succeeded
			if (rows == 1) {
				// Get the primary key of the user we just updated
				String usersSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?", KEY_USAGE_ID, TABLE_USAGES,
						KEY_USAGE_DAY);
				Cursor cursor = db.rawQuery(usersSelectQuery, new String[] { String.valueOf(usage.day) });
				try {
					if (cursor.moveToFirst()) {
						db.setTransactionSuccessful();
					}
				} finally {
					if (cursor != null && !cursor.isClosed()) {
						cursor.close();
					}
				}
			} else {
				// user with this userName did not already exist, so insert new user
				prefUtils.saveAs("mobile", (long) 0);
				prefUtils.saveAs("wifi", (long) 0);
				prefUtils.saveAs("total", (long) 0);
				values.put(KEY_USAGE_MOBILE, 0);
				values.put(KEY_USAGE_WIFI, 0);
				values.put(KEY_USAGE_TOTAL, 0);
				db.insertOrThrow(TABLE_USAGES, null, values);
				db.setTransactionSuccessful();
			}
		} catch (Exception e) {
			Log.d(TAG, "Error while trying to add or update user");
		} finally {
			db.endTransaction();
		}
	}

	public Usage getTodayUsage(String day) {
		SQLiteDatabase db = this.getWritableDatabase();
		Usage usage = new Usage();
		String sql = "SELECT * FROM " + TABLE_USAGES + " WHERE " + KEY_USAGE_DAY + " = '" + day + "'";
		Cursor c = db.rawQuery(sql, null);
		if (c.moveToFirst()) {
			usage.day = c.getString(1);
			usage.mobile = c.getLong(2);
			usage.wifi = c.getLong(3);
			usage.total = c.getLong(4);
		}
		return usage;
	}

	public ArrayList<Usage> getUsageList() {
		SQLiteDatabase db = this.getWritableDatabase();
		ArrayList<Usage> usageList = new ArrayList<>();
		String sql = "SELECT * FROM " + TABLE_USAGES + " ORDER BY id DESC limit 30";
		Cursor c = db.rawQuery(sql, null);
		if (c.moveToFirst()) {
			do {
				Usage usage = new Usage();
				usage.day = c.getString(1);
				usage.mobile = c.getLong(2);
				usage.wifi = c.getLong(3);
				usage.total = c.getLong(4);
				usageList.add(usage);
			} while (c.moveToNext());
		}
		return usageList;
	}
}
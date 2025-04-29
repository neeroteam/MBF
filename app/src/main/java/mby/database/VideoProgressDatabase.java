package mby.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class VideoProgressDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "video_progress.db";
    private static final int DATABASE_VERSION = 1;
    
    private static final String TABLE_PROGRESS = "video_progress";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_VIDEO_URL = "video_url";
    private static final String COLUMN_POSITION = "position";
    private static final String COLUMN_DURATION = "duration";

    public VideoProgressDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_PROGRESS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_VIDEO_URL + " TEXT UNIQUE, " +
                COLUMN_POSITION + " INTEGER, " +
                COLUMN_DURATION + " INTEGER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROGRESS);
        onCreate(db);
    }

    public void saveProgress(String videoUrl, long position, long duration) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_VIDEO_URL, videoUrl);
        values.put(COLUMN_POSITION, position);
        values.put(COLUMN_DURATION, duration);

        db.insertWithOnConflict(TABLE_PROGRESS, null, values, 
            SQLiteDatabase.CONFLICT_REPLACE);
    }

    public long getProgress(String videoUrl) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PROGRESS,
                new String[]{COLUMN_POSITION},
                COLUMN_VIDEO_URL + " = ?",
                new String[]{videoUrl},
                null, null, null);

        if (cursor.moveToFirst()) {
            long position = cursor.getLong(0);
            cursor.close();
            return position;
        }
        cursor.close();
        return 0;
    }
} 
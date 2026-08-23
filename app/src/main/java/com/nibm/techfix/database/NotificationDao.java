package com.nibm.techfix.database;

import static com.nibm.techfix.database.DBHelper.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Real SQLite DAO for the Notification domain.
 * SQL, ContentValues and Cursor mapping for this domain live here, not in DBHelper.
 */
public class NotificationDao {
    private final DBHelper helper;

    public NotificationDao(Context context) {
        this(DBHelper.getInstance(context));
    }

    NotificationDao(DBHelper helper) {
        this.helper = helper;
    }

    public void insertNotification(int userId, String message) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("message", message);
        values.put("created_at", new java.text.SimpleDateFormat("MMM d, h:mm a", java.util.Locale.getDefault())
                .format(new java.util.Date()));
        values.put("is_read", 0);
        db.insert(TABLE_NOTIFICATION, null, values);
    }

    public List<String[]> getNotificationsForUser(int userId) {
        List<String[]> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NOTIFICATION, null, "user_id = ?",
                new String[]{String.valueOf(userId)}, null, null, "id DESC");
        if (cursor.moveToFirst()) {
            do {
                list.add(new String[]{
                        String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("id"))),
                        cursor.getString(cursor.getColumnIndexOrThrow("message")),
                        cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                        String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("is_read")))
                });
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public int getUnreadNotificationCount(int userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NOTIFICATION, new String[]{"COUNT(*)"},
                "user_id = ? AND is_read = 0", new String[]{String.valueOf(userId)}, null, null, null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public void markAllNotificationsRead(int userId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_read", 1);
        db.update(TABLE_NOTIFICATION, values, "user_id = ?", new String[]{String.valueOf(userId)});
    }
}

package com.nibm.techfix.database;

import static com.nibm.techfix.database.DBHelper.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.nibm.techfix.models.User;

/**
 * Real SQLite DAO for the User domain.
 * SQL, ContentValues and Cursor mapping for this domain live here, not in DBHelper.
 */
public class UserDao {
    private final DBHelper helper;

    public UserDao(Context context) {
        this(DBHelper.getInstance(context));
    }

    UserDao(DBHelper helper) {
        this.helper = helper;
    }

    public long registerUser(String name, String email, String phone, String password) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email.trim().toLowerCase());
        values.put("phone", phone);
        values.put("password_hash", DBHelper.hash(password));
        values.put("is_admin", 0); // customers only - staff accounts are seeded separately
        long id = db.insertWithOnConflict(TABLE_USER, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        return id;
    }

    @Nullable
    public User login(String email, String password) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER, null, "email = ? AND password_hash = ?",
                new String[]{email.trim().toLowerCase(), DBHelper.hash(password)}, null, null, null);

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("email")),
                    cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                    null, // never expose the hash back out
                    cursor.getInt(cursor.getColumnIndexOrThrow("is_admin")) == 1
            );
        }
        cursor.close();
        return user;
    }

    public boolean changePassword(int userId, String currentPassword, String newPassword) {
        // One writable connection is sufficient for both verification and update.
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query(TABLE_USER, new String[]{"password_hash"}, "id = ?",
                new String[]{String.valueOf(userId)}, null, null, null);

        boolean matches = false;
        if (cursor.moveToFirst()) {
            String storedHash = cursor.getString(cursor.getColumnIndexOrThrow("password_hash"));
            matches = storedHash.equals(DBHelper.hash(currentPassword));
        }
        cursor.close();

        if (!matches) return false;

        ContentValues values = new ContentValues();
        values.put("password_hash", DBHelper.hash(newPassword));
        return db.update(TABLE_USER, values, "id = ?",
                new String[]{String.valueOf(userId)}) > 0;
    }

    @Nullable
    public User getUserById(int id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER, null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("email")),
                    cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                    null,
                    cursor.getInt(cursor.getColumnIndexOrThrow("is_admin")) == 1
            );
        }
        cursor.close();
        return user;
    }
}

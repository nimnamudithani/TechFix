package com.nibm.techfix.database;

import static com.nibm.techfix.database.DBHelper.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Real SQLite DAO for the SampleImage domain.
 * SQL, ContentValues and Cursor mapping for this domain live here, not in DBHelper.
 */
public class SampleImageDao {
    private final DBHelper helper;

    public SampleImageDao(Context context) {
        this(DBHelper.getInstance(context));
    }

    SampleImageDao(DBHelper helper) {
        this.helper = helper;
    }

    public long insertSampleImage(String imagePath, String caption) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("image_path", imagePath);
        values.put("caption", caption);
        long id = db.insert(TABLE_SAMPLE_IMAGE, null, values);
        return id;
    }

    public void deleteSampleImage(int id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(TABLE_SAMPLE_IMAGE, "id = ?", new String[]{String.valueOf(id)});
    }

    public List<String[]> getAllSampleImages() {
        // Each entry: [id, imagePath, caption]
        List<String[]> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SAMPLE_IMAGE, null, null, null, null, null, "id DESC");
        if (cursor.moveToFirst()) {
            do {
                list.add(new String[]{
                        String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("id"))),
                        cursor.getString(cursor.getColumnIndexOrThrow("image_path")),
                        cursor.getString(cursor.getColumnIndexOrThrow("caption"))
                });
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}

package com.nibm.techfix.database;

import static com.nibm.techfix.database.DBHelper.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.nibm.techfix.models.Technician;

import java.util.ArrayList;
import java.util.List;

/**
 * Real SQLite DAO for the Technician domain.
 * SQL, ContentValues and Cursor mapping for this domain live here, not in DBHelper.
 */
public class TechnicianDao {
    private final DBHelper helper;

    public TechnicianDao(Context context) {
        this(DBHelper.getInstance(context));
    }

    TechnicianDao(DBHelper helper) {
        this.helper = helper;
    }

    public List<Technician> getAvailableTechnicians(int branchId, String specialization) {
        List<Technician> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TECHNICIAN, null,
                "branch_id = ? AND specialization = ? AND available = 1",
                new String[]{String.valueOf(branchId), specialization}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new Technician(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("branch_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("specialization")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("available")) == 1
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    @Nullable
    public Technician getTechnicianById(int id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TECHNICIAN, null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        Technician technician = null;
        if (cursor.moveToFirst()) {
            technician = new Technician(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("branch_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("specialization")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("available")) == 1
            );
        }
        cursor.close();
        return technician;
    }

    public long insertTechnician(String name, int branchId, String specialization) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("branch_id", branchId);
        values.put("specialization", specialization);
        values.put("available", 1);
        long id = db.insert(TABLE_TECHNICIAN, null, values);
        return id;
    }

    public void updateTechnicianAvailability(int id, boolean available) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("available", available ? 1 : 0);
        db.update(TABLE_TECHNICIAN, values, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteTechnician(int id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(TABLE_TECHNICIAN, "id = ?", new String[]{String.valueOf(id)});
    }

    public List<Technician> getAllTechnicians() {
        List<Technician> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TECHNICIAN, null, null, null, null, null, "name ASC");
        if (cursor.moveToFirst()) {
            do {
                list.add(new Technician(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("branch_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("specialization")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("available")) == 1
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}

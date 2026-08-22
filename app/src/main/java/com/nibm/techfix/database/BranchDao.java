package com.nibm.techfix.database;

import static com.nibm.techfix.database.DBHelper.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.nibm.techfix.models.Branch;

import java.util.ArrayList;
import java.util.List;

/**
 * Real SQLite DAO for the Branch domain.
 * SQL, ContentValues and Cursor mapping for this domain live here, not in DBHelper.
 */
public class BranchDao {
    private final DBHelper helper;

    public BranchDao(Context context) {
        this(DBHelper.getInstance(context));
    }

    BranchDao(DBHelper helper) {
        this.helper = helper;
    }

    public List<Branch> getAllBranches() {
        List<Branch> branches = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BRANCH, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                branches.add(new Branch(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("address")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")),
                        cursor.getString(cursor.getColumnIndexOrThrow("contact_number"))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return branches;
    }

    @Nullable
    public Branch getBranchById(int id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BRANCH, null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        Branch branch = null;
        if (cursor.moveToFirst()) {
            branch = new Branch(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("address")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")),
                    cursor.getString(cursor.getColumnIndexOrThrow("contact_number"))
            );
        }
        cursor.close();
        return branch;
    }
}

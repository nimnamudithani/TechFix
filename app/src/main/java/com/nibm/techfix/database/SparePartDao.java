package com.nibm.techfix.database;

import static com.nibm.techfix.database.DBHelper.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Real SQLite DAO for the SparePart domain.
 * SQL, ContentValues and Cursor mapping for this domain live here, not in DBHelper.
 */
public class SparePartDao {
    private final DBHelper helper;

    public SparePartDao(Context context) {
        this(DBHelper.getInstance(context));
    }

    SparePartDao(DBHelper helper) {
        this.helper = helper;
    }

    public boolean hasRequiredSparePartInStock(int branchId, String serviceName) {
        String partKeyword = null;
        if (serviceName != null) {
            String normalized = serviceName.toLowerCase();
            if (normalized.contains("screen")) partKeyword = "%Screen%";
            else if (normalized.contains("battery")) partKeyword = "%Battery%";
            else if (normalized.contains("keyboard")) partKeyword = "%Keyboard%";
        }

        // Diagnosis/repair services with no specific replacement part in the
        // current catalogue do not need a spare-part stock check.
        if (partKeyword == null) return true;

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SPARE_PART, new String[]{"id"},
                "branch_id = ? AND stock_qty > 0 AND name LIKE ?",
                new String[]{String.valueOf(branchId), partKeyword}, null, null, null);
        boolean hasStock = cursor.moveToFirst();
        cursor.close();
        return hasStock;
    }
}

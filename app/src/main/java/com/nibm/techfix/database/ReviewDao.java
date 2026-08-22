package com.nibm.techfix.database;

import static com.nibm.techfix.database.DBHelper.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nibm.techfix.models.RepairAppointment;

/**
 * Real SQLite DAO for the Review domain.
 * SQL, ContentValues and Cursor mapping for this domain live here, not in DBHelper.
 */
public class ReviewDao {
    private final DBHelper helper;

    public ReviewDao(Context context) {
        this(DBHelper.getInstance(context));
    }

    ReviewDao(DBHelper helper) {
        this.helper = helper;
    }

    public long addReview(int appointmentId, int userId, int rating, String comment) {
        if (rating < 1 || rating > 5) return -1;
        SQLiteDatabase db = helper.getWritableDatabase();

        // Fetch only the field required by this DAO. Avoid loading a full
        // RepairAppointment through another DAO just to obtain technician_id.
        Cursor appointmentCursor = db.query(
                TABLE_APPOINTMENT,
                new String[]{"technician_id", "user_id", "status"},
                "id = ?",
                new String[]{String.valueOf(appointmentId)},
                null, null, null
        );

        if (!appointmentCursor.moveToFirst()) {
            appointmentCursor.close();
            return -1;
        }

        int technicianId = appointmentCursor.getInt(
                appointmentCursor.getColumnIndexOrThrow("technician_id"));
        int appointmentUserId = appointmentCursor.getInt(
                appointmentCursor.getColumnIndexOrThrow("user_id"));
        String appointmentStatus = appointmentCursor.getString(
                appointmentCursor.getColumnIndexOrThrow("status"));
        appointmentCursor.close();

        if (technicianId <= 0 || appointmentUserId != userId ||
                !(RepairAppointment.STATUS_COMPLETED.equals(appointmentStatus) ||
                  RepairAppointment.STATUS_PAID.equals(appointmentStatus))) {
            return -1;
        }

        ContentValues values = new ContentValues();
        values.put("appointment_id", appointmentId);
        values.put("user_id", userId);
        values.put("technician_id", technicianId);
        values.put("rating", rating);
        values.put("comment", comment == null ? "" : comment.trim());
        values.put("created_at", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(new java.util.Date()));
        long id = db.insertWithOnConflict(TABLE_REVIEW, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        return id;
    }

    public float getAverageRatingForTechnician(int technicianId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT AVG(rating) FROM " + TABLE_REVIEW + " WHERE technician_id = ?",
                new String[]{String.valueOf(technicianId)});
        float average = 0f;
        if (cursor.moveToFirst() && !cursor.isNull(0)) average = cursor.getFloat(0);
        cursor.close();
        return average;
    }

    public int getReviewCountForTechnician(int technicianId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_REVIEW + " WHERE technician_id = ?",
                new String[]{String.valueOf(technicianId)});
        int count = cursor.moveToFirst() ? cursor.getInt(0) : 0;
        cursor.close();
        return count;
    }

    public String[] getReviewForAppointment(int appointmentId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_REVIEW, new String[]{"rating", "comment"}, "appointment_id = ?",
                new String[]{String.valueOf(appointmentId)}, null, null, null);
        String[] result = null;
        if (cursor.moveToFirst()) result = new String[]{String.valueOf(cursor.getInt(0)), cursor.getString(1)};
        cursor.close();
        return result;
    }
}

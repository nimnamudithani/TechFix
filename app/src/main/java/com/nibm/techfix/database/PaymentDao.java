package com.nibm.techfix.database;

import static com.nibm.techfix.database.DBHelper.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nibm.techfix.models.RepairAppointment;

/**
 * Real SQLite DAO for the Payment domain.
 * SQL, ContentValues and Cursor mapping for this domain live here, not in DBHelper.
 */
public class PaymentDao {
    private final DBHelper helper;
    private final AppointmentDao appointmentDao;

    public PaymentDao(Context context) {
        this(DBHelper.getInstance(context));
    }

    PaymentDao(DBHelper helper) {
        this.helper = helper;
        this.appointmentDao = new AppointmentDao(helper);
    }

    public long recordPayment(int appointmentId, double amount, String method) {
        SQLiteDatabase db = helper.getWritableDatabase();

        // Prevent duplicate payment rows if staff taps "Mark Paid" more than once.
        Cursor existing = db.query(TABLE_PAYMENT, new String[]{"id"},
                "appointment_id = ? AND status = ?",
                new String[]{String.valueOf(appointmentId), "Paid"},
                null, null, null, "1");
        if (existing.moveToFirst()) {
            long existingId = existing.getLong(existing.getColumnIndexOrThrow("id"));
            existing.close();
            // Payment already exists. Do not update the status again because that
            // would create a duplicate customer notification.
            return existingId;
        }
        existing.close();

        ContentValues values = new ContentValues();
        values.put("appointment_id", appointmentId);
        values.put("amount", amount);
        values.put("method", method);
        values.put("status", "Paid");
        values.put("paid_at", String.valueOf(System.currentTimeMillis()));
        long id = db.insert(TABLE_PAYMENT, null, values);
        if (id != -1) {
            appointmentDao.updateAppointmentStatus(appointmentId, RepairAppointment.STATUS_PAID);
        }
        return id;
    }

    public String[] getPaymentForAppointment(int appointmentId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PAYMENT,
                new String[]{"amount", "method", "status", "paid_at"},
                "appointment_id = ?",
                new String[]{String.valueOf(appointmentId)},
                null, null, "id DESC", "1");
        String[] result = null;
        if (cursor.moveToFirst()) {
            result = new String[]{
                    String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow("amount"))),
                    cursor.getString(cursor.getColumnIndexOrThrow("method")),
                    cursor.getString(cursor.getColumnIndexOrThrow("status")),
                    cursor.getString(cursor.getColumnIndexOrThrow("paid_at"))
            };
        }
        cursor.close();
        return result;
    }
}

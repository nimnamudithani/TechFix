package com.nibm.techfix.database;

import static com.nibm.techfix.database.DBHelper.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.nibm.techfix.models.Branch;
import com.nibm.techfix.models.RepairAppointment;

import java.util.ArrayList;
import java.util.List;

/**
 * Real SQLite DAO for the Appointment domain.
 * SQL, ContentValues and Cursor mapping for this domain live here, not in DBHelper.
 */
public class AppointmentDao {
    private final DBHelper helper;
    private final BranchDao branchDao;
    private final NotificationDao notificationDao;

    public AppointmentDao(Context context) {
        this(DBHelper.getInstance(context));
    }

    AppointmentDao(DBHelper helper) {
        this.helper = helper;
        this.branchDao = new BranchDao(helper);
        this.notificationDao = new NotificationDao(helper);
    }

    public long createAppointment(RepairAppointment appt) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", appt.getUserId());
        values.put("branch_id", appt.getBranchId());
        values.put("technician_id", appt.getTechnicianId());
        values.put("device_category_id", appt.getDeviceCategoryId());
        values.put("repair_service_id", appt.getRepairServiceId());
        values.put("status", appt.getStatus());
        values.put("requested_date", appt.getRequestedDate());
        long id = db.insert(TABLE_APPOINTMENT, null, values);
        if (id != -1) {
            Branch branch = branchDao.getBranchById(appt.getBranchId());
            String branchName = branch != null ? branch.getName() : "a branch";
            notificationDao.insertNotification(appt.getUserId(),
                    "Your repair request was submitted and assigned to " + branchName + ".");
        }
        return id;
    }

    public List<RepairAppointment> getAppointmentsForUser(int userId) {
        List<RepairAppointment> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_APPOINTMENT, null, "user_id = ?",
                new String[]{String.valueOf(userId)}, null, null, "id DESC");
        if (cursor.moveToFirst()) {
            do {
                list.add(mapAppointment(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<RepairAppointment> getCompletedAppointmentsForUser(int userId) {
        List<RepairAppointment> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_APPOINTMENT, null, "user_id = ? AND status IN (?, ?)",
                new String[]{String.valueOf(userId), RepairAppointment.STATUS_COMPLETED, RepairAppointment.STATUS_PAID},
                null, null, "id DESC");
        if (cursor.moveToFirst()) {
            do {
                list.add(mapAppointment(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<RepairAppointment> getOngoingAppointmentsForUser(int userId) {
        List<RepairAppointment> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_APPOINTMENT, null,
                "user_id = ? AND status NOT IN (?, ?)",
                new String[]{String.valueOf(userId), RepairAppointment.STATUS_COMPLETED, RepairAppointment.STATUS_PAID},
                null, null, "id DESC");
        if (cursor.moveToFirst()) {
            do {
                list.add(mapAppointment(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<RepairAppointment> getFinishedAppointmentsForUser(int userId) {
        List<RepairAppointment> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_APPOINTMENT, null,
                "user_id = ? AND status IN (?, ?)",
                new String[]{String.valueOf(userId), RepairAppointment.STATUS_COMPLETED, RepairAppointment.STATUS_PAID},
                null, null, "id DESC");
        if (cursor.moveToFirst()) {
            do {
                list.add(mapAppointment(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<RepairAppointment> getAllOngoingAppointments() {
        List<RepairAppointment> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_APPOINTMENT, null,
                "status NOT IN (?, ?)",
                new String[]{RepairAppointment.STATUS_COMPLETED, RepairAppointment.STATUS_PAID},
                null, null, "id DESC");
        if (cursor.moveToFirst()) {
            do {
                list.add(mapAppointment(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<RepairAppointment> getAllFinishedAppointments() {
        List<RepairAppointment> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_APPOINTMENT, null,
                "status IN (?, ?)",
                new String[]{RepairAppointment.STATUS_COMPLETED, RepairAppointment.STATUS_PAID},
                null, null, "id DESC");
        if (cursor.moveToFirst()) {
            do {
                list.add(mapAppointment(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    @Nullable
    public RepairAppointment getAppointmentById(int id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_APPOINTMENT, null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        RepairAppointment appt = null;
        if (cursor.moveToFirst()) {
            appt = mapAppointment(cursor);
        }
        cursor.close();
        return appt;
    }

    public int updateAppointmentStatus(int appointmentId, String status) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);

        int rowsUpdated = db.update(
                TABLE_APPOINTMENT,
                values,
                "id = ?",
                new String[]{String.valueOf(appointmentId)}
        );

        // Never send a success notification if no appointment row was updated.
        if (rowsUpdated > 0) {
            RepairAppointment appt = getAppointmentById(appointmentId);
            if (appt != null) {
                String friendlyStatus = status.equals(RepairAppointment.STATUS_PAID) ? "Paid" : status;
                notificationDao.insertNotification(
                        appt.getUserId(),
                        "Your repair status changed to " + friendlyStatus + "."
                );
            }
        }
        return rowsUpdated;
    }

    public void updateAppointmentImage(int appointmentId, String beforeOrAfter, String path) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(beforeOrAfter.equals("before") ? "before_image_path" : "after_image_path", path);
        db.update(TABLE_APPOINTMENT, values, "id = ?", new String[]{String.valueOf(appointmentId)});
    }

    private RepairAppointment mapAppointment(Cursor cursor) {
        RepairAppointment appt = new RepairAppointment(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                cursor.getInt(cursor.getColumnIndexOrThrow("branch_id")),
                cursor.getInt(cursor.getColumnIndexOrThrow("technician_id")),
                cursor.getInt(cursor.getColumnIndexOrThrow("device_category_id")),
                cursor.getInt(cursor.getColumnIndexOrThrow("repair_service_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("status")),
                cursor.getString(cursor.getColumnIndexOrThrow("requested_date"))
        );
        appt.setBeforeImagePath(cursor.getString(cursor.getColumnIndexOrThrow("before_image_path")));
        appt.setAfterImagePath(cursor.getString(cursor.getColumnIndexOrThrow("after_image_path")));
        return appt;
    }

    public List<RepairAppointment> getAllAppointments() {
        List<RepairAppointment> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_APPOINTMENT, null, null, null, null, null, "id DESC");
        if (cursor.moveToFirst()) {
            do {
                list.add(mapAppointment(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}

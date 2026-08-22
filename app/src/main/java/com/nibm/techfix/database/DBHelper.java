package com.nibm.techfix.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;


import java.security.MessageDigest;

/**
 * SQLite schema/lifecycle owner for TechFix.
 *
 * This class is intentionally limited to table creation, upgrades, seed data
 * and shared password hashing. Domain queries live in focused DAO classes.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "techfix.db";
    private static final int DB_VERSION = 6;

    private static volatile DBHelper instance;

    public static final String TABLE_USER = "user";
    public static final String TABLE_BRANCH = "branch";
    public static final String TABLE_TECHNICIAN = "technician";
    public static final String TABLE_DEVICE_CATEGORY = "device_category";
    public static final String TABLE_REPAIR_SERVICE = "repair_service";
    public static final String TABLE_SPARE_PART = "spare_part";
    public static final String TABLE_APPOINTMENT = "appointment";
    public static final String TABLE_PAYMENT = "payment";
    public static final String TABLE_SAMPLE_IMAGE = "sample_image";
    public static final String TABLE_NOTIFICATION = "notification";
    public static final String TABLE_REVIEW = "review";

    public DBHelper(@Nullable Context context) {
        super(context != null ? context.getApplicationContext() : null, DB_NAME, null, DB_VERSION);
    }

    /**
     * Returns the single application-wide SQLiteOpenHelper used by all DAOs.
     * Sharing one helper avoids opening independent helper/connection stacks
     * when DAOs call one another.
     */
    public static DBHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DBHelper.class) {
                if (instance == null) {
                    instance = new DBHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USER + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, email TEXT UNIQUE, phone TEXT, password_hash TEXT, is_admin INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE " + TABLE_BRANCH + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, address TEXT, latitude REAL, longitude REAL, contact_number TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_TECHNICIAN + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, branch_id INTEGER, specialization TEXT, available INTEGER)");

        db.execSQL("CREATE TABLE " + TABLE_DEVICE_CATEGORY + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_REPAIR_SERVICE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "device_category_id INTEGER, name TEXT, description TEXT, base_price REAL)");

        db.execSQL("CREATE TABLE " + TABLE_SPARE_PART + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, branch_id INTEGER, stock_qty INTEGER, unit_price REAL)");

        db.execSQL("CREATE TABLE " + TABLE_APPOINTMENT + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, branch_id INTEGER, technician_id INTEGER, " +
                "device_category_id INTEGER, repair_service_id INTEGER, " +
                "status TEXT, requested_date TEXT, " +
                "before_image_path TEXT, after_image_path TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_PAYMENT + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "appointment_id INTEGER, amount REAL, method TEXT, status TEXT, paid_at TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_SAMPLE_IMAGE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "image_path TEXT, caption TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_NOTIFICATION + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, message TEXT, created_at TEXT, is_read INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE " + TABLE_REVIEW + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "appointment_id INTEGER UNIQUE, user_id INTEGER, technician_id INTEGER, " +
                "rating INTEGER, comment TEXT, created_at TEXT)");

        seedData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 5 adds customer ratings/reviews. Use a non-destructive migration
        // so existing accounts, appointments and repair history are preserved.
        if (oldVersion < 5) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_REVIEW + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "appointment_id INTEGER UNIQUE, user_id INTEGER, technician_id INTEGER, " +
                    "rating INTEGER, comment TEXT, created_at TEXT)");
        }
        if (oldVersion >= 5 && oldVersion < 6) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_REVIEW + " ADD COLUMN technician_id INTEGER");
            } catch (Exception ignored) { }
            db.execSQL("UPDATE " + TABLE_REVIEW +
                    " SET technician_id = (SELECT technician_id FROM " + TABLE_APPOINTMENT +
                    " WHERE " + TABLE_APPOINTMENT + ".id = " + TABLE_REVIEW + ".appointment_id)" +
                    " WHERE technician_id IS NULL");
        }
    }

    /** Inserts starter data so the app isn't empty on first run. */
    private void seedData(SQLiteDatabase db) {
        // Default staff/admin account for testing the "Manage TechFix" side.
        // Customers who register through the app always get is_admin = 0.
        db.execSQL("INSERT INTO " + TABLE_USER + " (name, email, phone, password_hash, is_admin) VALUES " +
                "('TechFix Staff', 'staff@techfix.lk', '0770000000', '" + hash("staff123") + "', 1)");

        db.execSQL("INSERT INTO " + TABLE_BRANCH + " (name, address, latitude, longitude, contact_number) VALUES " +
                "('TechFix Colombo', '123 Galle Road, Colombo 03', 6.9271, 79.8612, '011-2345678')");
        db.execSQL("INSERT INTO " + TABLE_BRANCH + " (name, address, latitude, longitude, contact_number) VALUES " +
                "('TechFix Galle', '45 Main Street, Galle', 6.0535, 80.2210, '091-2233445')");

        db.execSQL("INSERT INTO " + TABLE_DEVICE_CATEGORY + " (name) VALUES ('Mobile Phone')");
        db.execSQL("INSERT INTO " + TABLE_DEVICE_CATEGORY + " (name) VALUES ('Laptop')");
        db.execSQL("INSERT INTO " + TABLE_DEVICE_CATEGORY + " (name) VALUES ('Desktop')");
        db.execSQL("INSERT INTO " + TABLE_DEVICE_CATEGORY + " (name) VALUES ('Tablet')");

        db.execSQL("INSERT INTO " + TABLE_REPAIR_SERVICE + " (device_category_id, name, description, base_price) VALUES " +
                "(1, 'Screen Replacement', 'Replace a cracked or unresponsive mobile screen', 8500)");
        db.execSQL("INSERT INTO " + TABLE_REPAIR_SERVICE + " (device_category_id, name, description, base_price) VALUES " +
                "(1, 'Battery Replacement', 'Replace a degraded phone battery', 3500)");
        db.execSQL("INSERT INTO " + TABLE_REPAIR_SERVICE + " (device_category_id, name, description, base_price) VALUES " +
                "(2, 'Keyboard Repair', 'Fix or replace a faulty laptop keyboard', 6000)");
        db.execSQL("INSERT INTO " + TABLE_REPAIR_SERVICE + " (device_category_id, name, description, base_price) VALUES " +
                "(2, 'Motherboard Diagnosis', 'Diagnose and repair laptop motherboard issues', 12000)");
        db.execSQL("INSERT INTO " + TABLE_REPAIR_SERVICE + " (device_category_id, name, description, base_price) VALUES " +
                "(3, 'Power Supply Repair', 'Fix desktop power supply issues', 5500)");

        db.execSQL("INSERT INTO " + TABLE_TECHNICIAN + " (name, branch_id, specialization, available) VALUES " +
                "('Kasun Perera', 1, 'Mobile', 1)");
        db.execSQL("INSERT INTO " + TABLE_TECHNICIAN + " (name, branch_id, specialization, available) VALUES " +
                "('Nimal Silva', 1, 'Computer', 1)");
        db.execSQL("INSERT INTO " + TABLE_TECHNICIAN + " (name, branch_id, specialization, available) VALUES " +
                "('Ishara Fernando', 2, 'Mobile', 1)");
        db.execSQL("INSERT INTO " + TABLE_TECHNICIAN + " (name, branch_id, specialization, available) VALUES " +
                "('Dilan Jayasuriya', 2, 'Computer', 1)");

        db.execSQL("INSERT INTO " + TABLE_SPARE_PART + " (name, branch_id, stock_qty, unit_price) VALUES " +
                "('Mobile Screen (Generic)', 1, 15, 6000)");
        db.execSQL("INSERT INTO " + TABLE_SPARE_PART + " (name, branch_id, stock_qty, unit_price) VALUES " +
                "('Laptop Keyboard', 1, 8, 4000)");
        db.execSQL("INSERT INTO " + TABLE_SPARE_PART + " (name, branch_id, stock_qty, unit_price) VALUES " +
                "('Mobile Screen (Generic)', 2, 10, 6000)");
        db.execSQL("INSERT INTO " + TABLE_SPARE_PART + " (name, branch_id, stock_qty, unit_price) VALUES " +
                "('Phone Battery', 2, 20, 2500)");
    }

    // ---------------- Password hashing ----------------

    /** Simple SHA-256 hash. Good enough for a coursework login system. */
    public static String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

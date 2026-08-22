package com.nibm.techfix.database;

import static com.nibm.techfix.database.DBHelper.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.nibm.techfix.models.DeviceCategory;
import com.nibm.techfix.models.RepairService;

import java.util.ArrayList;
import java.util.List;

/**
 * Real SQLite DAO for the Catalog domain.
 * SQL, ContentValues and Cursor mapping for this domain live here, not in DBHelper.
 */
public class CatalogDao {
    private final DBHelper helper;

    public CatalogDao(Context context) {
        this(DBHelper.getInstance(context));
    }

    CatalogDao(DBHelper helper) {
        this.helper = helper;
    }

    public List<DeviceCategory> getAllDeviceCategories() {
        List<DeviceCategory> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DEVICE_CATEGORY, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new DeviceCategory(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name"))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<RepairService> searchRepairServices(String query) {
        List<RepairService> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String like = "%" + query + "%";
        Cursor cursor = db.query(TABLE_REPAIR_SERVICE, null,
                "name LIKE ? OR description LIKE ?", new String[]{like, like}, null, null, "name ASC");
        if (cursor.moveToFirst()) {
            do {
                list.add(new RepairService(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("device_category_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("base_price"))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<RepairService> getServicesForCategory(int deviceCategoryId) {
        List<RepairService> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_REPAIR_SERVICE, null, "device_category_id = ?",
                new String[]{String.valueOf(deviceCategoryId)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new RepairService(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("device_category_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("base_price"))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    @Nullable
    public RepairService getServiceById(int id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_REPAIR_SERVICE, null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        RepairService service = null;
        if (cursor.moveToFirst()) {
            service = new RepairService(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("device_category_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("base_price"))
            );
        }
        cursor.close();
        return service;
    }

    public long insertDeviceCategory(String name) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        long id = db.insert(TABLE_DEVICE_CATEGORY, null, values);
        return id;
    }

    public void deleteDeviceCategory(int id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(TABLE_DEVICE_CATEGORY, "id = ?", new String[]{String.valueOf(id)});
    }

    public long insertRepairService(int deviceCategoryId, String name, String description, double basePrice) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("device_category_id", deviceCategoryId);
        values.put("name", name);
        values.put("description", description);
        values.put("base_price", basePrice);
        long id = db.insert(TABLE_REPAIR_SERVICE, null, values);
        return id;
    }

    public void updateRepairServicePrice(int serviceId, double newPrice) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("base_price", newPrice);
        db.update(TABLE_REPAIR_SERVICE, values, "id = ?", new String[]{String.valueOf(serviceId)});
    }

    public void deleteRepairService(int id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(TABLE_REPAIR_SERVICE, "id = ?", new String[]{String.valueOf(id)});
    }

    public List<RepairService> getAllRepairServices() {
        List<RepairService> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_REPAIR_SERVICE, null, null, null, null, null, "name ASC");
        if (cursor.moveToFirst()) {
            do {
                list.add(new RepairService(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("device_category_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("base_price"))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}

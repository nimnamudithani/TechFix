package com.nibm.techfix.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Content Provider deliverable.
 *
 * Exposes the spare_part table (device screens, keyboards, batteries etc.
 * held at each branch) through a real Android ContentProvider backed by
 * the same SQLite database DBHelper already manages, instead of every
 * screen talking to DBHelper directly. This is the standard Android
 * pattern for sharing structured data between components.
 *
 * Registered in AndroidManifest.xml under authority
 * "com.nibm.techfix.provider". See {@link SparePartContract} for the
 * public URIs/columns other code should use to talk to this provider.
 */
public class SparePartProvider extends ContentProvider {

    private static final int SPARE_PARTS = 1;
    private static final int SPARE_PART_ID = 2;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        URI_MATCHER.addURI(SparePartContract.AUTHORITY, SparePartContract.TABLE_NAME, SPARE_PARTS);
        URI_MATCHER.addURI(SparePartContract.AUTHORITY, SparePartContract.TABLE_NAME + "/#", SPARE_PART_ID);
    }

    private DBHelper dbHelper;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        if (context != null) {
            dbHelper = DBHelper.getInstance(context);
        }
        return dbHelper != null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                         @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int match = URI_MATCHER.match(uri);

        String finalSelection = selection;
        String[] finalArgs = selectionArgs;
        String finalSort = sortOrder;

        switch (match) {
            case SPARE_PARTS:
                if (finalSort == null) finalSort = SparePartContract.COLUMN_NAME + " ASC";
                break;
            case SPARE_PART_ID:
                finalSelection = SparePartContract.COLUMN_ID + " = ?";
                finalArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        Cursor cursor = db.query(SparePartContract.TABLE_NAME, projection,
                finalSelection, finalArgs, null, null, finalSort);
        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case SPARE_PARTS:
                return SparePartContract.CONTENT_TYPE;
            case SPARE_PART_ID:
                return SparePartContract.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (URI_MATCHER.match(uri) != SPARE_PARTS) {
            throw new IllegalArgumentException("Insert not supported for URI: " + uri);
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert(SparePartContract.TABLE_NAME, null, values);
        if (id <= 0) {
            return null;
        }
        Uri resultUri = SparePartContract.itemUri(id);
        notifyChange(uri);
        return resultUri;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                       @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = URI_MATCHER.match(uri);

        String finalSelection = selection;
        String[] finalArgs = selectionArgs;

        if (match == SPARE_PART_ID) {
            finalSelection = SparePartContract.COLUMN_ID + " = ?";
            finalArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
        } else if (match != SPARE_PARTS) {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        int rows = db.update(SparePartContract.TABLE_NAME, values, finalSelection, finalArgs);
        if (rows > 0) {
            notifyChange(uri);
        }
        return rows;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = URI_MATCHER.match(uri);

        String finalSelection = selection;
        String[] finalArgs = selectionArgs;

        if (match == SPARE_PART_ID) {
            finalSelection = SparePartContract.COLUMN_ID + " = ?";
            finalArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
        } else if (match != SPARE_PARTS) {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        int rows = db.delete(SparePartContract.TABLE_NAME, finalSelection, finalArgs);
        if (rows > 0) {
            notifyChange(uri);
        }
        return rows;
    }

    private void notifyChange(@NonNull Uri uri) {
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }
}

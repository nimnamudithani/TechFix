package com.nibm.techfix.database;

import android.net.Uri;

/**
 * Content Provider deliverable.
 *
 * Defines the public contract for accessing the spare_part table through a
 * real Android ContentProvider ({@link SparePartProvider}) rather than
 * talking to DBHelper/SQLite directly. Any component in (or outside) the
 * app can now read/write spare-part inventory the standard Android way:
 *
 *   getContentResolver().query(SparePartContract.CONTENT_URI, ...)
 *   getContentResolver().insert(SparePartContract.CONTENT_URI, values)
 *   getContentResolver().update(SparePartContract.itemUri(id), values, null, null)
 *   getContentResolver().delete(SparePartContract.itemUri(id), null, null)
 *
 * {@link com.nibm.techfix.activities.AdminSparePartsActivity} is wired up
 * to use this instead of calling DBHelper's spare-part methods directly.
 */
public final class SparePartContract {

    private SparePartContract() { }

    public static final String AUTHORITY = "com.nibm.techfix.provider";

    /** Table name, shared with DBHelper.TABLE_SPARE_PART. */
    public static final String TABLE_NAME = DBHelper.TABLE_SPARE_PART;

    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

    // MIME types returned by SparePartProvider#getType
    public static final String CONTENT_TYPE =
            "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + TABLE_NAME;
    public static final String CONTENT_ITEM_TYPE =
            "vnd.android.cursor.item/vnd." + AUTHORITY + "." + TABLE_NAME;

    // Columns - match the spare_part table schema in DBHelper exactly
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_BRANCH_ID = "branch_id";
    public static final String COLUMN_STOCK_QTY = "stock_qty";
    public static final String COLUMN_UNIT_PRICE = "unit_price";

    /** Uri for a single spare part row, e.g. content://.../spare_part/7 */
    public static Uri itemUri(long id) {
        return Uri.withAppendedPath(CONTENT_URI, String.valueOf(id));
    }
}

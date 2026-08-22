package com.nibm.techfix.activities;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.techfix.R;
import com.nibm.techfix.adapters.AdminSparePartAdapter;
import com.nibm.techfix.database.BranchDao;
import com.nibm.techfix.database.SparePartContract;
import com.nibm.techfix.models.Branch;
import com.nibm.techfix.models.SparePart;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages spare-part inventory for staff.
 *
 * Content Provider deliverable: all reads/writes to spare-part data go
 * through the ContentResolver -> SparePartProvider -> SQLite, instead of
 * calling SQLite directly from the screen. Spare-part CRUD uses the ContentProvider,
 * while branch lookup uses BranchDao.
 */
public class AdminSparePartsActivity extends AppCompatActivity {

    private BranchDao branchDao;
    private RecyclerView rvSpareParts;
    private List<Branch> branches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_spare_parts);

        branchDao = new BranchDao(this);
        rvSpareParts = findViewById(R.id.rvSpareParts);
        rvSpareParts.setLayoutManager(new LinearLayoutManager(this));

        Button btnAddSparePart = findViewById(R.id.btnAddSparePart);
        btnAddSparePart.setOnClickListener(v -> showAddDialog());

        refreshList();
    }

    /** Reads every spare part via the ContentProvider rather than SQLite directly. */
    private List<SparePart> loadSparePartsFromProvider() {
        List<SparePart> list = new ArrayList<>();
        try (Cursor cursor = getContentResolver().query(
                SparePartContract.CONTENT_URI, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(new SparePart(
                            cursor.getInt(cursor.getColumnIndexOrThrow(SparePartContract.COLUMN_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(SparePartContract.COLUMN_NAME)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(SparePartContract.COLUMN_BRANCH_ID)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(SparePartContract.COLUMN_STOCK_QTY)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(SparePartContract.COLUMN_UNIT_PRICE))
                    ));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    private void refreshList() {
        branches = branchDao.getAllBranches();
        AdminSparePartAdapter adapter = new AdminSparePartAdapter(
                loadSparePartsFromProvider(), this, new AdminSparePartAdapter.Listener() {
                    @Override
                    public void onEditStock(SparePart part) {
                        showEditStockDialog(part);
                    }

                    @Override
                    public void onDelete(SparePart part) {
                        getContentResolver().delete(SparePartContract.itemUri(part.getId()), null, null);
                        refreshList();
                    }
                });
        rvSpareParts.setAdapter(adapter);
    }

    private void showAddDialog() {
        if (branches.isEmpty()) {
            Toast.makeText(this, "No branches available", Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 0);

        EditText etName = new EditText(this);
        etName.setHint("Part name (e.g. Mobile Screen)");

        Spinner spinnerBranch = new Spinner(this);
        List<String> branchNames = new ArrayList<>();
        for (Branch b : branches) branchNames.add(b.getName());
        spinnerBranch.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, branchNames));

        EditText etStock = new EditText(this);
        etStock.setHint("Stock quantity");
        etStock.setInputType(InputType.TYPE_CLASS_NUMBER);

        EditText etPrice = new EditText(this);
        etPrice.setHint("Unit price (Rs.)");
        etPrice.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        layout.addView(etName);
        layout.addView(spinnerBranch);
        layout.addView(etStock);
        layout.addView(etPrice);

        new AlertDialog.Builder(this)
                .setTitle("Add Spare Part")
                .setView(layout)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String stockStr = etStock.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();

                    if (name.isEmpty() || stockStr.isEmpty() || priceStr.isEmpty()) {
                        Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int branchId = branches.get(spinnerBranch.getSelectedItemPosition()).getId();

                    ContentValues values = new ContentValues();
                    values.put(SparePartContract.COLUMN_NAME, name);
                    values.put(SparePartContract.COLUMN_BRANCH_ID, branchId);
                    values.put(SparePartContract.COLUMN_STOCK_QTY, Integer.parseInt(stockStr));
                    values.put(SparePartContract.COLUMN_UNIT_PRICE, Double.parseDouble(priceStr));
                    getContentResolver().insert(SparePartContract.CONTENT_URI, values);

                    Toast.makeText(this, "Spare part added", Toast.LENGTH_SHORT).show();
                    refreshList();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditStockDialog(SparePart part) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(part.getStockQty()));

        new AlertDialog.Builder(this)
                .setTitle("Edit Stock: " + part.getName())
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String stockStr = input.getText().toString().trim();
                    if (!stockStr.isEmpty()) {
                        ContentValues values = new ContentValues();
                        values.put(SparePartContract.COLUMN_STOCK_QTY, Integer.parseInt(stockStr));
                        getContentResolver().update(
                                SparePartContract.itemUri(part.getId()), values, null, null);
                        refreshList();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

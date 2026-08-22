package com.nibm.techfix.activities;

import android.app.AlertDialog;
import android.os.Bundle;
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
import com.nibm.techfix.adapters.AdminServiceAdapter;
import com.nibm.techfix.database.CatalogDao;
import com.nibm.techfix.models.DeviceCategory;
import com.nibm.techfix.models.RepairService;

import java.util.ArrayList;
import java.util.List;

public class AdminServicesActivity extends AppCompatActivity {

    private CatalogDao catalogDao;
    private RecyclerView rvServices;
    private List<DeviceCategory> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_services);

        catalogDao = new CatalogDao(this);
        rvServices = findViewById(R.id.rvServices);
        rvServices.setLayoutManager(new LinearLayoutManager(this));

        Button btnAddCategory = findViewById(R.id.btnAddCategory);
        Button btnAddService = findViewById(R.id.btnAddService);

        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
        btnAddService.setOnClickListener(v -> showAddServiceDialog());

        refreshList();
    }

    private void refreshList() {
        categories = catalogDao.getAllDeviceCategories();
        List<RepairService> services = catalogDao.getAllRepairServices();

        AdminServiceAdapter adapter = new AdminServiceAdapter(services, new AdminServiceAdapter.Listener() {
            @Override
            public void onEditPrice(RepairService service) {
                showEditPriceDialog(service);
            }

            @Override
            public void onDelete(RepairService service) {
                catalogDao.deleteRepairService(service.getId());
                refreshList();
            }
        });
        rvServices.setAdapter(adapter);
    }

    private void showAddCategoryDialog() {
        EditText input = new EditText(this);
        input.setHint("Category name (e.g. Smartwatch)");

        new AlertDialog.Builder(this)
                .setTitle("Add Device Category")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        catalogDao.insertDeviceCategory(name);
                        Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show();
                        refreshList();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddServiceDialog() {
        if (categories.isEmpty()) {
            Toast.makeText(this, "Add a device category first", Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 0);

        Spinner spinnerCategory = new Spinner(this);
        List<String> categoryNames = new ArrayList<>();
        for (DeviceCategory c : categories) categoryNames.add(c.getName());
        spinnerCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryNames));

        EditText etName = new EditText(this);
        etName.setHint("Service name (e.g. Camera Repair)");

        EditText etDescription = new EditText(this);
        etDescription.setHint("Short description");

        EditText etPrice = new EditText(this);
        etPrice.setHint("Base price (Rs.)");
        etPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        layout.addView(spinnerCategory);
        layout.addView(etName);
        layout.addView(etDescription);
        layout.addView(etPrice);

        new AlertDialog.Builder(this)
                .setTitle("Add Repair Service")
                .setView(layout)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();

                    if (name.isEmpty() || priceStr.isEmpty()) {
                        Toast.makeText(this, "Name and price are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int categoryId = categories.get(spinnerCategory.getSelectedItemPosition()).getId();
                    double price = Double.parseDouble(priceStr);
                    catalogDao.insertRepairService(categoryId, name, description, price);
                    Toast.makeText(this, "Service added", Toast.LENGTH_SHORT).show();
                    refreshList();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditPriceDialog(RepairService service) {
        EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.valueOf((int) service.getBasePrice()));

        new AlertDialog.Builder(this)
                .setTitle("Edit Price: " + service.getName())
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String priceStr = input.getText().toString().trim();
                    if (!priceStr.isEmpty()) {
                        catalogDao.updateRepairServicePrice(service.getId(), Double.parseDouble(priceStr));
                        refreshList();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

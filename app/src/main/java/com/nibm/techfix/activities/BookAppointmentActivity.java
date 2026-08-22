package com.nibm.techfix.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.nibm.techfix.R;
import com.nibm.techfix.database.CatalogDao;
import com.nibm.techfix.database.BranchDao;
import com.nibm.techfix.database.TechnicianDao;
import com.nibm.techfix.database.SparePartDao;
import com.nibm.techfix.database.AppointmentDao;
import com.nibm.techfix.models.Branch;
import com.nibm.techfix.models.DeviceCategory;
import com.nibm.techfix.models.RepairAppointment;
import com.nibm.techfix.models.RepairService;
import com.nibm.techfix.models.Technician;
import com.nibm.techfix.utils.LocationUtils;
import com.nibm.techfix.utils.RepairInfoUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookAppointmentActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 100;

    private Spinner spinnerDeviceCategory, spinnerRepairService;
    private TextView tvSelectedBranch, tvAssignedTechnician, tvRepairEstimate, tvEstimatedCompletion;

    private CatalogDao catalogDao;
    private BranchDao branchDao;
    private TechnicianDao technicianDao;
    private SparePartDao sparePartDao;
    private AppointmentDao appointmentDao;
    private FusedLocationProviderClient fusedLocationClient;

    private List<DeviceCategory> categories = new ArrayList<>();
    private List<RepairService> services = new ArrayList<>();

    private Branch selectedBranch;
    private Technician selectedTechnician;
    private int userId;
    private int preselectedCategoryId = -1;
    private int preselectedServiceId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        userId = getIntent().getIntExtra("userId", -1);
        preselectedCategoryId = getIntent().getIntExtra("preselectedCategoryId", -1);
        preselectedServiceId = getIntent().getIntExtra("preselectedServiceId", -1);
        catalogDao = new CatalogDao(this);
        branchDao = new BranchDao(this);
        technicianDao = new TechnicianDao(this);
        sparePartDao = new SparePartDao(this);
        appointmentDao = new AppointmentDao(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        spinnerDeviceCategory = findViewById(R.id.spinnerDeviceCategory);
        spinnerRepairService = findViewById(R.id.spinnerRepairService);
        tvSelectedBranch = findViewById(R.id.tvSelectedBranch);
        tvAssignedTechnician = findViewById(R.id.tvAssignedTechnician);
        tvRepairEstimate = findViewById(R.id.tvRepairEstimate);
        tvEstimatedCompletion = findViewById(R.id.tvEstimatedCompletion);
        Button btnFindBranch = findViewById(R.id.btnFindBranch);
        Button btnSubmit = findViewById(R.id.btnSubmit);

        loadCategories();

        spinnerDeviceCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                loadServicesForSelectedCategory();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        spinnerRepairService.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                updateEstimate();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        btnFindBranch.setOnClickListener(v -> findNearestBranch());
        btnSubmit.setOnClickListener(v -> showPreRepairChecklist());
    }

    private void loadCategories() {
        categories = catalogDao.getAllDeviceCategories();
        List<String> names = new ArrayList<>();
        for (DeviceCategory c : categories) names.add(c.getName());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
        spinnerDeviceCategory.setAdapter(adapter);

        if (preselectedCategoryId != -1) {
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getId() == preselectedCategoryId) {
                    spinnerDeviceCategory.setSelection(i);
                    break;
                }
            }
        }

        if (!categories.isEmpty()) loadServicesForSelectedCategory();
    }

    private void loadServicesForSelectedCategory() {
        int position = spinnerDeviceCategory.getSelectedItemPosition();
        if (position < 0 || position >= categories.size()) return;

        int categoryId = categories.get(position).getId();
        services = catalogDao.getServicesForCategory(categoryId);

        List<String> names = new ArrayList<>();
        for (RepairService s : services) names.add(s.getName() + " - Rs. " + (int) s.getBasePrice());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
        spinnerRepairService.setAdapter(adapter);

        if (preselectedServiceId != -1) {
            for (int i = 0; i < services.size(); i++) {
                if (services.get(i).getId() == preselectedServiceId) {
                    spinnerRepairService.setSelection(i);
                    preselectedServiceId = -1; // only apply once
                    break;
                }
            }
        }
    }

    private void updateEstimate() {
        int position = spinnerRepairService.getSelectedItemPosition();
        if (position < 0 || position >= services.size()) return;
        RepairService service = services.get(position);
        tvRepairEstimate.setText("💰 Estimated repair cost: Rs. " + (int) service.getBasePrice() + "*");
        tvEstimatedCompletion.setText("⏱ Estimated completion: " + RepairInfoUtils.estimatedTime(service.getName()));
    }

    /** Maps a device category name to the technician specialization field used in the DB. */
    private String specializationFor(String categoryName) {
        if (categoryName.equals("Mobile Phone") || categoryName.equals("Tablet")) return "Mobile";
        return "Computer"; // Laptop, Desktop
    }

    private void findNearestBranch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }
        resolveNearestBranch();
    }

    private void resolveNearestBranch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            List<Branch> branches = branchDao.getAllBranches();
            if (branches.isEmpty()) {
                Toast.makeText(this, "No branches available", Toast.LENGTH_SHORT).show();
                return;
            }

            List<Branch> ranked;
            if (location != null) {
                ranked = LocationUtils.sortBranchesByDistance(location.getLatitude(), location.getLongitude(), branches);
            } else {
                ranked = branches; // no location fix - just try them in DB order
                Toast.makeText(this, "Couldn't get your location, trying branches in default order", Toast.LENGTH_SHORT).show();
            }

            assignBranchAndTechnician(ranked);
        });
    }

    /**
     * Walks branches nearest-first, picking the first one that has BOTH an
     * available technician for this device type AND the spare part required by the selected service in
     * stock - matching the brief's "nearest branch where technicians and
     * required spare parts are available" requirement.
     */
    private void assignBranchAndTechnician(List<Branch> rankedBranches) {
        int categoryPosition = spinnerDeviceCategory.getSelectedItemPosition();
        if (categoryPosition < 0 || categoryPosition >= categories.size()) return;

        String categoryName = categories.get(categoryPosition).getName();
        String specialization = specializationFor(categoryName);

        for (Branch branch : rankedBranches) {
            List<Technician> available = technicianDao.getAvailableTechnicians(branch.getId(), specialization);
            int servicePosition = spinnerRepairService.getSelectedItemPosition();
            String serviceName = (servicePosition >= 0 && servicePosition < services.size())
                    ? services.get(servicePosition).getName() : "";
            boolean hasParts = sparePartDao.hasRequiredSparePartInStock(branch.getId(), serviceName);

            if (!available.isEmpty() && hasParts) {
                selectedBranch = branch;
                selectedTechnician = available.get(0);
                tvSelectedBranch.setText("Branch: " + branch.getName());
                tvAssignedTechnician.setText("Technician: " + selectedTechnician.getName() + " (parts in stock)");
                return;
            }
        }

        // No branch satisfied both conditions
        selectedBranch = rankedBranches.get(0);
        selectedTechnician = null;
        tvSelectedBranch.setText("Branch: " + selectedBranch.getName() + " (nearest, but check availability)");
        tvAssignedTechnician.setText("No branch currently has both an available technician and spare parts for this device type");
    }


    private void showPreRepairChecklist() {
        if (selectedBranch == null) {
            Toast.makeText(this, "Please find a branch first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedTechnician == null) {
            Toast.makeText(this, "No technician available - try again later", Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("📋 Pre-Repair Checklist")
                .setMessage("Before handing over your device:\\n\\n" +
                        "✓ Back up important files and photos\\n" +
                        "✓ Remove SIM/SD card when possible\\n" +
                        "✓ Disable device locks or provide access if required\\n" +
                        "✓ Bring the charger for power-related laptop issues\\n" +
                        "✓ Remove unnecessary accessories\\n\\n" +
                        "Continue only when you have checked these items.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("I Understand - Book", (dialog, which) -> submitAppointment())
                .show();
    }

    private void submitAppointment() {
        if (selectedBranch == null) {
            Toast.makeText(this, "Please find a branch first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedTechnician == null) {
            Toast.makeText(this, "No technician available - try again later", Toast.LENGTH_SHORT).show();
            return;
        }

        int categoryPosition = spinnerDeviceCategory.getSelectedItemPosition();
        int servicePosition = spinnerRepairService.getSelectedItemPosition();
        if (categoryPosition < 0 || servicePosition < 0 || servicePosition >= services.size()) {
            Toast.makeText(this, "Please select a device category and service", Toast.LENGTH_SHORT).show();
            return;
        }

        int categoryId = categories.get(categoryPosition).getId();
        int serviceId = services.get(servicePosition).getId();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new java.util.Date());

        RepairAppointment appointment = new RepairAppointment(
                0, userId, selectedBranch.getId(), selectedTechnician.getId(),
                categoryId, serviceId, RepairAppointment.STATUS_ASSIGNED, now);

        long id = appointmentDao.createAppointment(appointment);
        if (id != -1) {
            Toast.makeText(this, "Appointment booked!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED
                || (grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED))) {
            resolveNearestBranch();
        } else {
            Toast.makeText(this, "Location permission is needed to find the nearest branch", Toast.LENGTH_SHORT).show();
        }
    }
}

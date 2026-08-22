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
import com.nibm.techfix.adapters.AdminTechnicianAdapter;
import com.nibm.techfix.database.BranchDao;
import com.nibm.techfix.database.TechnicianDao;
import com.nibm.techfix.models.Branch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminTechniciansActivity extends AppCompatActivity {

    private BranchDao branchDao;
    private TechnicianDao technicianDao;
    private RecyclerView rvTechnicians;
    private List<Branch> branches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_technicians);

        branchDao = new BranchDao(this);
        technicianDao = new TechnicianDao(this);
        rvTechnicians = findViewById(R.id.rvTechnicians);
        rvTechnicians.setLayoutManager(new LinearLayoutManager(this));

        Button btnAddTechnician = findViewById(R.id.btnAddTechnician);
        btnAddTechnician.setOnClickListener(v -> showAddDialog());

        refreshList();
    }

    private void refreshList() {
        branches = branchDao.getAllBranches();
        AdminTechnicianAdapter adapter = new AdminTechnicianAdapter(
                technicianDao.getAllTechnicians(), this, technician -> {
                    technicianDao.deleteTechnician(technician.getId());
                    refreshList();
                });
        rvTechnicians.setAdapter(adapter);
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
        etName.setHint("Technician name");

        Spinner spinnerBranch = new Spinner(this);
        List<String> branchNames = new ArrayList<>();
        for (Branch b : branches) branchNames.add(b.getName());
        spinnerBranch.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, branchNames));

        Spinner spinnerSpecialization = new Spinner(this);
        List<String> specializations = Arrays.asList("Mobile", "Computer");
        spinnerSpecialization.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, specializations));

        layout.addView(etName);
        layout.addView(spinnerBranch);
        layout.addView(spinnerSpecialization);

        new AlertDialog.Builder(this)
                .setTitle("Add Technician")
                .setView(layout)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int branchId = branches.get(spinnerBranch.getSelectedItemPosition()).getId();
                    String specialization = specializations.get(spinnerSpecialization.getSelectedItemPosition());
                    technicianDao.insertTechnician(name, branchId, specialization);
                    Toast.makeText(this, "Technician added", Toast.LENGTH_SHORT).show();
                    refreshList();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

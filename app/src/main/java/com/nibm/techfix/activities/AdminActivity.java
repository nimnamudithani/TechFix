package com.nibm.techfix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.nibm.techfix.R;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        View btnManageAppointments = findViewById(R.id.btnManageAppointments);
        View btnManageServices = findViewById(R.id.btnManageServices);
        View btnManageTechnicians = findViewById(R.id.btnManageTechnicians);
        View btnManageSpareParts = findViewById(R.id.btnManageSpareParts);
        View btnManageSampleImages = findViewById(R.id.btnManageSampleImages);

        btnManageAppointments.setOnClickListener(v ->
                startActivity(new Intent(this, AdminAppointmentsActivity.class)));
        btnManageServices.setOnClickListener(v ->
                startActivity(new Intent(this, AdminServicesActivity.class)));
        btnManageTechnicians.setOnClickListener(v ->
                startActivity(new Intent(this, AdminTechniciansActivity.class)));
        btnManageSpareParts.setOnClickListener(v ->
                startActivity(new Intent(this, AdminSparePartsActivity.class)));
        btnManageSampleImages.setOnClickListener(v ->
                startActivity(new Intent(this, AdminSampleImagesActivity.class)));
    }
}

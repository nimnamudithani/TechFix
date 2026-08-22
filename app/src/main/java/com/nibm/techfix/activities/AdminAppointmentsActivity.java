package com.nibm.techfix.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.techfix.R;
import com.nibm.techfix.adapters.AppointmentAdapter;
import com.nibm.techfix.database.AppointmentDao;

/**
 * Staff view of every appointment across both branches (not filtered by
 * user, unlike AppointmentHistoryActivity). Tapping one opens the same
 * AppointmentDetailActivity customers use, which already has "Mark as
 * Completed" / "Mark as Paid" - reused here as the staff status-update flow.
 */
public class AdminAppointmentsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_appointments);

        AppointmentDao appointmentDao = new AppointmentDao(this);
        RecyclerView rv = findViewById(R.id.rvAppointments);
        rv.setLayoutManager(new LinearLayoutManager(this));

        AppointmentAdapter adapter = new AppointmentAdapter(appointmentDao.getAllAppointments(), this, appointment -> {
            Intent intent = new Intent(AdminAppointmentsActivity.this, AppointmentDetailActivity.class);
            intent.putExtra("appointmentId", appointment.getId());
            intent.putExtra("isStaffView", true); // staff get the full edit controls
            startActivity(intent);
        });
        rv.setAdapter(adapter);
    }
}

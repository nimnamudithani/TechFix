package com.nibm.techfix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.techfix.R;
import com.nibm.techfix.adapters.AppointmentAdapter;
import com.nibm.techfix.database.AppointmentDao;
import com.nibm.techfix.models.RepairAppointment;

import java.util.List;

/**
 * Shows every appointment for the logged-in user (both in-progress and
 * completed), so this single screen covers both "track repair status" and
 * "view repair history" from the brief. Pass showOnlyCompleted=true via the
 * intent if you want a strictly-history-only view instead.
 */
public class AppointmentHistoryActivity extends AppCompatActivity {

    private AppointmentDao appointmentDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_history);

        appointmentDao = new AppointmentDao(this);
        int userId = getIntent().getIntExtra("userId", -1);
        boolean onlyCompleted = getIntent().getBooleanExtra("onlyCompleted", false);
        String mode = getIntent().getStringExtra("mode");

        TextView tvTitle = findViewById(R.id.tvScreenTitle);
        if ("receipt".equals(mode)) tvTitle.setText("🧾 Digital Receipts");
        else if ("warrantyReview".equals(mode)) tvTitle.setText("🛡 Warranty & ⭐ Reviews");
        else tvTitle.setText(onlyCompleted ? "📜 Repair History" : "My Appointments");

        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        List<RepairAppointment> appointments;
        if ("receipt".equals(mode) || "warrantyReview".equals(mode) || onlyCompleted) {
            appointments = appointmentDao.getCompletedAppointmentsForUser(userId);
        } else {
            appointments = appointmentDao.getAppointmentsForUser(userId);
        }

        AppointmentAdapter adapter = new AppointmentAdapter(appointments, this, appointment -> {
            Intent intent;
            if ("receipt".equals(mode) && RepairAppointment.STATUS_PAID.equals(appointment.getStatus())) {
                intent = new Intent(AppointmentHistoryActivity.this, ReceiptActivity.class);
                intent.putExtra("appointmentId", appointment.getId());
            } else {
                intent = new Intent(AppointmentHistoryActivity.this, AppointmentDetailActivity.class);
                intent.putExtra("appointmentId", appointment.getId());
                intent.putExtra("isStaffView", false);
            }
            startActivity(intent);
        });
        rvHistory.setAdapter(adapter);
    }
}

package com.nibm.techfix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.techfix.R;
import com.nibm.techfix.adapters.AppointmentAdapter;
import com.nibm.techfix.database.AppointmentDao;
import com.nibm.techfix.models.RepairAppointment;
import com.nibm.techfix.utils.BottomNavHelper;

import java.util.List;

public class ActivitiesActivity extends AppCompatActivity {

    private AppointmentDao appointmentDao;
    private RecyclerView rvActivities;
    private TextView tvEmpty;
    private int userId;
    private boolean isAdmin;
    private boolean showingOngoing = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities);

        appointmentDao = new AppointmentDao(this);
        userId = getIntent().getIntExtra("userId", -1);
        isAdmin = getIntent().getBooleanExtra("isAdmin", false);

        rvActivities = findViewById(R.id.rvActivities);
        rvActivities.setLayoutManager(new LinearLayoutManager(this));
        tvEmpty = findViewById(R.id.tvEmpty);

        findViewById(R.id.tabOngoing).setOnClickListener(v -> selectTab(true));
        findViewById(R.id.tabCompleted).setOnClickListener(v -> selectTab(false));

        loadList();
        BottomNavHelper.setup(this, userId, isAdmin, BottomNavHelper.Tab.ACTIVITIES);
    }

    private void selectTab(boolean ongoing) {
        if (showingOngoing == ongoing) return;
        showingOngoing = ongoing;

        int active = ContextCompat.getColor(this, R.color.primary);
        int inactive = ContextCompat.getColor(this, R.color.text_secondary);

        TextView tvOngoing = findViewById(R.id.tvTabOngoing);
        TextView tvCompleted = findViewById(R.id.tvTabCompleted);
        android.view.View indicatorOngoing = findViewById(R.id.indicatorOngoing);
        android.view.View indicatorCompleted = findViewById(R.id.indicatorCompleted);

        tvOngoing.setTextColor(ongoing ? active : inactive);
        tvOngoing.setTypeface(null, ongoing ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        indicatorOngoing.setBackgroundColor(ongoing ? active : ContextCompat.getColor(this, android.R.color.transparent));

        tvCompleted.setTextColor(!ongoing ? active : inactive);
        tvCompleted.setTypeface(null, !ongoing ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        indicatorCompleted.setBackgroundColor(!ongoing ? active : ContextCompat.getColor(this, android.R.color.transparent));

        loadList();
    }

    private void loadList() {
        List<RepairAppointment> appointments;
        if (isAdmin) {
            appointments = showingOngoing ? appointmentDao.getAllOngoingAppointments() : appointmentDao.getAllFinishedAppointments();
        } else {
            appointments = showingOngoing ? appointmentDao.getOngoingAppointmentsForUser(userId) : appointmentDao.getFinishedAppointmentsForUser(userId);
        }

        tvEmpty.setVisibility(appointments.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
        rvActivities.setVisibility(appointments.isEmpty() ? android.view.View.INVISIBLE : android.view.View.VISIBLE);

        AppointmentAdapter adapter = new AppointmentAdapter(appointments, this, appointment -> {
            Intent intent = new Intent(ActivitiesActivity.this, AppointmentDetailActivity.class);
            intent.putExtra("appointmentId", appointment.getId());
            intent.putExtra("isStaffView", isAdmin);
            startActivity(intent);
        });
        rvActivities.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadList(); // pick up any status changes made elsewhere (e.g. staff marking paid)
    }
}

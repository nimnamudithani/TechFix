package com.nibm.techfix.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.techfix.R;
import com.nibm.techfix.adapters.NotificationAdapter;
import com.nibm.techfix.database.NotificationDao;
import com.nibm.techfix.utils.BottomNavHelper;

import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private NotificationDao notificationDao;
    private int userId;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        notificationDao = new NotificationDao(this);
        userId = getIntent().getIntExtra("userId", -1);
        isAdmin = getIntent().getBooleanExtra("isAdmin", false);

        RecyclerView rv = findViewById(R.id.rvNotifications);
        rv.setLayoutManager(new LinearLayoutManager(this));
        TextView tvEmpty = findViewById(R.id.tvEmpty);

        List<String[]> notifications = notificationDao.getNotificationsForUser(userId);
        tvEmpty.setVisibility(notifications.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
        rv.setVisibility(notifications.isEmpty() ? android.view.View.INVISIBLE : android.view.View.VISIBLE);
        rv.setAdapter(new NotificationAdapter(notifications));

        // Mark everything read now that the user has seen this screen
        notificationDao.markAllNotificationsRead(userId);

        BottomNavHelper.setup(this, userId, isAdmin, BottomNavHelper.Tab.NOTIFICATIONS);
    }
}

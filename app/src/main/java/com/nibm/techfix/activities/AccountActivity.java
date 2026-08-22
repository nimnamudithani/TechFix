package com.nibm.techfix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.nibm.techfix.R;
import com.nibm.techfix.database.UserDao;
import com.nibm.techfix.models.User;
import com.nibm.techfix.utils.BottomNavHelper;

public class AccountActivity extends AppCompatActivity {

    private int userId;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        UserDao userDao = new UserDao(this);
        userId = getIntent().getIntExtra("userId", -1);
        isAdmin = getIntent().getBooleanExtra("isAdmin", false);

        User user = userDao.getUserById(userId);

        TextView tvAvatarInitials = findViewById(R.id.tvAvatarInitials);
        TextView tvName = findViewById(R.id.tvName);
        TextView tvEmail = findViewById(R.id.tvEmail);
        TextView tvRoleBadge = findViewById(R.id.tvRoleBadge);

        if (user != null) {
            tvName.setText(user.getName());
            tvEmail.setText(user.getEmail());
            tvAvatarInitials.setText(initialsFor(user.getName()));
        }
        tvRoleBadge.setText(isAdmin ? "STAFF" : "CUSTOMER");

        if (isAdmin) {
            android.view.View menuManage = findViewById(R.id.menuManage);
            menuManage.setVisibility(android.view.View.VISIBLE);
            menuManage.setOnClickListener(v ->
                    startActivity(new Intent(AccountActivity.this, AdminActivity.class)));

            // Customer-only account services are hidden for staff.
            findViewById(R.id.menuRepairHistory).setVisibility(android.view.View.GONE);
            findViewById(R.id.menuReceipts).setVisibility(android.view.View.GONE);
            findViewById(R.id.menuWarrantyReviews).setVisibility(android.view.View.GONE);
            findViewById(R.id.menuHelp).setVisibility(android.view.View.GONE);
        }

        findViewById(R.id.menuRepairHistory).setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, AppointmentHistoryActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("onlyCompleted", true);
            startActivity(intent);
        });

        findViewById(R.id.menuReceipts).setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, AppointmentHistoryActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("mode", "receipt");
            startActivity(intent);
        });

        findViewById(R.id.menuWarrantyReviews).setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, AppointmentHistoryActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("mode", "warrantyReview");
            startActivity(intent);
        });

        findViewById(R.id.menuChangePassword).setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, ChangePasswordActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        findViewById(R.id.menuHelp).setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, FaqActivity.class)));

        findViewById(R.id.menuLogout).setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        BottomNavHelper.setup(this, userId, isAdmin, BottomNavHelper.Tab.ACCOUNT);
    }

    private String initialsFor(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        String initials = parts[0].substring(0, 1).toUpperCase();
        if (parts.length > 1) initials += parts[parts.length - 1].substring(0, 1).toUpperCase();
        return initials;
    }
}

package com.nibm.techfix.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nibm.techfix.R;
import com.nibm.techfix.database.UserDao;

public class ChangePasswordActivity extends AppCompatActivity {

    private UserDao userDao;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        userDao = new UserDao(this);
        userId = getIntent().getIntExtra("userId", -1);

        EditText etCurrentPassword = findViewById(R.id.etCurrentPassword);
        EditText etNewPassword = findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnSavePassword = findViewById(R.id.btnSavePassword);

        btnSavePassword.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPassword.length() < 4) {
                Toast.makeText(this, "New password must be at least 4 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "New passwords don't match", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = userDao.changePassword(userId, currentPassword, newPassword);
            if (success) {
                Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

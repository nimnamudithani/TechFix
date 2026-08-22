package com.nibm.techfix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.techfix.R;
import com.nibm.techfix.adapters.BranchAdapter;
import com.nibm.techfix.database.UserDao;
import com.nibm.techfix.database.BranchDao;
import com.nibm.techfix.models.Branch;
import com.nibm.techfix.utils.BottomNavHelper;

import java.util.Calendar;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private UserDao userDao;
    private BranchDao branchDao;
    private int userId;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        userId = getIntent().getIntExtra("userId", -1);
        isAdmin = getIntent().getBooleanExtra("isAdmin", false);
        userDao = new UserDao(this);
        branchDao = new BranchDao(this);

        setupGreeting();
        setupQuickActions();
        setupBranches();

        BottomNavHelper.setup(this, userId, isAdmin, BottomNavHelper.Tab.HOME);
    }

    private void setupGreeting() {
        TextView tvGreeting = findViewById(R.id.tvGreeting);
        TextView tvRoleBadge = findViewById(R.id.tvRoleBadge);

        com.nibm.techfix.models.User user = userDao.getUserById(userId);
        String userName = user != null ? user.getName() : null;

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String timeOfDay = hour < 12 ? "Morning" : (hour < 17 ? "Afternoon" : "Evening");
        String name = (userName != null && !userName.isEmpty()) ? userName.split(" ")[0] : "there";

        tvGreeting.setText(getString(R.string.greeting_format, name, timeOfDay));

        // Customer home should stay clean: no CUSTOMER badge.
        // Staff still keeps its STAFF badge when this shared home screen is opened.
        if (isAdmin) {
            tvRoleBadge.setText(R.string.role_staff);
            tvRoleBadge.setVisibility(View.VISIBLE);
        } else {
            tvRoleBadge.setVisibility(View.GONE);
        }
    }

    private void setupQuickActions() {
        // First tile changes meaning based on role: Book a Repair for customers, Manage TechFix for staff
        android.widget.LinearLayout tileBookOrManage = findViewById(R.id.tileBookOrManage);
        ImageView ivTile = findViewById(R.id.ivTileBookOrManage);
        TextView tvTile = findViewById(R.id.tvTileBookOrManage);

        if (isAdmin) {
            ivTile.setImageResource(R.drawable.ic_action_manage);
            tvTile.setText(R.string.manage_techfix);
            tileBookOrManage.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, AdminActivity.class)));
        } else {
            ivTile.setImageResource(R.drawable.ic_action_book);
            tvTile.setText(R.string.book_a_repair);
            tileBookOrManage.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, BookAppointmentActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            });
        }

        findViewById(R.id.searchBar).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SearchServicesActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        findViewById(R.id.tileGallery).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SampleImagesActivity.class)));

        findViewById(R.id.tileMap).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, BranchMapActivity.class)));
    }

    private void setupBranches() {
        RecyclerView rvBranches = findViewById(R.id.rvBranches);
        rvBranches.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<Branch> branches = branchDao.getAllBranches();
        BranchAdapter adapter = new BranchAdapter(branches, branch ->
                android.widget.Toast.makeText(HomeActivity.this,
                        branch.getName() + " - " + branch.getAddress(), android.widget.Toast.LENGTH_SHORT).show());
        rvBranches.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the notification badge in case something changed while away
        BottomNavHelper.setup(this, userId, isAdmin, BottomNavHelper.Tab.HOME);
    }
}

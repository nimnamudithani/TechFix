package com.nibm.techfix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.techfix.R;
import com.nibm.techfix.adapters.RepairServiceAdapter;
import com.nibm.techfix.database.CatalogDao;
import com.nibm.techfix.models.RepairService;

import java.util.ArrayList;
import java.util.List;

public class SearchServicesActivity extends AppCompatActivity {

    private CatalogDao catalogDao;
    private RecyclerView rvResults;
    private TextView tvNoResults;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_services);

        catalogDao = new CatalogDao(this);
        userId = getIntent().getIntExtra("userId", -1);

        EditText etSearch = findViewById(R.id.etSearch);
        rvResults = findViewById(R.id.rvResults);
        tvNoResults = findViewById(R.id.tvNoResults);
        rvResults.setLayoutManager(new LinearLayoutManager(this));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                runSearch(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void runSearch(String query) {
        if (query.isEmpty()) {
            showResults(new ArrayList<>());
            tvNoResults.setText("Start typing to search");
            tvNoResults.setVisibility(android.view.View.VISIBLE);
            return;
        }

        List<RepairService> results = catalogDao.searchRepairServices(query);
        if (results.isEmpty()) {
            tvNoResults.setText("No matching services found");
            tvNoResults.setVisibility(android.view.View.VISIBLE);
        } else {
            tvNoResults.setVisibility(android.view.View.GONE);
        }
        showResults(results);
    }

    private void showResults(List<RepairService> results) {
        RepairServiceAdapter adapter = new RepairServiceAdapter(results, service -> {
            // Jump straight into booking with this service pre-selected
            Intent intent = new Intent(SearchServicesActivity.this, BookAppointmentActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("preselectedCategoryId", service.getDeviceCategoryId());
            intent.putExtra("preselectedServiceId", service.getId());
            startActivity(intent);
        });
        rvResults.setAdapter(adapter);
    }
}

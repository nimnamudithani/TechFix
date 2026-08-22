package com.nibm.techfix.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.techfix.R;
import com.nibm.techfix.adapters.SampleImageAdapter;
import com.nibm.techfix.database.SampleImageDao;

public class SampleImagesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_images);

        SampleImageDao sampleImageDao = new SampleImageDao(this);
        RecyclerView rv = findViewById(R.id.rvSampleImages);
        rv.setLayoutManager(new LinearLayoutManager(this));
        // null listener = read-only, no delete button shown to customers
        rv.setAdapter(new SampleImageAdapter(sampleImageDao.getAllSampleImages(), null));
    }
}

package com.michael.dormie.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.util.Log;

import com.michael.dormie.R;
import com.michael.dormie.adapter.PhotoAdapter;
import com.michael.dormie.model.Photo;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class PostCreationActivity extends AppCompatActivity {

    private ViewPager2 viewPager2;
    private CircleIndicator3 circleIndicator3;
    private PhotoAdapter photoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_creation);

        viewPager2 = findViewById(R.id.view_pager);
        circleIndicator3 = findViewById(R.id.circle_indicator);

        photoAdapter = new PhotoAdapter(this, getPhotos());

        Log.e("DEBUG", String.valueOf(viewPager2 == null));
        Log.e("DEBUG", String.valueOf(photoAdapter == null));

        viewPager2.setAdapter(photoAdapter);

        circleIndicator3.setViewPager(viewPager2);
        photoAdapter.registerAdapterDataObserver(circleIndicator3.getAdapterDataObserver());
    }

    private List<Photo> getPhotos() {
        List<Photo> photos = new ArrayList<>();
        photos.add(new Photo(R.drawable.sample));
        photos.add(new Photo(R.drawable.rmit_sample));
        return photos;
    }
}
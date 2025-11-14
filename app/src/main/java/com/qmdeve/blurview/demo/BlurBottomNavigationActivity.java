package com.qmdeve.blurview.demo;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.qmdeve.blurview.demo.adapter.ViewPagerAdapter;
import com.qmdeve.blurview.demo.fragment.ContentFragment;
import com.qmdeve.blurview.demo.util.Utils;
import com.qmdeve.blurview.widget.BlurBottomNavigationView;

public class BlurBottomNavigationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blur_navigation);

        Utils.transparentStatusBar(getWindow());
        Utils.transparentNavigationBar(getWindow());

        ViewPager viewPager = findViewById(R.id.viewpager);
        BlurBottomNavigationView bottomNavigationView = findViewById(R.id.bottomnav);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        for (int i = 1; i <= 4; i++) {
            ContentFragment fragment = ContentFragment.newInstance();
            adapter.addFragment(fragment);
        }
        viewPager.setAdapter(adapter);

        // Bind ViewPager or ViewPager2
        bottomNavigationView.bind(viewPager);
    }
}
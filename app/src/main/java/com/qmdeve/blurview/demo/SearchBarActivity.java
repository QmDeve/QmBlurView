package com.qmdeve.blurview.demo;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.qmdeve.blurview.demo.util.Utils;
import com.qmdeve.blurview.widget.SearchBar;

public class SearchBarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_searchbar);

        Utils.transparentStatusBar(getWindow());
        Utils.transparentNavigationBar(getWindow());

        SearchBar searchBar = findViewById(R.id.searchBar);
        searchBar.setOnSearchActionListener(query ->
                Toast.makeText(this, query, Toast.LENGTH_SHORT).show());
    }
}

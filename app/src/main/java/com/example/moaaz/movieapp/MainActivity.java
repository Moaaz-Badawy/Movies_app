package com.example.moaaz.movieapp;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.example.moaaz.movieapp.Data.MovieDataModel;

public class MainActivity extends AppCompatActivity implements Movie_fragment.Callback {

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    public static boolean mTwoPane;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (findViewById(R.id.movie_detail_container) != null) {
        // Tablets Mode
            mTwoPane = true;

        }// Mobile Phones Mode
        else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }



/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

           startActivity(new Intent(this,SettingsActivity.class));
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

@Override
public void onItemSelected(MovieDataModel content) {

    if (mTwoPane) {
        Detail_fragment fragment = new Detail_fragment();
        Bundle args = new Bundle();
        args.putParcelable("object",content);
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                .commit();

    } else {

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("object",content);
        startActivity(intent);


    }
}
}

package com.example.moaaz.movieapp.Data;

import android.app.Activity;
import android.media.Image;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.moaaz.movieapp.MainActivity;
import com.example.moaaz.movieapp.R;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by moaaz on 8/8/2016.
 */
public class MovieAdapter extends ArrayAdapter<MovieDataModel> {


    public MovieAdapter(Activity context, ArrayList<MovieDataModel> movies) {
        super(context, 0 ,movies);
    }




    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MovieDataModel dataModel=getItem(position);
        convertView= LayoutInflater.from(getContext()).inflate(R.layout.poster_view,parent,false);
        ImageView imageView=(ImageView)convertView.findViewById(R.id.imageView);


        // Handel Portrait View in (Tablets & Mobile phones)
        portraitView(imageView);
        Picasso.with(getContext()).load(dataModel.poster_Path).into(imageView);



        return convertView;
    }

    /**
     * Helper Method To adjust Width of Image View Dynamically
      * @param view ImageView of Poster
     */
    public void portraitView(ImageView view){

        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        if(metrics.heightPixels>metrics.widthPixels) {
            int width=metrics.widthPixels;
            android.view.ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            view.setLayoutParams(layoutParams);
            // Tablet Session
            if(MainActivity.mTwoPane){
            layoutParams.width = 172;
            }
            // Mobile Phones Session
            else{
                layoutParams.width = width / 2;}
        }
    }
}

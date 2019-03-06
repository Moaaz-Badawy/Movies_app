package com.example.moaaz.movieapp.Data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.moaaz.movieapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by moaaz on 9/11/2016.
 */
public class TrailerAdapter extends ArrayAdapter<String> {
    public TrailerAdapter(Context context, ArrayList<String> MovieList) {
        super(context,0, MovieList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String TrailerName=getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.trailer_view, parent, false);
        }

        TextView TrailerNameView = (TextView) convertView.findViewById(R.id.Trailer_Name);
        TrailerNameView.setText(TrailerName);

        return convertView;
    }


}

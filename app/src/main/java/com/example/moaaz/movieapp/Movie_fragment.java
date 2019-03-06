package com.example.moaaz.movieapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;


import com.example.moaaz.movieapp.Data.MovieAdapter;
import com.example.moaaz.movieapp.Data.MovieContract;
import com.example.moaaz.movieapp.Data.MovieDataModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;
import com.example.moaaz.movieapp.Data.MovieContract.FavouriteMovies;
/**
 * Created by moaaz on 8/2/2016.
 */
public class Movie_fragment extends Fragment {

    private int mPosition = GridView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";
    MovieAdapter adapter;
    GridView gridView;

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(MovieDataModel content);

    }

    public Movie_fragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onStart() {
        super.onStart();
        if(isOnline()){
            updatemovies();
        }
        else{
            SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
            String type=preferences.getString(getString(R.string.pref_type_list_key),getString(R.string.pref_default_value));
            if(type.equals("favourite")){
                FetchMovieTaskFromDB taskFromDB =new FetchMovieTaskFromDB();
                taskFromDB.execute();
               // Toast.makeText(getActivity(),"Favourite",Toast.LENGTH_LONG).show();
            }else
            Toast.makeText(getActivity(),"OffLine Mode(Could not Refresh)\n(You Can on Show Favourite Movies).\nPlease Check Your Internet Connection.",Toast.LENGTH_LONG).show();

        }
    }
    public void updatemovies(){
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
        String type=preferences.getString(getString(R.string.pref_type_list_key),getString(R.string.pref_default_value));
        if(type.equals("favourite")){
            FetchMovieTaskFromDB taskFromDB =new FetchMovieTaskFromDB();
            taskFromDB.execute();
        }else{

            FetchMovieTask task=new FetchMovieTask();
            task.execute(type);}
    }
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Gridtview.INVALID_POSITION
        if (mPosition != gridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootview= inflater.inflate(R.layout.movies_fragment,container,false);

        adapter=new MovieAdapter(getActivity(),new ArrayList<MovieDataModel>());
        gridView=(GridView)rootview.findViewById(R.id.gridView);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                MovieDataModel item = adapter.getItem(i);
                if (item != null) {
                    ((Callback) getActivity()).onItemSelected(item);
                }
                mPosition = i;
            }

       });

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        return rootview;
    }


    public class FetchMovieTask extends AsyncTask<String, Void, ArrayList<MovieDataModel>> {
        private final String LOG_TAG=FetchMovieTask.class.getSimpleName();
        private ArrayList<MovieDataModel> getMovieDataFromJson(String movieJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.

            final String jsonArrayName="results";
            final String MO_Title = "original_title";
            final String Base_Poster_url="http://image.tmdb.org/t/p/w342/";
            final String MO_poster_Path = "poster_path";
            final String MO_overview = "overview";
            final String MO_user_Rating = "vote_average";
            final String MO_release_date = "release_date";
            final String MO_id = "id";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(jsonArrayName);

            ArrayList<MovieDataModel> resultObject= new ArrayList<MovieDataModel>();

            for(int i = 0; i < movieArray.length(); i++) {

                String original_Title;
                String poster_Path;
                String overView;
                String user_Rating;
                String release_Date;
                int movie_Id;

                // Get the JSON object representing the movie
                JSONObject Movie = movieArray.getJSONObject(i);

                // get Data from Json Object
                original_Title=Movie.getString(MO_Title);
                poster_Path=Base_Poster_url.concat(Movie.getString(MO_poster_Path));
                overView=Movie.getString(MO_overview);
                user_Rating=Movie.getString(MO_user_Rating);
                release_Date=extractYear(Movie.getString(MO_release_date));
                movie_Id=Movie.getInt(MO_id);

                //Log.v(LOG_TAG,poster_Path);

                //Using MovieDataModel to Store data in array of objects
                MovieDataModel dataModel=new MovieDataModel(original_Title,poster_Path,overView,user_Rating,release_Date,movie_Id);
                resultObject.add(dataModel);
            }

            return resultObject;

        }

        /**
         * Helper Method To Extract Release Year from Full Date
         * @param release_date
         * @return String of year
         */
        private String extractYear(String release_date){
            StringTokenizer date=new StringTokenizer(release_date,"-");
            String year=null;

            year=date.nextToken();
            //Log.v(LOG_TAG,year);

            return year;
        }

        @Override
        protected ArrayList<MovieDataModel> doInBackground(String... params) {

            if(params.length==0){
                return null;
            }
            HttpURLConnection urlConnection=null;
            BufferedReader reader=null;

            String movieJsonstring=null;
            try {
                //Construct Url for themoviedb query
                final String BaseUrl = "http://api.themoviedb.org/3/movie/";
                final String APPID_PARAMS="api_key";

                Uri uri=Uri.parse(BaseUrl).buildUpon()
                        .appendPath(params[0])
                        .appendQueryParameter(APPID_PARAMS, BuildConfig.MOVIE_API_KEY).build();

                URL url=new URL(uri.toString());

                // Print Url
                Log.v(LOG_TAG,url.toString());


                // create request for themoviedb and open connection
                urlConnection=(HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                Log.v("Http Connection :","Connected");

                //Read Input stream
                InputStream inputStream=urlConnection.getInputStream();
                StringBuffer buffer=new StringBuffer();

                if(inputStream==null){
                    return null;
                }

                reader=new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while ((line=reader.readLine())!=null){

                    buffer.append(line+"\n");

                }

                if(buffer.length()==0)
                {
                    return null;
                }
                movieJsonstring=buffer.toString();


                // Print JSON Result
                Log.v(LOG_TAG,movieJsonstring);




            }catch (IOException ex){
                Log.e(LOG_TAG, "Erorr", ex);
                return null;

            } finally {
                if(urlConnection!=null){

                    urlConnection.disconnect();
                    Log.v("Http Connection :","Disconnected");
                }
                if(reader!=null){
                    try{
                        reader.close();
                    }catch (final IOException e){
                        Log.e(LOG_TAG, "Erorr closing Stream",e);
                    }

                }
                try {

                    return getMovieDataFromJson(movieJsonstring);

                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }catch (NullPointerException e){
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();

                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<MovieDataModel> result) {


            if (result != null) {
                adapter.clear();
                adapter.addAll(result);
            }else
            {
                Toast.makeText(getActivity(),"Error",Toast.LENGTH_LONG).show();}
        }
    }



    public class FetchMovieTaskFromDB extends AsyncTask<Void, Void, ArrayList<MovieDataModel>> {

        private final String LOG_TAG=FetchMovieTaskFromDB.class.getSimpleName();

        private ArrayList<MovieDataModel> getMovieDataFromDB()
                throws SQLException {

            ArrayList<MovieDataModel> resultObject= new ArrayList<MovieDataModel>();
            Cursor data = getActivity().getContentResolver().query(MovieContract.FavouriteMovies.CONTENT_URI, null, null, null, null);
            if (data.moveToFirst()) {
                String original_Title;
                String poster_Path;
                String overView;
                String user_Rating;
                String release_Date;
                int movie_Id;
                do {
                    original_Title=data.getString(data.getColumnIndex(FavouriteMovies.COLUMN_ORIGINAL_TITLE));
                    poster_Path=data.getString(data.getColumnIndex(FavouriteMovies.COLUMN_POSTER));
                    overView=data.getString(data.getColumnIndex(FavouriteMovies.COLUMN_OVERVIEW));
                    user_Rating=data.getString(data.getColumnIndex(FavouriteMovies.COLUMN_USER_RATING));
                    release_Date=data.getString(data.getColumnIndex(FavouriteMovies.COLUMN_RELEASE_DATE));
                    movie_Id=data.getInt(data.getColumnIndex(FavouriteMovies.COLUMN_MOVIE_ID));

                    MovieDataModel dataModel=new MovieDataModel(original_Title,poster_Path,overView,user_Rating,release_Date,movie_Id);
                    resultObject.add(dataModel);
                } while (data.moveToNext());
            }

            return resultObject;
        }

        @Override
        protected ArrayList<MovieDataModel> doInBackground(Void... voids) {

        try{
            return getMovieDataFromDB();
        }catch (SQLException ex){
            Log.v(LOG_TAG,ex.getStackTrace().toString());
        }
            return null;
        }


        @Override
        protected void onPostExecute(ArrayList<MovieDataModel> result) {


            if (result != null) {
                adapter.clear();
                adapter.addAll(result);
            }else {Toast.makeText(getActivity(),"Error in Fetch from DB",Toast.LENGTH_LONG).show();}
        }
    }
}

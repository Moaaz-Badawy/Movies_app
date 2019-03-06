package com.example.moaaz.movieapp;

import android.content.ContentValues;
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
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.moaaz.movieapp.Data.MovieContract;
import com.example.moaaz.movieapp.Data.MovieDataModel;
import com.example.moaaz.movieapp.Data.TrailerAdapter;
import com.squareup.picasso.Picasso;
import com.example.moaaz.movieapp.Data.MovieContract.FavouriteMovies;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by moaaz on 8/12/2016.
 */

public class Detail_fragment extends Fragment {

    public Detail_fragment(){}

    MovieDataModel dataModel;
    TextView title_textview;
    ImageView poster_ImageView;
    TextView year_textView;
    TextView rating_textView;
    TextView overview_textView;
    String Movie_id;

    ArrayList<String> Trailers_keys;
    ArrayList<String> Trailers_Names;
    TrailerAdapter TrailerAdapter;
    ListView TlistView;

    ArrayList<String> Reviews_Authors;
    ArrayList<String> Reviews_Contents;

    TextView Reviews_textView;
    @Override
    public void onStart() {
        super.onStart();

        if (isOnline()) {
            FetchTrailersTask Trailers_Task = new FetchTrailersTask();
            Trailers_Task.execute(Movie_id);

            FetchReviewsTask Reviews_Task = new FetchReviewsTask();
            Reviews_Task.execute(Movie_id);
        } else {

            SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
            String type=preferences.getString(getString(R.string.pref_type_list_key),getString(R.string.pref_default_value));
            if(type.equals("favourite")){
            FetchTrailersTaskOffline offlineTrailers = new FetchTrailersTaskOffline();
            FetchReviewsTaskOffline offlineReviews = new FetchReviewsTaskOffline();
            offlineTrailers.execute();
            offlineReviews.execute();
            } else{Toast.makeText(getActivity(),"You Are Offline(Could not Refresh).", Toast.LENGTH_SHORT).show();}
        }
    }

    /**
     * Helper Method To Check if There is Internet Connection Or Not
     * @return true if Connected & false if not connected
     */
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(MainActivity.mTwoPane){
            Bundle arguments = getArguments();
            if (arguments != null) {
                dataModel = (MovieDataModel)arguments.getParcelable("object");
            }

        }else{

            Intent intent=getActivity().getIntent();
            Bundle bundle=intent.getExtras();
            dataModel=bundle.getParcelable("object");
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.detail_fragment, container, false);

        Movie_id = "" + dataModel.movie_Id;
        title_textview = (TextView) rootview.findViewById(R.id.title);
        title_textview.setText(dataModel.original_Title);

        poster_ImageView = (ImageView) rootview.findViewById(R.id.poster);
        Picasso.with(getContext()).load(dataModel.poster_Path).into(poster_ImageView);

        year_textView = (TextView) rootview.findViewById(R.id.year);
        year_textView.setText(dataModel.release_Date);

        rating_textView = (TextView) rootview.findViewById(R.id.rating);
        rating_textView.setText(dataModel.user_Rating);

        overview_textView = (TextView) rootview.findViewById(R.id.overview);
        overview_textView.setText(dataModel.overView);

        TrailerAdapter = new TrailerAdapter(getActivity(), new ArrayList<String>());
        TlistView = (ListView) rootview.findViewById(R.id.Trailers_list);
        TlistView.setAdapter(TrailerAdapter);


        TlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // get Key for Youtube Trailer
                String Key = Trailers_keys.get(i);

                // Create Uri and then Launch Youtube
                Intent intent = new Intent(Intent.ACTION_VIEW);
                final String BaseUrl = "https://www.youtube.com/watch";
                final String Watch_Key = "v";

                Uri uri = Uri.parse(BaseUrl)
                        .buildUpon()
                        .appendQueryParameter(Watch_Key, Key).build();

                intent.setData(uri);
                startActivity(intent);

            }
        });


        Reviews_textView = (TextView) rootview.findViewById(R.id.Reviews);
        final ImageButton Favourite = (ImageButton) rootview.findViewById(R.id.Favourite);

        Cursor data = getActivity().getContentResolver().query(FavouriteMovies.CONTENT_URI,
                new String[]{FavouriteMovies.COLUMN_MOVIE_ID}, FavouriteMovies.COLUMN_MOVIE_ID + " = ?", new String[]{"" + dataModel.movie_Id}, null);

        if (data.moveToFirst()) {
            Favourite.setImageResource(R.drawable.ic_star_border_gold_24dp);
        } else {
            Favourite.setImageResource(R.drawable.ic_star_border_black_24dp);
        }

        Favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOnline()) {
                    Cursor data = getActivity().getContentResolver().query(FavouriteMovies.CONTENT_URI,
                            new String[]{FavouriteMovies.COLUMN_MOVIE_ID}, FavouriteMovies.COLUMN_MOVIE_ID + " = ?", new String[]{"" + dataModel.movie_Id}, null);

                    if (data.moveToFirst()) {
                        getActivity().getContentResolver().delete(FavouriteMovies.CONTENT_URI, FavouriteMovies.COLUMN_MOVIE_ID + "= ?"
                                , new String[]{"" + dataModel.movie_Id});
                        Toast.makeText(getActivity(), "Removed From Favourite", Toast.LENGTH_LONG).show();
                        Favourite.setImageResource(R.drawable.ic_star_border_black_24dp);
                    } else {
                        Favourite.setImageResource(R.drawable.ic_star_border_gold_24dp);
                        InsertFavouriteTask insert = new InsertFavouriteTask();
                        insert.execute();
                        Toast.makeText(getActivity(), "Marked As Favourite", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "You Are Offline", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rootview;
    }

    /**
     * Helper Method to Format The Database Strings For Trailers And Reviews
     *
     * @param list a list of Trailers(Keys or Names) or Reviews(Authors or Contents)
     * @return A string That Contains all Trailers or All reviews
     */
    public String FormatDatabaseStrings(ArrayList<String> list) {
        String formatedString = "";
        if (list.isEmpty()) {
            formatedString = "NO ITEMS";
        } else {
            for (int i = 0; i < list.toArray().length; i++) {
                formatedString = formatedString + list.get(i) + "^&";
            }

        }
        return formatedString;
    }




    /**
     * Inner Class for fetching Trailers Online
     */

    public class FetchTrailersTask extends AsyncTask<String, Void, ArrayList<String>> {


        private final String LOG_TAG = FetchTrailersTask.class.getSimpleName();


        @Override
        protected ArrayList<String> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String TrailerJsonstring = null;


            try {

                //Construct Url for themoviedb query
                final String BaseUrl = "https://api.themoviedb.org/3/movie/";
                final String APPID_PARAMS = "api_key";

                Uri uri = Uri.parse(BaseUrl).buildUpon()
                        .appendPath(params[0])
                        .appendPath("videos")
                        .appendQueryParameter(APPID_PARAMS, BuildConfig.MOVIE_API_KEY).build();

                URL url = new URL(uri.toString());

                // Print Url
//                Log.v(LOG_TAG, url.toString());


                // create request for themoviedb and open connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

//                Log.v("Http Connection :", "Connected");

                //Read Input stream
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                TrailerJsonstring = buffer.toString();


                // Print JSON Result
//                Log.v(LOG_TAG, TrailerJsonstring);


            } catch (IOException ex) {
                Log.e(LOG_TAG, "Erorr", ex);
                return null;

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
//                    Log.v("Http Connection :", "Disconnected");
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
//                        Log.e(LOG_TAG, "Erorr closing Stream", e);
                    }

                }
                try {return getMovieDataFromJson(TrailerJsonstring);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();

                }
            }
            return null;
        }

        private ArrayList<String> getMovieDataFromJson(String trailerJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.

            final String jsonArrayName = "results";
            final String MO_Trailer_Name = "name";
            final String MO_Trailer_Key = "key";

            JSONObject TrailerJson = new JSONObject(trailerJsonStr);
            JSONArray TrailerArray = TrailerJson.getJSONArray(jsonArrayName);

            Trailers_keys = new ArrayList<String>();
            Trailers_Names = new ArrayList<String>();


            for (int i = 0; i < TrailerArray.length(); i++) {

                String Trailer_key;
                String Trailer_name;

                // Get the JSON object representing the movie Trailers
                JSONObject Trailer = TrailerArray.getJSONObject(i);

                // get Data from Json Object
                Trailer_key = Trailer.getString(MO_Trailer_Key);
                Trailer_name = Trailer.getString(MO_Trailer_Name);
//                Log.v(LOG_TAG, Trailer_key);
                Trailers_keys.add(Trailer_key);
                Trailers_Names.add(Trailer_name);
            }
            return Trailers_Names;
        }


        @Override
        protected void onPostExecute(ArrayList<String> result) {


            if (result != null) {
                TrailerAdapter.clear();
                TrailerAdapter.addAll(result);

                int num = Trailers_keys.size();
                AdjustTrailersSpace(num);

            } else {
                Toast.makeText(getActivity(), "You Are Offline", Toast.LENGTH_LONG).show();
            }
        }

        /**
         * Helper Method To Adjust Trailers Space
         *
         * @param num number of Trailers
         */
        public void AdjustTrailersSpace(int num) {


            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int dpi = metrics.densityDpi;
            int actualPixels = 70 * (dpi / 160);
            if (actualPixels < 120) {
                actualPixels = 105;
            }


            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 0);
            switch (num) {
                case 1:
                    lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, actualPixels);
                    break;
                case 2:
                    lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, actualPixels * 2);
                    break;
                case 3:
                    lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, actualPixels * 3);
                    break;
                case 4:
                    lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, actualPixels * 4);
                    break;
                case 5:
                    lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, actualPixels * 5);
                    break;
                case 6:
                    lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, actualPixels * 6);
                    break;
                case 7:
                    lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, actualPixels * 7);
                    break;
                case 8:
                    lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, actualPixels * 8);
                    break;
                case 9:
                    lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, actualPixels * 9);
                    break;
                case 10:
                    lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, actualPixels * 10);
                    break;
            }
            LinearLayout layout1 = (LinearLayout) getActivity().findViewById(R.id.Linear);
            layout1.setLayoutParams(lp);
        }

    }

    /**
     * Inner Class for fetching Reviews Online
     */

    public class FetchReviewsTask extends AsyncTask<String, Void, ArrayList<String>> {

        private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();

        @Override
        protected ArrayList<String> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String ReviewsJsonstring = null;


            try {

                //Construct Url for themoviedb query
                final String BaseUrl = "https://api.themoviedb.org/3/movie/";
                final String APPID_PARAMS = "api_key";

                Uri uri = Uri.parse(BaseUrl).buildUpon()
                        .appendPath(params[0])
                        .appendPath("reviews")
                        .appendQueryParameter(APPID_PARAMS, BuildConfig.MOVIE_API_KEY).build();

                URL url = new URL(uri.toString());

                // Print Url
                Log.v(LOG_TAG, url.toString());


                // create request for themoviedb and open connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                Log.v("Http Connection :", "Connected");

                //Read Input stream
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                ReviewsJsonstring = buffer.toString();


                // Print JSON Result
//                Log.v(LOG_TAG, ReviewsJsonstring);


            } catch (IOException ex) {
                Log.e(LOG_TAG, "Erorr", ex);
                return null;
            } finally {
                if (urlConnection != null) {

                    urlConnection.disconnect();
                    Log.v("Http Connection :", "Disconnected");
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Erorr closing Stream", e);
                    }

                }
                try {

                    return getMovieDataFromJson(ReviewsJsonstring);

                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();

                }
            }
            return null;
        }

        private ArrayList<String> getMovieDataFromJson(String ReviewsJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.

            final String jsonArrayName = "results";
            final String MO_Reviews_Content = "content";
            final String MO_Reviews_author = "author";


            JSONObject ReviewJson = new JSONObject(ReviewsJsonStr);
            JSONArray ReviewArray = ReviewJson.getJSONArray(jsonArrayName);


            Reviews_Contents = new ArrayList<String>();
            Reviews_Authors = new ArrayList<String>();


            for (int i = 0; i < ReviewArray.length(); i++) {

                String Review_Content;
                String Review_author;

                // Get the JSON object representing the movie
                JSONObject Review = ReviewArray.getJSONObject(i);

                // get Data from Json Object
                Review_Content = Review.getString(MO_Reviews_Content);
                Review_author = Review.getString(MO_Reviews_author);


                Reviews_Contents.add(Review_Content);
                Reviews_Authors.add(Review_author);
            }
            return Reviews_Contents;
        }


        @Override
        protected void onPostExecute(ArrayList<String> result) {

            if(Reviews_Authors.isEmpty()){
                Reviews_textView.setText("There is not Any Reviews For This Movie");}
            else {
                for (int i = 0; i < Reviews_Contents.toArray().length; i++) {
                    Reviews_textView.append("Author : " + Reviews_Authors.get(i) + "\n" + Reviews_Contents.get(i) + "\n\n");
                }
            }
        }

    }

    /**
     * Inner Task For Insert Data In DataBase
     */
    public class InsertFavouriteTask extends AsyncTask<Void, Void, Void> {


        private final String LOG_TAG = InsertFavouriteTask.class.getSimpleName();

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put(FavouriteMovies.COLUMN_ORIGINAL_TITLE, dataModel.original_Title);
                contentValues.put(FavouriteMovies.COLUMN_POSTER, dataModel.poster_Path);
                contentValues.put(FavouriteMovies.COLUMN_OVERVIEW, dataModel.overView);
                contentValues.put(FavouriteMovies.COLUMN_USER_RATING, dataModel.user_Rating);
                contentValues.put(FavouriteMovies.COLUMN_RELEASE_DATE, dataModel.release_Date);
                contentValues.put(FavouriteMovies.COLUMN_MOVIE_ID, dataModel.movie_Id);
                contentValues.put(FavouriteMovies.COLUMN_TRAILERS_KEYS, FormatDatabaseStrings(Trailers_keys));
                contentValues.put(FavouriteMovies.COLUMN_TRAILERS_NAMES, FormatDatabaseStrings(Trailers_Names));
                contentValues.put(FavouriteMovies.COLUMN_REVIEWS_AUTHORS, FormatDatabaseStrings(Reviews_Authors));
                contentValues.put(FavouriteMovies.COLUMN_REVIEWS_CONTENTS, FormatDatabaseStrings(Reviews_Contents));
                getActivity().getContentResolver().insert(MovieContract.FavouriteMovies.CONTENT_URI, contentValues);

            } catch (SQLException ex) {
                Log.v(LOG_TAG, ex.getStackTrace().toString());
            }
            return null;
        }
    }

    /**
     * Inner Class for fetching Trailers Offline From Database of Favourite Option
     */

    public class FetchTrailersTaskOffline extends AsyncTask<Void, Void, ArrayList<String>> {


        private final String LOG_TAG = FetchTrailersTaskOffline.class.getSimpleName();
        String trailers_keys = "";
        String trailers_names = "";


        @Override
        protected ArrayList<String> doInBackground(Void... voids) {

            try {
                return getMovieTrailersFromDB();
            } catch (SQLException ex) {
                Log.v(LOG_TAG, ex.getStackTrace().toString());
            }
            return null;
        }

        private ArrayList<String> getMovieTrailersFromDB()
                throws SQLException {


            Trailers_keys = new ArrayList<String>();
            Trailers_Names = new ArrayList<String>();

            Cursor data = getActivity().getContentResolver().query(MovieContract.FavouriteMovies.CONTENT_URI, null
                    , FavouriteMovies.COLUMN_MOVIE_ID + "=?", new String[]{"" + dataModel.movie_Id}, null);


            if (data.moveToFirst()) {

                do {
                    trailers_keys = data.getString(data.getColumnIndex(FavouriteMovies.COLUMN_TRAILERS_KEYS));
                    trailers_names = data.getString(data.getColumnIndex(FavouriteMovies.COLUMN_TRAILERS_NAMES));

                } while (data.moveToNext());
            }

            String[] keys = ChangeToArray(trailers_keys);
            String[] names = ChangeToArray(trailers_names);


            for (int i = 0; i < keys.length; i++) {
                Trailers_keys.add(keys[i]);
                Trailers_Names.add(names[i]);

            }
            return Trailers_Names;
        }

        /**
         * Helper Method to change String to A Array of Strings
         * @param string(keys or Names )of Trailers
         * @return array of Keys or Names
         */
        private String[] ChangeToArray(String string) {
            ArrayList<String> list = new ArrayList<String>();
            StringTokenizer token = new StringTokenizer(string, "^&");
            int size = token.countTokens();
            String[] array = new String[size];
            for (int i = 0; i < array.length; i++) {
                array[i] = token.nextToken();
            }
            return array;
        }


        @Override
        protected void onPostExecute(ArrayList<String> result) {


            if (result != null) {
                TrailerAdapter.clear();
                TrailerAdapter.addAll(result);

                int num = Trailers_keys.size();
                AdjustTrailersSpace(num);

            } else {
                Toast.makeText(getActivity(), "You Are Offline", Toast.LENGTH_LONG).show();
            }
        }

        /**
         * Helper Method To Adjust Trailers Space
         *
         * @param num number of Trailers
         */
        public void AdjustTrailersSpace(int num) {


            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int dpi = metrics.densityDpi;
            int actualPixels = 70 * (dpi / 160);
            if (actualPixels < 120) {
                actualPixels = 105;
            }


            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 0);
            switch (num) {
                case 1:
                    lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, actualPixels);
                    break;
                case 2:
                    lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, actualPixels * 2);
                    break;
                case 3:
                    lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, actualPixels * 3);
                    break;
                case 4:
                    lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, actualPixels * 4);
                    break;
                case 5:
                    lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, actualPixels * 5);
                    break;
                case 6:
                    lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, actualPixels * 6);
                    break;
                case 7:
                    lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, actualPixels * 7);
                    break;
                case 8:
                    lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, actualPixels * 8);
                    break;
                case 9:
                    lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, actualPixels * 9);
                    break;
                case 10:
                    lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, actualPixels * 10);
                    break;
            }
            LinearLayout layout1 = (LinearLayout) getActivity().findViewById(R.id.Linear);
            layout1.setLayoutParams(lp);
        }

    }

    /**
     * Inner Class For performing Task of Fetch Reviews Offline For
     * Favourite Movies From DataBase
     */
    public class FetchReviewsTaskOffline extends AsyncTask<Void, Void, ArrayList<String>> {


        private final String LOG_TAG = FetchReviewsTaskOffline.class.getSimpleName();

        String reviews_authors = "";
        String reviews_contents = "";


        @Override
        protected ArrayList<String> doInBackground(Void... voids) {

            try {
                return getMovieReviewsFromDB();
            } catch (SQLException ex) {
                Log.v(LOG_TAG, ex.getStackTrace().toString());
            }
            return null;
        }

        private ArrayList<String> getMovieReviewsFromDB()
                throws SQLException {


            Reviews_Authors = new ArrayList<String>();
            Reviews_Contents = new ArrayList<String>();

            Cursor data = getActivity().getContentResolver().query(MovieContract.FavouriteMovies.CONTENT_URI, null
                    , FavouriteMovies.COLUMN_MOVIE_ID + "=?", new String[]{"" + dataModel.movie_Id}, null);


            if (data.moveToFirst()) {

                do {
                    reviews_authors = data.getString(data.getColumnIndex(FavouriteMovies.COLUMN_REVIEWS_AUTHORS));
                    reviews_contents = data.getString(data.getColumnIndex(FavouriteMovies.COLUMN_REVIEWS_CONTENTS));
                    Log.v("Moaaz", reviews_authors + reviews_contents);

                } while (data.moveToNext());
            }

            String[] authors = ChangeToArray(reviews_authors);
            String[] contents = ChangeToArray(reviews_contents);

            for (int i = 0; i < contents.length; i++) {
                Reviews_Authors.add(authors[i]);
                Reviews_Contents.add(contents[i]);

            }
            return Reviews_Contents;
        }

        /**
         * Helper Method to change String to A Array of Strings
         * @param string(Authors or Contents )of Reviews
         * @return array of Keys or Names
         */

        private String[] ChangeToArray(String string) {
            ArrayList<String> list = new ArrayList<String>();
            StringTokenizer token = new StringTokenizer(string, "^&");
            int size = token.countTokens();
            String[] array = new String[size];
            for (int i = 0; i < array.length; i++) {
                array[i] = token.nextToken();
            }
            return array;
        }


        @Override
        protected void onPostExecute(ArrayList<String> result) {

            if (result != null) {
                for (int i = 0; i < Reviews_Contents.toArray().length; i++) {
                    if(Reviews_Authors.get(i).equals("NO ITEMS")){
                        Reviews_textView.append("There is not Any Reviews For This Movie");
                    }else {
                        Reviews_textView.append("Author : " + Reviews_Authors.get(i) + "\n" + Reviews_Contents.get(i) + "\n\n");
                    }
                }
            }

        }
    }
}
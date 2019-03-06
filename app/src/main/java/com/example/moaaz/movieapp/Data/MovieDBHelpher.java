package com.example.moaaz.movieapp.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.moaaz.movieapp.Data.MovieContract.FavouriteMovies;

/**
 * Created by moaaz on 9/14/2016.
 */
public class MovieDBHelpher extends SQLiteOpenHelper{
    static final String DATABASE_NAME="movies.db";
    private static final int DATABASE_VERSION = 1;

    public MovieDBHelpher(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String CREATE_TABLE=
                "CREATE TABLE "+FavouriteMovies.TABLE_NAME+" ("+
                        FavouriteMovies._ID+" INTEGER PRIMARY KEY AUTOINCREMENT ,"+
                        FavouriteMovies.COLUMN_ORIGINAL_TITLE+" TEXT NOT NULL,"+
                        FavouriteMovies.COLUMN_MOVIE_ID+" INTEGER NOT NULL,"+
                        FavouriteMovies.COLUMN_POSTER+" TEXT NOT NULL,"+
                        FavouriteMovies.COLUMN_OVERVIEW+" TEXT NOT NULL,"+
                        FavouriteMovies.COLUMN_USER_RATING+" TEXT NOT NULL,"+
                        FavouriteMovies.COLUMN_RELEASE_DATE+" TEXT NOT NULL,"+
                        FavouriteMovies.COLUMN_TRAILERS_KEYS+" TEXT NOT NULL ,"+
                        FavouriteMovies.COLUMN_TRAILERS_NAMES+" TEXT NOT NULL ,"+
                        FavouriteMovies.COLUMN_REVIEWS_AUTHORS+" TEXT NOT NULL ,"+
                        FavouriteMovies.COLUMN_REVIEWS_CONTENTS+" TEXT NOT NULL );";

        sqLiteDatabase.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS"+FavouriteMovies.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}

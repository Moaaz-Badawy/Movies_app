package com.example.moaaz.movieapp.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by moaaz on 9/15/2016.
 */
public class MoviesContentProvider extends ContentProvider {

    private static final String LOG_TAG = MoviesContentProvider.class.getSimpleName();
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDBHelpher dbHelpher;


    // Code for the UriMatcher
    private static final int FAVOURITE = 1;
    private static final int FAVOURITE_WITH_ID = 2;


    private static UriMatcher buildUriMatcher(){

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MovieContract.FavouriteMovies.TABLE_NAME, FAVOURITE);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        dbHelpher= new MovieDBHelpher(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch(sUriMatcher.match(uri)){
            // All Favourite Movies selected
            case FAVOURITE:{
                retCursor = dbHelpher.getReadableDatabase().query(
                        MovieContract.FavouriteMovies.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                return retCursor;
            }
            // Individual Favourite based on Id selected
            case FAVOURITE_WITH_ID:{
                retCursor = dbHelpher.getReadableDatabase().query(
                        MovieContract.FavouriteMovies.TABLE_NAME,
                        projection,
                        MovieContract.FavouriteMovies._ID + " = ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder);
                return retCursor;
            }
            default:{
                // By default, we assume a bad URI
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match){
            case FAVOURITE:{
                return MovieContract.FavouriteMovies.CONTENT_DIR_TYPE;
            }
            case FAVOURITE_WITH_ID:{
                return MovieContract.FavouriteMovies.CONTENT_ITEM_TYPE;
            }
            default:{
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = dbHelpher.getWritableDatabase();
        Uri returnUri;
        switch (sUriMatcher.match(uri)) {
            case FAVOURITE: {
                long _id = db.insert(MovieContract.FavouriteMovies.TABLE_NAME, null, contentValues);
                // insert unless it is already contained in the database
                if (_id > 0) {
                    returnUri = MovieContract.FavouriteMovies.buildFlavorsUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into: " + uri);
                }
                break;
            }

            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);

            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelpher.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int numDeleted;
        switch(match){
            case FAVOURITE:
                numDeleted = db.delete(
                        MovieContract.FavouriteMovies.TABLE_NAME, selection, selectionArgs);
                // reset _ID
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                        MovieContract.FavouriteMovies.TABLE_NAME + "'");
                break;
            case FAVOURITE_WITH_ID:
                numDeleted = db.delete(MovieContract.FavouriteMovies.TABLE_NAME,
                        MovieContract.FavouriteMovies._ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))});
                // reset _ID
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                        MovieContract.FavouriteMovies.TABLE_NAME + "'");

                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return numDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelpher.getWritableDatabase();
        int numUpdated = 0;

        if (contentValues == null){
            throw new IllegalArgumentException("Cannot have null content values");
        }

        switch(sUriMatcher.match(uri)){
            case FAVOURITE:{
                numUpdated = db.update(MovieContract.FavouriteMovies.TABLE_NAME,
                        contentValues,
                        selection,
                        selectionArgs);
                break;
            }
            case FAVOURITE_WITH_ID: {
                numUpdated = db.update(MovieContract.FavouriteMovies.TABLE_NAME,
                        contentValues,
                        MovieContract.FavouriteMovies._ID + " = ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))});
                break;
            }
            default:{
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

        if (numUpdated > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numUpdated;
    }


}


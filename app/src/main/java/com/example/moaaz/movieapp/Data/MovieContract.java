package com.example.moaaz.movieapp.Data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by moaaz on 9/14/2016.
 */
public class MovieContract  {

    public static final String CONTENT_AUTHORITY = "com.example.moaaz.movieapp";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final class FavouriteMovies implements BaseColumns{

        public static final String TABLE_NAME="FavouriteMovies";
        public static final String COLUMN_ORIGINAL_TITLE="original_title";
        public static final String COLUMN_POSTER="poster";
        public static final String COLUMN_OVERVIEW="overview";
        public static final String COLUMN_USER_RATING="user_rating";
        public static final String COLUMN_RELEASE_DATE="release_date";
        public static final String COLUMN_MOVIE_ID="movie_id";
        public static final String COLUMN_TRAILERS_KEYS="trailers_keys";
        public static final String COLUMN_TRAILERS_NAMES="trailers_names";
        public static final String COLUMN_REVIEWS_CONTENTS="reviews_contents";
        public static final String COLUMN_REVIEWS_AUTHORS="reviews_authors";



        // create content uri
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(TABLE_NAME).build();

        // create cursor of base type directory for multiple entries
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;

        // create cursor of base type item for single entry
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE +"/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;


        // for building URIs on insertion
        public static Uri buildFlavorsUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }
}

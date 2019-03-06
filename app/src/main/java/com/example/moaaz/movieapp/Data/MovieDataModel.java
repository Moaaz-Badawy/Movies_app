package com.example.moaaz.movieapp.Data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by moaaz on 8/8/2016.
 */
public class MovieDataModel implements Parcelable{
    public String original_Title;
    public String poster_Path;
    public String overView;
    public String user_Rating;
    public String release_Date;
    public int movie_Id;



    // Constructor
    public MovieDataModel(String original_Title, String poster_Path, String overView,
                          String user_Rating, String release_Date, int movie_Id){

        this.original_Title=original_Title;
        this.poster_Path=poster_Path;
        this.overView=overView;
        this.user_Rating=user_Rating;
        this.release_Date=release_Date;
        this.movie_Id=movie_Id;

}

    private MovieDataModel(Parcel in){

        original_Title=in.readString();
        poster_Path=in.readString();
        overView=in.readString();
        user_Rating=in.readString();
        release_Date=in.readString();
        movie_Id=in.readInt();


    }


    public static final Creator<MovieDataModel> CREATOR = new Creator<MovieDataModel>() {
        @Override
        public MovieDataModel createFromParcel(Parcel in) {
            return new MovieDataModel(in);
        }

        @Override
        public MovieDataModel[] newArray(int size) {
            return new MovieDataModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeString(original_Title);
        parcel.writeString(poster_Path);
        parcel.writeString(overView);
        parcel.writeString(user_Rating);
        parcel.writeString(release_Date);
        parcel.writeInt(movie_Id);
    }
}

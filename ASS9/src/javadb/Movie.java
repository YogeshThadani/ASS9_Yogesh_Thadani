package javadb;

import java.util.*;
import java.sql.Date;

class Movie implements java.io.Serializable
{
   private int movieId;
   private String movieName;
   private String movieType;
   private String language;
   private Date releaseDate;
   private List<String> casting;
   private double rating, totalBusinessDone;

public int getMovieId()
{
    return movieId;
}

public void setMovieId(int mid)
{
    movieId = mid;
}

public void setMovieName(String mname)
{
    movieName = mname;
}

public String getMovieType() {
	return movieType;
}

public void setMovieType(String movieType) {
	this.movieType = movieType;
}

public String getLanguage() {
	return language;
}

public void setLanguage(String language) {
	this.language = language;
}

public List<String> getCasting() {
	return casting;
}

public void setCasting(List<String> casting) {
	this.casting = casting;
}

public double getTotalBusinessDone() {
	return totalBusinessDone;
}

public void setTotalBusinessDone(double totalBusinessDone) {
	this.totalBusinessDone = totalBusinessDone;
}

public String getMovieName() {
	return movieName;
}

public Date getReleaseDate() {
	return releaseDate;
}

public void setReleaseDate(Date d) {
	 releaseDate = d;
}

public double getRating() {
	return rating;
}

public void setRating(double r)
{
    rating = r;
}
}//Movie
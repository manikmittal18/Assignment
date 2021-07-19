package com.manik.quiz.assignment;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface  RetrofitObjectAPI {

    @Headers("Content-Type: text/html")
    @GET("data/2.5/weather")
    Call<Weather> getJson(@Query("lat")double lat,@Query("lon")double lon,@Query("appid")String apiKey);
}

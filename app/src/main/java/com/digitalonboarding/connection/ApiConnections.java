package com.digitalonboarding.connection;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by apptitud on 08/08/2017.
 */

public interface ApiConnections {

  @FormUrlEncoded
  @POST("start")
  Call<ResponseBody> generateCode(@Field("api_key") String apiKey, @Field("via") String via, @Field("phone_number") String phoneNumber, @Field("country_code") String country_code, @Field("locale") String locale);

  @GET("check")
  Call<ResponseBody> confirmCode(@Query("api_key") String apiKey, @Query("phone_number") String phoneNumber, @Query("country_code") String country_code, @Query("verification_code") String verificationCode);



}

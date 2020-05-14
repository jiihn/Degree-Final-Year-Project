package com.example.fyp.Fragments;

import com.example.fyp.Notifications.MyResponse;
import com.example.fyp.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA-6ihlAk:APA91bEOtTN1nyocXiLG3_6r2CQqx-slmlJiA5FHEY16qejZWjOcEprKVx8S5T2MV94A7XDSriZJMghrp41HmEJ1LRbFwPKwxwxLdHclKHcV6mj5SZp-CTCtcBvm7OgjHbxPn7t5DByJ"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}

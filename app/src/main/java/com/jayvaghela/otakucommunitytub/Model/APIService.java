package com.jayvaghela.otakucommunitytub.Model;

import com.jayvaghela.otakucommunitytub.Notifications.MyResponse;
import com.jayvaghela.otakucommunitytub.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA7wsc-aI:APA91bG1aC9kbC9ptrICshth4IKAHm8C0-HV7GSm0Xtol8Lttpk8pSPdTJAUVM97Zc6jTckZxyk981dGY7uEIuKgdEB27ggXokpl9UuP09WAmbg0v98_vIQPuXoeo0W5n6qtlaSZ1Htz"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}

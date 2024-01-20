package lk.jiat.xpect.service;

import java.util.ArrayList;
import java.util.List;

import lk.jiat.xpect.dto.EventDTO;
import lk.jiat.xpect.dto.UserDTO;
import lk.jiat.xpect.entity.Event;
import lk.jiat.xpect.model.Category;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface XpectWebService {
    @GET("category/getAllcategory")
    Call<List<Category>> getAllCategory();

    @POST("event/saveevent")
    Call<Void> registerEvent(@Body Event event);

    @GET("event/getallevent")
    Call<ArrayList<EventDTO>> getAllEvent();

    @GET("event/geteventbyuniqueid/{uniqueID}")
    Call<Event> getEventByUniqueId(@Path("uniqueID") String uniqueID);

    @GET("event/geteventbyuserid/{eventuserId}")
    Call<ArrayList<EventDTO>> getEventByUserId(@Path("eventuserId") String uniqueID);

    @POST("event/updateevent")
    Call<Void> updateEvent(@Body Event event);

    @GET("user/getUserByUniqueId/{userId}")
    Call<UserDTO> getUserByUniqueId(@Path("userId") String userId);

    @POST("user/updateuser")
    Call<Void> updateUser(@Body UserDTO userDTO);
}

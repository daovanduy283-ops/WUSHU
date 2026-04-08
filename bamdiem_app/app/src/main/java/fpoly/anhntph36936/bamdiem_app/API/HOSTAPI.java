package fpoly.anhntph36936.bamdiem_app.API;

import java.util.ArrayList;

import fpoly.anhntph36936.bamdiem_app.Model.bdiemModel;
import fpoly.anhntph36936.bamdiem_app.Model.thidauModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface HOSTAPI {
    String IPV4 = "192.168.0.108";
    String PORT = "3000";
    String HOST = IPV4+ ":" +PORT;
    String DOMAIN = "http://"+HOST;

    @GET("/api/list")
    Call<ArrayList<bdiemModel>> getBdiems();

    @PUT("/api/updatedo/{id}")
    Call<ArrayList<bdiemModel>> updateDiemdo(@Path("id") String id, @Body bdiemModel model);

    @PUT("/api/updatexanh/{id}")
    Call<ArrayList<bdiemModel>> updateDiemXanh(@Path("id") String id, @Body bdiemModel model);

    @PUT("/api/reset/{id}")
    Call<ArrayList<bdiemModel>> reset(@Path("id") String id, @Body bdiemModel model);
    @GET("/api/list/{id}")
    Call<ArrayList<bdiemModel>> getVtriId(@Query("id") String id);

    @GET("/api/list_thidau")
    Call<ArrayList<thidauModel>> getDSTD();

    @POST("/api/view/")
    Call<ResponseBody> sendData(@Body thidauModel model);
    @PUT("/api/update-view/{id}")
    Call<ResponseBody> updateData(@Path("id") String id, @Body thidauModel model);

    @PUT("/api/up_thidau/{id}")
    Call<ArrayList<thidauModel>> updateTD(@Path("id") String id, @Body thidauModel model);
}

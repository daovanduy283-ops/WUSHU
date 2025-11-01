package fpoly.anhntph36936.bamdiem_app.Main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fpoly.anhntph36936.bamdiem_app.API.HOSTAPI;
import fpoly.anhntph36936.bamdiem_app.Model.thidauModel;
import fpoly.anhntph36936.bamdiem_app.R;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Main_BamGio extends AppCompatActivity {
    Button btn_r1, btn_r2, btn_r3, btn_batdau, btn_huy,  btn_nghi, btn_1p30s, btn_2p;
    ImageView btn_ts_td, btn_ts_gd, btn_ts_tx, btn_ts_gx;
    TextView tv_sotran, tv_tsd, tv_tsx;
    Switch aSwitch;
    WebSocket webSocket;
    Button selectedButton = null;
    Button selectedButtonTime = null;
    String selectedRound = "";
    int minutes = 0;
    int seconds = 0;
    thidauModel item;
    int diem_n1;
    int diem_n2;
    String itemId;
    boolean play;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bam_gio);
        tv_sotran = findViewById(R.id.tv_sotran);
        btn_r1 = findViewById(R.id.btn_r1);
        btn_r2 = findViewById(R.id.btn_r2);
        btn_r3 = findViewById(R.id.btn_r3);
        btn_batdau = findViewById(R.id.btn_batdau);
        btn_huy = findViewById(R.id.btn_exit);
        btn_nghi = findViewById(R.id.btn_nghi);
        btn_1p30s = findViewById(R.id.btn_1p30s);
        btn_2p = findViewById(R.id.btn_2p);
        btn_ts_td = findViewById(R.id.btn_ts_td);
        btn_ts_gd = findViewById(R.id.btn_ts_gd);
        btn_ts_tx = findViewById(R.id.btn_ts_tx);
        btn_ts_gx = findViewById(R.id.btn_ts_gx);
        tv_tsd = findViewById(R.id.tv_tsd);
        tv_tsx = findViewById(R.id.tv_tsx);
        aSwitch = findViewById(R.id.switch_toggle);
        aSwitch.setVisibility(View.GONE);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        Intent intent = getIntent();
        if (intent != null) {
            int stt = intent.getIntExtra("stt", 0); // Lấy giá trị stt từ Intent, mặc định là 0 nếu không tìm thấy
            int dn1 = intent.getIntExtra("diem_n1", 0);
            int dn2 = intent.getIntExtra("diem_n2", 0);
            tv_sotran.setText(String.valueOf(stt));
            diem_n1 = dn1;
            diem_n2 = dn2;
            item = (thidauModel) intent.getSerializableExtra("thidauModel");
            if (item != null) {
                itemId = item.get_id();
                diem_n1 = item.getDiem_n1();
                diem_n2 = item.getDiem_n2();
                tv_tsd.setText(String.valueOf(diem_n1));
                tv_tsx.setText(String.valueOf(diem_n2));
            }
        }


//        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    sendWebSocketMessage("on");
//                } else {
//                    sendWebSocketMessage("off");
//                }
//            }
//        });
        btn_batdau.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                sendWebSocketMessage("off");
                if (selectedButtonTime == null || selectedRound == null){
                    Toast.makeText(Main_BamGio.this, "Hãy chọn round và time", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(Main_BamGio.this, Main_Run.class);
                    intent.putExtra("id", itemId);
                    intent.putExtra("round", selectedRound);
                    intent.putExtra("minutes", minutes);
                    intent.putExtra("seconds", seconds);
                    intent.putExtra("diem_n1", diem_n1);
                    intent.putExtra("diem_n2", diem_n2);
                    if (item != null) {
                        intent.putExtra("thidauModel", item); // Truyền đối tượng thidauModel hiện tại
                    } else {
                        Log.e("Main_BamGio", "thidauModel is null"); // Log lỗi nếu item là null
                    }
                    startActivity(intent);
                }

            }
        });
        btn_huy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thidauModel model = new thidauModel();
                model.setRound("CHƯA BẮT ĐẦU");
                model.setName_n1("GIÁP ĐỎ");
                model.setName_n2("GIÁP XANH");
                model.setDiem_n1(0);
                model.setDiem_n2(0);
                model.setProvince_n1("ĐƠN VỊ");
                model.setProvince_n2("ĐƠN VỊ");
                sendDataToServer(itemId, model);
                startActivity(new Intent(Main_BamGio.this, Main_DSTD.class));
            }
        });
        btn_nghi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thidauModel model = new thidauModel();
                model.setRound("NGHỈ GIỮA HIỆP");
                model.setMinute(1);
                model.setSecond(0);
                model.setDiem_n1(diem_n1);
                model.setDiem_n2(diem_n2);
                sendDataToServer(itemId, model);

                Intent intent = new Intent(Main_BamGio.this, Main_Run.class);
                intent.putExtra("id", itemId);
                intent.putExtra("round", "NGHỈ GIỮA HIỆP");
                intent.putExtra("minutes", 1);
                intent.putExtra("seconds", 01);
                intent.putExtra("diem_n1", diem_n1);
                intent.putExtra("diem_n2", diem_n2);
                if (item != null) {
                    intent.putExtra("thidauModel", item);
                } else {
                    Log.e("Main_BamGio", "thidauModel is null");
                }

                startActivity(intent);
            }
            });

        btn_r1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleButtonClick(btn_r1, "ROUND 1");
            }
        });

        btn_r2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleButtonClick(btn_r2, "ROUND 2");
            }
        });

        btn_r3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleButtonClick(btn_r3, "ROUND 3");
            }
        });
        btn_1p30s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleButtonClickTime(btn_1p30s, 01, 31);
            }
        });

        btn_2p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleButtonClickTime(btn_2p, 02, 01);
            }
        });

        btn_ts_td.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (diem_n1 < 3){
                    diem_n1++;
                    tv_tsd.setText(String.valueOf(diem_n1));
                    updateDiem(itemId);
                }
            }
        });

        btn_ts_gd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (diem_n1 > 0 ){
                    diem_n1--;
                    tv_tsd.setText(String.valueOf(diem_n1));
                    updateDiem(itemId);
                }
            }
        });

        btn_ts_tx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (diem_n2 < 3){
                    diem_n2++;
                    tv_tsx.setText(String.valueOf(diem_n2));
                    updateDiem(itemId);
                }
            }
        });

        btn_ts_gx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (diem_n2 > 0) {
                    diem_n2--;
                    tv_tsx.setText(String.valueOf(diem_n2));
                    updateDiem(itemId);
                }
            }
        });

    }

    private void handleButtonClick(Button button, String roundName) {
        if (button == selectedButton) {
            selectedButton.setBackgroundResource(R.drawable.button_selector);
            selectedButton = null;
            selectedRound = null;
        } else {
            if (selectedButton != null) {
                selectedButton.setBackgroundResource(R.drawable.button_selector);
            }
            selectedButton = button;
                button.setBackgroundResource(R.drawable.button_pressed);
            selectedRound = roundName;
        }
    }

    private void handleButtonClickTime(Button button, int phut, int giay) {
        if (button == selectedButtonTime) {
            selectedButtonTime.setBackgroundResource(R.drawable.button_selector);
            selectedButtonTime = null;
            minutes = 0;
            seconds = 0;
            return;
        }

        if (selectedButtonTime != null) {
            selectedButtonTime.setBackgroundResource(R.drawable.button_selector);
        }

        selectedButtonTime = button;
        button.setBackgroundResource(R.drawable.button_pressed);
        minutes = phut;
        seconds = giay;
    }
    private void updateDiem(String id) {
        if (id != null) {
            thidauModel model = new thidauModel();
            model.setDiem_n1(diem_n1);
            model.setDiem_n2(diem_n2);
            updateDT(id, model);
            sendDataToServer(id, model);
        }
    }
    private void updateDT(String id, thidauModel model) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(HOSTAPI.DOMAIN)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            HOSTAPI apiService = retrofit.create(HOSTAPI.class);
            Call<ArrayList<thidauModel>> call = apiService.updateTD(id, model);
            call.enqueue(new Callback<ArrayList<thidauModel>>() {
                @Override
                public void onResponse(Call<ArrayList<thidauModel>> call, Response<ArrayList<thidauModel>> response) {
                    if (response.isSuccessful()) {
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<thidauModel>> call, Throwable t) {

                }
            });
    }
    private void sendDataToServer(String id, thidauModel model) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOSTAPI.DOMAIN)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        HOSTAPI hostapi = retrofit.create(HOSTAPI.class);
        Call<ResponseBody> call = hostapi.updateData(id, model);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d("SERVER_RESPONSE", "Success: " + response.body().toString());
                } else {
                    Log.e("SERVER_RESPONSE", "Error: " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("SERVER_RESPONSE", "Failure: " + t.getMessage());
            }
        });

    }

    private void sendWebSocketMessage(String action) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", action);
            if (webSocket != null) {
                webSocket.send(jsonObject.toString());
                Log.d("WebSocket", "Sent: " + jsonObject.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initWebSocket() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();

        Request request = new Request.Builder()
                .url("ws://" + HOSTAPI.HOST + "/ws")
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {

            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                Log.d("WebSocket", "Connected");
                Log.d("WebSocket", "Response: " + response.toString());
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d("WebSocket", "Message received: " + text);

                try {
                    JSONObject jsonObject = new JSONObject(text);
                    if (jsonObject.has("action")) {
                        String action = jsonObject.getString("action");
                        Log.d("WebSocket", "Action Sound: " + action);
                        switch (action) {
                            case "playSound":
                                Toast.makeText(Main_BamGio.this, "Playing sound", Toast.LENGTH_SHORT).show();
                                break;
                            case "stopSound":
                                Toast.makeText(Main_BamGio.this, "Stopping sound", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                Log.d("WebSocket", "Unknown action: " + action);
                                break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                Log.e("WebSocket", "Error: " + t.getMessage());
                t.printStackTrace(); // In chi tiết lỗi
                if (response != null) {
                    Log.e("WebSocket", "Response: " + response.message());
                }
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(code, reason);
                Log.d("WebSocket", "Closing: " + reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d("WebSocket", "Closed: " + reason);
            }
        });
    }

}
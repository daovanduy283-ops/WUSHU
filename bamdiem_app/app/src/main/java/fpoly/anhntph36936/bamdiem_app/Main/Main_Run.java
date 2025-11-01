package fpoly.anhntph36936.bamdiem_app.Main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import fpoly.anhntph36936.bamdiem_app.API.HOSTAPI;
import fpoly.anhntph36936.bamdiem_app.Model.thidauModel;
import fpoly.anhntph36936.bamdiem_app.R;
import okhttp3.OkHttp;
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

public class Main_Run extends AppCompatActivity {
    TextView tv_round, tv_run;
    Button btn_huy, btn_run, btn_10s, btn_huys;
    private CountDownTimer countDownTimer;
    private CountDownTimer temporaryCountDownTimer;
    private boolean isRunning = false;
    private boolean isTemporaryTimerRunning = false;
    private long timeLeftInMillis;
    private long previousTimeInMillis;
    private long initialTimeInMillis;
    private long time10s;
    thidauModel item;
    int minutes, seconds, diem_n1, diem_n2;
    String round = "";
    String id;
    WebSocket webSocket;
    boolean soundPlaying = false;
    private boolean isCountdown10s = false;
    private MediaPlayer mediaPlayer;
    private long pausedTimeLeftInMillis = 0;
    private boolean pausedSoundPlaying = false;
    private int soundCurrentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_run);
        tv_round = findViewById(R.id.tv_round);
        tv_run = findViewById(R.id.tv_run);
        btn_huy = findViewById(R.id.btn_huy);
        btn_huys = findViewById(R.id.btn_huys);
        btn_run = findViewById(R.id.btn_run);
        btn_10s = findViewById(R.id.btn_10s);
        btn_huys.setVisibility(View.GONE);

//        mediaPlayer = MediaPlayer.create(this, R.raw.wushu_269_1);
        // code moi
        mediaPlayer = MediaPlayer.create(this, R.raw.wushu_269_1);
        mediaPlayer.setOnCompletionListener(mp -> {
            soundCurrentPosition = 0;
            soundPlaying = false;
            // Không cần làm gì thêm, chỉ reset trạng thái
        });


        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        Intent intent = getIntent();
        if (intent != null) {
            id = intent.getStringExtra("id");
            round = intent.getStringExtra("round");
            diem_n1 = intent.getIntExtra("diem_n1", 0);
            diem_n2 = intent.getIntExtra("diem_n2", 0);
            minutes = intent.getIntExtra("minutes", 0);
            seconds = intent.getIntExtra("seconds", 0);
            item = (thidauModel) intent.getSerializableExtra("thidauModel");

            tv_round.setText(round);
            initialTimeInMillis = (minutes * 60 + (seconds)) * 1000;
            timeLeftInMillis = initialTimeInMillis;
            updateTimerDisplay(timeLeftInMillis);
        }

        initWebSocket();

        btn_huy.setOnClickListener(v -> {
            if (Main_BamDiem.isOnOff()) {
                Toast.makeText(Main_Run.this, "Không thể tắt khi onoff đang bật", Toast.LENGTH_SHORT).show();
            } else {
                sendSoundControlMessage("stopSound");
                // Thực hiện hành động tắt
                soundPlaying = false;

                pauseTimer();
                stopTemporaryTimer();
                minutes = 0;
                seconds = 0;
                round = "KẾT THÚC -  " + round;
                updateClock(id);
                finish();
            }
        });

        btn_10s.setOnClickListener(v -> {
            if (Main_BamDiem.isOnOff()) {
                Toast.makeText(Main_Run.this, "Không thể tắt khi onoff đang bật", Toast.LENGTH_SHORT).show();
            } else {
                // ✅ Lưu trạng thái timer chính
                pausedTimeLeftInMillis = timeLeftInMillis;
                pausedSoundPlaying = soundPlaying;

                // ✅ Lưu vị trí phát của âm thanh đang chạy
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    soundCurrentPosition = mediaPlayer.getCurrentPosition();
                    mediaPlayer.pause();
                }

                pauseTimer(); // Dừng timer chính
                btn_huy.setVisibility(View.GONE);
                btn_huys.setVisibility(View.VISIBLE);
                stopTemporaryTimer();
                sendSoundControlMessage("resetCountdown");

                isCountdown10s = true;

                startTemporaryTimer(11000, false); // Bắt đầu 10s
            }
        });

        btn_huys.setOnClickListener(v -> {
            if (Main_BamDiem.isOnOff()) {
                Toast.makeText(Main_Run.this, "Không thể tắt khi onoff đang bật", Toast.LENGTH_SHORT).show();
            } else {
                stopTemporaryTimer();
//                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
//                    mediaPlayer.stop();
//                    try {
//                        mediaPlayer.prepare();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
                // code moi
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause(); // Chỉ tạm dừng
                    soundCurrentPosition = 0; // Reset vị trí
                }

                // Trả lại trạng thái cho timer chính
                timeLeftInMillis = pausedTimeLeftInMillis;
                isCountdown10s = false;
                btn_huy.setVisibility(View.VISIBLE);
                btn_huys.setVisibility(View.GONE);

                // Tính toán vị trí âm thanh dựa trên thời gian còn lại của timer chính
                if (timeLeftInMillis / 1000 <= 10 && pausedSoundPlaying) {
                    // Giả sử âm thanh dài 10 giây (10,000ms)
                    int soundDuration = 10000; // Cần xác nhận thời lượng thực tế của file R.raw.soudwushu
                    long secondsLeft = timeLeftInMillis / 1000;
                    // Tính vị trí bắt đầu của âm thanh: (10s - thời gian còn lại)
                    soundCurrentPosition = (int) ((10 - secondsLeft) * 1000);
                    playSoundAtPosition(soundCurrentPosition);
                    soundPlaying = true;
                } else {
                    soundPlaying = false;
                }

                startTimer(); // Tiếp tục timer chính
            }
        });

        btn_run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Main_BamDiem.isOnOff()) {
                    Toast.makeText(Main_Run.this, "Không thể thao tác khi cấm dùng đang bật", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isTemporaryTimerRunning) {
                    if (isRunning) {
                        pauseTemporaryTimer();
                        if (isCountdown10s) {
                            pauseSound(); // ✅ dùng hàm pauseSound nội bộ
                            sendSoundControlMessage("pauseSound");
                        }
                        btn_run.setText("Tiếp tục");
                    } else {
                        startTemporaryTimer(previousTimeInMillis, true);
                        if (isCountdown10s) {
                            resumeSound(); // ✅ thêm dòng này để tiếp tục phát lại
                            sendSoundControlMessage("resumeSound");
                        }
                        btn_run.setText("Dừng");
                    }
                } else {
                    if (isRunning) {
                        pauseTimer();
                        pauseSound(); // ✅ tạm dừng âm thanh
                        btn_run.setText("Tiếp tục");
                    } else {
                        startTimer();
                        resumeSound(); // ✅ phát tiếp âm thanh nếu còn trong 10s
                        btn_run.setText("Dừng");
                    }
                }
            }
        });

        startTimer();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerDisplay(timeLeftInMillis);
                updateClock(id);

                long secondsLeft = millisUntilFinished / 1000;
                Log.d("TIMER_DEBUG", "secondsLeft = " + secondsLeft + ", isCountdown10s = " + isCountdown10s + ", soundPlaying = " + soundPlaying);
                if (!isCountdown10s && secondsLeft <= 10 && !soundPlaying) {
                    // Giả sử âm thanh dài 10 giây (10,000ms)
                    int soundDuration = 10000; // Cần xác nhận thời lượng thực tế của file R.raw.soudwushu
                    // Tính vị trí bắt đầu của âm thanh: (10s - thời gian còn lại)
                    soundCurrentPosition = (int) ((10 - secondsLeft) * 1000);
                    playSoundAtPosition(soundCurrentPosition);
                    soundPlaying = true;
                }
            }

            @Override
            public void onFinish() {
                isRunning = false;
                tv_run.setText("00:00");
                btn_run.setText("Tiếp tục");
                updateClock(id);
                sendSoundControlMessage("timerFinished");
                if (id != null) {
                    thidauModel model = new thidauModel();
                    model.set_id(id);
                    model.setRound(round);
                    model.setDiem_n1(diem_n1);
                    model.setDiem_n2(diem_n2);
                    model.setMinute(0);
                    model.setSecond(0);
                    updateDT(id, model);
                    sendDataToServer(id, model);
                }
            }
        }.start();

        isRunning = true;
        btn_run.setText("Dừng");
    }

    private void startTemporaryTimer(long duration, boolean isResuming) {
        previousTimeInMillis = duration;

        // Chỉ lưu trạng thái âm thanh của timer CHÍNH khi bắt đầu timer 10s (chứ không phải khi resume)
        if (!isResuming) {
            // Dừng âm thanh timer chính và lưu lại vị trí
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                soundCurrentPosition = mediaPlayer.getCurrentPosition();
                mediaPlayer.pause();
            }
            isCountdown10s = true;
        }

        // ✅ ĐÂY LÀ LOGIC SỬA LỖI
        if (isResuming) {
            // Nếu là "Tiếp tục", thì phát tiếp âm thanh từ vị trí đã lưu
            resumeSound();
            sendSoundControlMessage("resumeSound");
        } else {
            // Nếu là "Bắt đầu mới" (từ btn_10s), thì phát từ đầu
            soundCurrentPosition = 0;
            playSound();
        }

        temporaryCountDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                previousTimeInMillis = millisUntilFinished;
                updateTimerDisplay(previousTimeInMillis);
                updateClock(id);
            }

            @Override
            public void onFinish() {
                isTemporaryTimerRunning = false;
                updateTimerDisplay(timeLeftInMillis);
                btn_huy.setVisibility(View.VISIBLE);
                btn_huys.setVisibility(View.GONE);
                updateClock(id);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    timeLeftInMillis = pausedTimeLeftInMillis;
                    isCountdown10s = false;

                    // Tính toán vị trí âm thanh dựa trên thời gian còn lại của timer chính
                    if (timeLeftInMillis / 1000 <= 10 && pausedSoundPlaying) {
                        // Giả sử âm thanh dài 10 giây (10,000ms)
                        int soundDuration = 10000; // Cần xác nhận thời lượng thực tế của file R.raw.soudwushu
                        long secondsLeft = timeLeftInMillis / 1000;
                        // Tính vị trí bắt đầu của âm thanh: (10s - thời gian còn lại)
                        soundCurrentPosition = (int) ((10 - secondsLeft) * 1000);
                        playSoundAtPosition(soundCurrentPosition);
                        soundPlaying = true;
                    } else {
                        soundPlaying = false;
//                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
//                            mediaPlayer.stop();
//                            try {
//                                mediaPlayer.prepare();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
                        // code moi
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            soundCurrentPosition = 0;
                        }
                    }

                    startTimer(); // Tiếp tục timer chính
                }, 500);
            }
        }.start();

        isRunning = true;
        isTemporaryTimerRunning = true;
        btn_run.setText("Dừng");
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            previousTimeInMillis = timeLeftInMillis;
        }
        isRunning = false;
        btn_run.setText("Tiếp tục");
    }

    private void pauseTemporaryTimer() {
        if (temporaryCountDownTimer != null) {
            temporaryCountDownTimer.cancel();
        }
        isRunning = false;
        btn_run.setText("Tiếp tục");
    }
    private void stopTemporaryTimer() {
        if (temporaryCountDownTimer != null) {
            temporaryCountDownTimer.cancel();
        }
        isTemporaryTimerRunning = false;
    }

    private void updateTimerDisplay(long millisUntilFinished) {
        minutes = (int) (millisUntilFinished / 1000) / 60;
        seconds = (int) (millisUntilFinished / 1000) % 60;

        String timeLeftFormatted = String.format("%02d:%02d", minutes, seconds);
        tv_run.setText(timeLeftFormatted);
    }
    private void updateClock(String id) {
        if (id != null) {
            thidauModel model = new thidauModel();
            model.set_id(id);
            model.setRound(round);
            model.setDiem_n1(diem_n1);
            model.setDiem_n2(diem_n2);
            model.setMinute(minutes);
            model.setSecond(seconds);
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
    private void sendSoundControlMessage(String soundAction) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "soundControl");
            jsonObject.put("soundAction", soundAction);
            if (webSocket != null) {
                webSocket.send(jsonObject.toString());
                Log.d("WebSocket", "Sent_Main_Run: " + jsonObject.toString());
                // Gửi thêm syncTimer khi timer kết thúc
                if ("timerFinished".equals(soundAction)) {
                    JSONObject syncObject = new JSONObject();
                    syncObject.put("action", "syncTimer");
                    syncObject.put("timeLeft", 0); // Gửi 0 khi timer kết thúc
                    webSocket.send(syncObject.toString());
                    Log.d("WebSocket", "Sent syncTimer: 0");
                }
            } else {
                Log.d("WebSocket", "WebSocket is null. Cannot send message.");
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
                Log.d("WebSocket_Main_Run", "Connected");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JSONObject jsonObject = new JSONObject(text);
                    String action = jsonObject.optString("action");

                    if ("soundControl".equals(action)) {
                        String soundAction = jsonObject.optString("soundAction");
                        if ("resetCountdown".equals(soundAction)) {
                            // Xử lý nếu cần
                        }
                    } else if ("syncTimer".equals(action)) {
                        // Nhận thông điệp đồng bộ từ server
                        long serverTime = jsonObject.optLong("timeLeft", -1);
                        if (serverTime >= 0) {
                            timeLeftInMillis = serverTime;
                            updateTimerDisplay(timeLeftInMillis);
                            Log.d("WebSocket_Main_Run", "Synced timer from server: " + serverTime);
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

    private void playSound() {
        try {
            // Nếu mediaPlayer đã tồn tại, không khởi tạo lại
//            if (mediaPlayer == null) {
//                mediaPlayer = MediaPlayer.create(this, R.raw.wushu_269_1);
//            }
            // code moi
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, R.raw.wushu_269_1);
                mediaPlayer.setOnCompletionListener(mp -> {
                    soundCurrentPosition = 0;
                    soundPlaying = false;
                });
            }

            // Nếu đang phát, dừng và chuẩn bị lại
//            if (mediaPlayer.isPlaying()) {
//                mediaPlayer.stop();
//                mediaPlayer.prepare();
//            }
//
//            // Đảm bảo vị trí không vượt quá thời lượng âm thanh
//            int soundDuration = mediaPlayer.getDuration();
//            if (soundCurrentPosition < 0 || soundCurrentPosition >= soundDuration) {
//                soundCurrentPosition = 0;
//            }
//
//            mediaPlayer.seekTo(soundCurrentPosition);
//            mediaPlayer.start();
//            soundPlaying = true;
//
//            mediaPlayer.setOnCompletionListener(mp -> {
//                soundCurrentPosition = 0;
//                soundPlaying = false;
//            });
            // code moi
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause(); // Tạm dừng nếu nó đang phát cái gì đó
            }
            soundCurrentPosition = 0; // Đặt lại vị trí
            mediaPlayer.seekTo(soundCurrentPosition); // Tua về 0
            mediaPlayer.start(); // Phát
            soundPlaying = true;

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Không thể phát âm thanh", Toast.LENGTH_SHORT).show();
        }
    }

    private void pauseSound() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            soundCurrentPosition = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            soundPlaying = false;
        }
    }

    private void resumeSound() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying() && soundCurrentPosition > 0) {
            // Đảm bảo vị trí không vượt quá thời lượng âm thanh
            int soundDuration = mediaPlayer.getDuration();
            if (soundCurrentPosition < 0 || soundCurrentPosition >= soundDuration) {
                soundCurrentPosition = 0;
            }

            mediaPlayer.seekTo(soundCurrentPosition);
            mediaPlayer.start();
            soundPlaying = true;

            mediaPlayer.setOnCompletionListener(mp -> {
                soundCurrentPosition = 0;
                soundPlaying = false;
            });
        }
    }

    private void playSoundAtPosition(int position) {
        try {
            // Nếu mediaPlayer chưa tồn tại, khởi tạo
//            if (mediaPlayer == null) {
//                mediaPlayer = MediaPlayer.create(this, R.raw.wushu_269_1);
//            }
            // code moi
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, R.raw.wushu_269_1);
                mediaPlayer.setOnCompletionListener(mp -> {
                    soundCurrentPosition = 0;
                    soundPlaying = false;
                });
            }

            // Nếu đang phát, dừng và chuẩn bị lại
//            if (mediaPlayer.isPlaying()) {
//                mediaPlayer.stop();
//                mediaPlayer.prepare();
//            }
//
//            // Đảm bảo vị trí không vượt quá thời lượng âm thanh
//            int soundDuration = mediaPlayer.getDuration();
//            if (position < 0 || position >= soundDuration) {
//                position = 0; // Nếu vị trí không hợp lệ, phát từ đầu
//            }
//
//            mediaPlayer.seekTo(position);
//            mediaPlayer.start();
//            soundPlaying = true;
//
//            mediaPlayer.setOnCompletionListener(mp -> {
//                soundCurrentPosition = 0;
//                soundPlaying = false;
//            });

            // code moi
            int soundDuration = mediaPlayer.getDuration();
            if (position < 0 || position >= soundDuration) {
                position = 0; // Nếu vị trí không hợp lệ, phát từ đầu
            }

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }

            soundCurrentPosition = position; // Lưu vị trí hiện tại
            mediaPlayer.seekTo(position); // Tua đến vị trí
            mediaPlayer.start(); // Phát
            soundPlaying = true;

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Không thể phát âm thanh", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close(1000, "App closed");
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }



}

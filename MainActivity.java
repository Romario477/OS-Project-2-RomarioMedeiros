package com.example.timedown;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private EditText timeInput;
    private boolean isCounting;
    private TextView viewTimeLeft;
    private Button bStartPause;
    private long millisTotalTime;
    private long millisTimeLeft;
    private Button bSet;
    private long timeEnd;
    private CountDownTimer timerCountDown;
    private Button bReset;
    Vibrator vibrator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timeInput = findViewById(R.id.inputTime);
        viewTimeLeft =findViewById(R.id.timeLeftText);
        bSet =findViewById(R.id.enterTime);
        bStartPause =findViewById(R.id.b_StartPause);
        bReset =findViewById(R.id.b_Reset);
        vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
        bSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input= timeInput.getText().toString();
                if(input.length()==0){
                    Toast.makeText(MainActivity.this, "Enter time", Toast.LENGTH_SHORT).show();
                    return;
                }
                long millisInput=Long.parseLong(input)*60000;
                if(millisInput==0){
                    Toast.makeText(MainActivity.this, "Time must be higher", Toast.LENGTH_SHORT).show();
                    return;
                }
                inputTime(millisInput);
                timeInput.setText("");
            }
        });
        bStartPause.setOnClickListener((v)->{
                if(isCounting){
                    pause();
                }
                else{
                    startCountdown();
                }
        });

        bReset.setOnClickListener((v)->{ reset(); });
    }
    private void inputTime(long milliseconds){
        millisTotalTime =milliseconds;
        reset();
        keyboard();
    }
    private void startCountdown(){
        timeEnd =System.currentTimeMillis()+ millisTimeLeft;
        timerCountDown =new CountDownTimer(millisTimeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                millisTimeLeft =millisUntilFinished;
                timeTextFormat();
            }
            @Override
            public void onFinish() {
                isCounting =false;
                visibleInterface();
                vibrator.vibrate(2000);
            }
        }.start();
        isCounting =true;
        visibleInterface();
    }
    private void pause(){
        timerCountDown.cancel();
        isCounting =false;
        visibleInterface();
    }
    private void reset(){
        millisTimeLeft = millisTotalTime;
        timeTextFormat();
        visibleInterface();
    }
    private void timeTextFormat(){
        int hours=(int) (millisTimeLeft /1000)/3600;
        int minutes=(int) ((millisTimeLeft /1000)%3600)/60;
        int seconds=(int) millisTimeLeft /1000%60;
        String timeLeftFormatted;
        if(hours>0){
            timeLeftFormatted=String.format(Locale.getDefault(),
                    "%d:%02d:%02d",hours,minutes,seconds);
        }
        else{
            timeLeftFormatted=String.format(Locale.getDefault(),"%02d:%02d",minutes,seconds);
        }

        viewTimeLeft.setText(timeLeftFormatted);
    }
    private void visibleInterface(){
        if(isCounting){
            timeInput.setVisibility(View.INVISIBLE);
            bSet.setVisibility(View.INVISIBLE);
            bReset.setVisibility(View.INVISIBLE);
            bStartPause.setText("Pause");
        }
        else{
            timeInput.setVisibility(View.VISIBLE);
            bSet.setVisibility(View.VISIBLE);
            bStartPause.setText("Start");
            if(millisTimeLeft <1000){
                bStartPause.setVisibility(View.INVISIBLE);
            }
            else{
                bStartPause.setVisibility(View.VISIBLE);
            }
            if(millisTimeLeft < millisTotalTime){
                bReset.setVisibility(View.VISIBLE);
            }
            else{
                bReset.setVisibility(View.INVISIBLE);
            }
        }
    }
    private void keyboard(){
        View view=this.getCurrentFocus();
        if(view!=null){
            InputMethodManager imm=(InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences prefs=getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor=prefs.edit();
        editor.putLong("startTimeInMillis", millisTotalTime);
        editor.putLong("millisLeft", millisTimeLeft);
        editor.putBoolean("timerRunning", isCounting);
        editor.putLong("endTime", timeEnd);
        editor.apply();

        if(timerCountDown !=null) {
            timerCountDown.cancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        millisTotalTime =prefs.getLong("startTimeInMillis",600000);
        millisTimeLeft =prefs.getLong("millisLeft", millisTotalTime);
        isCounting =prefs.getBoolean("timerRunning",false);
        timeTextFormat();
        visibleInterface();
        if(isCounting){
            timeEnd =prefs.getLong("endTime", 0);
            millisTimeLeft = timeEnd -System.currentTimeMillis();
            if(millisTimeLeft <0){
                millisTimeLeft =0;
                isCounting =false;
                timeTextFormat();
                visibleInterface();
            }
            else{
                startCountdown();
            }
        }
    }
}
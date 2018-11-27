package com.digitalonboarding;

/**
 * Created by DiegoCG on 12/09/17.
 */

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class Detector extends LinearLayout {


    public Detector(Context context) {
        super(context);
        init(context);
    }

    public Detector(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Detector(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    Handler mhaHandler = new Handler();
    private void init(Context context) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                new CountDownTimer(200000000, 400) {
                    boolean a = false;
                    @Override
                    public void onTick(long millisUntilFinished) {
                        mhaHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                setVisibility(a?VISIBLE:GONE);
                                a = !a;
                            }
                        });
                    }

                    @Override
                    public void onFinish() {}
                }.start();
            }
        });
    }

}
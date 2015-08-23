package net.callmeike.android.loopersandbox;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;


public class LooperTimerActivity extends AppCompatActivity {
    private static final int OP_LOOP = -1;
    private static final int OP_DISPLAY = -2;
    private static final int OP_IDLE = -3;
    private static final int DELAY_MS = 10;

    private class LoopingHandlerCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(Message msg) {
            idle(false);
            switch (msg.what) {
                case OP_LOOP:
                    loop(msg.getTarget(), msg.arg1);
                    return true;
                default:
                    return false;
            }
        }
    }

    private class UILoopingHandlerCallback extends LoopingHandlerCallback {
        private final MessageQueue q;
        public UILoopingHandlerCallback(MessageQueue q) { this.q = q; }

        @Override
        @UiThread
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case OP_IDLE:
                    displayIdleState(msg.arg1);
                    return true;
                case OP_DISPLAY:
                    calculateVariance(msg.arg1);
                    return true;
                default:
                    return super.handleMessage(msg);
            }
        }
    }


    private Menu menu;
    private TextView textView;
    private View android;
    private Animation animation;

    private Handler altHdlr;
    private Handler uiHdlr;
    private volatile boolean stopped;

    private double mean, m2;
    private long n;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_looper_timer, menu);
        this.menu = menu;
        enableMenus(true);
        return true;
    }

    @Override
    @UiThread
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_start_ui) {
            startLoop(getUiHdlr());
            return true;
        }
        if (id == R.id.action_start_alt) {
            startLoop(altHdlr);
            return true;
        }
        else if (id == R.id.action_stop) {
            stopLoop();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_looper_timer);
        textView = (TextView) findViewById(R.id.diff);
        android = findViewById(R.id.android);
        animation = AnimationUtils.loadAnimation(this, R.anim.animation);
    }

    @Override
    @UiThread
    protected void onResume() {
        super.onResume();

        HandlerThread looperThread = new HandlerThread("Alt Looper") {
            public void onLooperPrepared() {
                Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
                    @Override
                    public boolean queueIdle() {
                        idle(true);
                        return true;
                    }
                });
            }
        };
        looperThread.start();
        Looper looper = looperThread.getLooper(); // may block!
        altHdlr = new Handler(looper, new LoopingHandlerCallback());

        setUiHdlr(new Handler(new UILoopingHandlerCallback(Looper.myQueue())));

        enableMenus(true);

        android.startAnimation(animation);
    }

    @Override
    @UiThread
    protected void onPause() {
        stopLoop();

        android.clearAnimation();

        setUiHdlr(null);

        altHdlr.getLooper().quit();
        altHdlr = null;

        super.onPause();
    }

    @WorkerThread
    void idle(boolean isIdle) {
//        Handler localUiHdlr = getUiHdlr();
//        if (null == localUiHdlr) { return; }
//        localUiHdlr.obtainMessage(OP_IDLE, (isIdle) ? 0 : 1, 0).sendToTarget();
    }

    @UiThread
    private void displayIdleState(int idle) {
        textView.setBackgroundColor((0 == idle) ? Color.RED : Color.GREEN);
    }

    @UiThread
    void calculateVariance(int x) {
        n++;
        double delta = x - mean;
        mean += delta / n;
        m2 += delta * (x - mean);

        if (n % 200 > 0) { return; }

        textView.setText(String.format("%.2f", Math.sqrt(m2 / (n - 1))));
    }

    void loop(@NonNull Handler hdlr, int t) {
        if (stopped) { return; }

        doLoop(hdlr);

        int deltaT = ((int) (SystemClock.uptimeMillis() & 0x7fffffff)) - t;

        Handler localUiHdlr = getUiHdlr();
        if (null != localUiHdlr) {
            localUiHdlr.obtainMessage(OP_DISPLAY, (int) deltaT, -1).sendToTarget();
        }
    }

    synchronized Handler getUiHdlr () { return uiHdlr; }

    @UiThread
    private synchronized void setUiHdlr (@Nullable Handler hdlr) { uiHdlr = hdlr; }

    @UiThread
    private void startLoop(@NonNull Handler hdlr) {
        n = 0;
        mean = m2 = 0.0;

        enableMenus(false);
        textView.setText(null);

        stopped = false;
        doLoop(hdlr);
    }

    @UiThread
    private void stopLoop() {
        stopped = true;
        enableMenus(true);
    }

    private void doLoop(@NonNull Handler hdlr) {
        long t = SystemClock.uptimeMillis();
        Message msg = hdlr.obtainMessage(OP_LOOP, (int) (t & 0x7fffffff), -1);
        msg.setAsynchronous(true);
        hdlr.sendMessageAtTime(msg, t);
    }

    @UiThread
    private void enableMenus(boolean startable) {
        if (null == menu) { return; }
        menu.findItem(R.id.action_start_ui).setEnabled(startable);
        menu.findItem(R.id.action_start_alt).setEnabled(startable);
        menu.findItem(R.id.action_stop).setEnabled(!startable);
    }

//    public Handler mainHandler;
//    public void createLooper() {
//        Thread looperThread = new Thread() {
//            @Override
//            public void run() {
//                Looper.prepare();
//                mainHandler = new Handler();
//                Looper.loop();
//            }
//        };
//        looperThread.start();
//    }
}

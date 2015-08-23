package net.callmeike.android.loopersandbox;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import net.callmeike.android.loopersandbox.svc.PeriodicTaskScheduler;


public class MainActivity extends AppCompatActivity implements PeriodicTaskScheduler.Task {

    private final List<PeriodicTaskScheduler.TaskInfo> tasks = new ArrayList<>();
    private PeriodicTaskScheduler scheduler;
    private TextView input;

    @Override
    public void onTask() {
        Log.i("TIMER", "ping!");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        input = (TextView) findViewById(R.id.input);

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { startTimer(); }
        });

        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { stopTimer(); }
        });

        scheduler = new PeriodicTaskScheduler(getMainLooper());
    }

    private void startTimer() {
        int interval;
        try { interval = Integer.parseInt(input.getText().toString()); }
        catch (NumberFormatException e) { return; }
        tasks.add(scheduler.scheduleTask(this, interval));
    }

    private void stopTimer() {
        int n = tasks.size() - 1;
        if (n < 0) { return; }
        scheduler.stopTask(tasks.remove(n));
    }
}

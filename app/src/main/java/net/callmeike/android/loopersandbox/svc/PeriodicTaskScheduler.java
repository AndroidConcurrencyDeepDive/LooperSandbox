/* $Id: $
   Copyright 2012, G. Blake Meike

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package net.callmeike.android.loopersandbox.svc;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;


/**
 * @author <a href="mailto:blake.meike@gmail.com">G. Blake Meike</a>
 * @version $Revision: $
 */
public class PeriodicTaskScheduler {
    private static final int PERIODIC_TASK = -1230;

    /**
     * Callback interface for periodic task execution.
     */
    public interface Task { void onTask(); }

    public static final class TaskInfo {
        final long interval;
        final Task task;
        volatile boolean cancelled;
        TaskInfo(Task task, long interval) {
            this.task = task;
            this.interval = interval;
        }
    }

    private static class PeriodicTaskHandler extends Handler {
        public PeriodicTaskHandler(Looper looper) { super(looper); }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what != PERIODIC_TASK) { return; }
            long t = SystemClock.elapsedRealtime();

            // A task that runs for longer than its interval
            // can starve the rest of the queue.
            TaskInfo info = (TaskInfo) msg.obj;
            if (!info.cancelled) {
                info.task.onTask();
                scheduleTask(msg.what, info, t);
            }
        }

        public void scheduleTask(int what, TaskInfo info, long t) {
            Message msg = obtainMessage(what, info);
            sendMessageAtTime(msg, t + info.interval);
        }
    }


    private final PeriodicTaskHandler handler;

    public PeriodicTaskScheduler(Looper looper) {
        handler = new PeriodicTaskHandler(looper);
    }

    /**
     * Schedule a callback to the passed Task every interval ms.
     * The first callback will occur interval ms from now.
     * The Task will be called on the thread associated with the looper
     * used to initialize this Scheduler.
     *
     * @param task the callback
     * @param interval time between callbacks
     */
    public TaskInfo scheduleTask(Task task, long interval) {
        TaskInfo info = new TaskInfo(task, interval);
        handler.scheduleTask(PERIODIC_TASK, info, SystemClock.elapsedRealtime());
        return info;
    }

    /**
     * Stop the task associated with the passed token.
     *
     * @param info the task to stop.
     */
    public void stopTask(TaskInfo info) {
        info.cancelled = true;
    }
}

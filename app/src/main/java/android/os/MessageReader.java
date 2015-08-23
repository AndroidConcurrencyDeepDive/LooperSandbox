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
package android.os;

import android.util.Log;

import java.lang.reflect.Field;


/**
 * @author <a href="mailto:blake.meike@gmail.com">G. Blake Meike</a>
 * @version $Revision: $
 */
public class MessageReader {
    Field fieldMessages;
    Field fieldFlag;
    Field fieldWhen;
    Field fieldData;
    Field fieldTarget;
    Field fieldCallback;
    Field fieldNext;

    public static void dump(Message msg) {
        try { new MessageReader().dumpMessage(msg); }
        catch (NoSuchFieldException e) {
            Log.e("DUMP", "Fail: " + e);
        }
        catch (IllegalAccessException e) {
            Log.e("DUMP", "Fail: " + e);
        }
    }

    public static void dumpQ(MessageQueue q) {
        try { new MessageReader().dumpQueue(q); }
        catch (NoSuchFieldException e) {
            Log.e("DUMP", "Fail: " + e);
        }
        catch (IllegalAccessException e) {
            Log.e("DUMP", "Fail: " + e);
        }
    }

    private MessageReader() throws NoSuchFieldException {
        fieldMessages = MessageQueue.class.getDeclaredField("mMessages");
        fieldMessages.setAccessible(true);

        fieldFlag = Message.class.getDeclaredField("flags");
        fieldFlag.setAccessible(true);

        fieldWhen = Message.class.getDeclaredField("when");
        fieldWhen.setAccessible(true);

        fieldData = Message.class.getDeclaredField("data");
        fieldData.setAccessible(true);

        fieldTarget = Message.class.getDeclaredField("target");
        fieldTarget.setAccessible(true);

        fieldCallback = Message.class.getDeclaredField("callback");
        fieldCallback.setAccessible(true);

        fieldNext = Message.class.getDeclaredField("next");
        fieldNext.setAccessible(true);
    }


    private void dumpQueue(MessageQueue q) throws IllegalAccessException {
        Log.d("MSG", "======================== LOOP");
        Message msg = (Message) fieldMessages.get(q);
        while (null != msg) {
            dump(msg);
            msg = (Message) fieldNext.get(msg);
        }
    }

    private void dumpMessage(Message msg) throws IllegalAccessException {
        Log.d("MSG", "msg: " + msg);
        Log.d("MSG", "  what: " + msg.what);
        Log.d("MSG", "  arg1: " + msg.arg1);
        Log.d("MSG", "  arg2: " + msg.arg2);
        Log.d("MSG", "  obj: " + msg.obj);
        Log.d("MSG", "  reply: " + msg.replyTo);
        Log.d("MSG", "  sender: " + msg.sendingUid);
        Log.d("MSG", "  flags: " + fieldFlag.getInt(msg));
        Log.d("MSG", "  when: " + fieldWhen.getLong(msg));
        Log.d("MSG", "  data: " + fieldData.get(msg));
        Log.d("MSG", "  target: " + fieldTarget.get(msg));
        Log.d("MSG", "  callback: " + fieldCallback.get(msg));
        Log.d("MSG", "  next: " + fieldNext.get(msg));
    }
}

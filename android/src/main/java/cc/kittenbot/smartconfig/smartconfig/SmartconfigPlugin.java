package cc.kittenbot.smartconfig.smartconfig;

import android.util.Log;
import android.os.AsyncTask;
import android.app.Activity;
import android.content.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchListener;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.task.__IEsptouchTask;

/** SmartconfigPlugin */
public class SmartconfigPlugin implements MethodCallHandler {
  private static final String TAG = "smartconfig";

  private Activity activity;
  private MethodChannel channel;
  private IEsptouchTask mEsptouchTask;

  public SmartconfigPlugin(Activity activity,MethodChannel channel) {
    this.activity = activity;
    this.channel = channel;
  }

  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "smartconfig");
    channel.setMethodCallHandler(new SmartconfigPlugin(registrar.activity(),channel));
  }

  public void stop() {
    if (mEsptouchTask != null) {
      Log.d(TAG, "cancel task");
      mEsptouchTask.interrupt();
    }
  }

  public void start(String ssid, String bssid, String pass, final Result smResult){
    new EsptouchAsyncTask(new TaskListener() {
        @Override
        public void onFinished(List<IEsptouchResult> result) {
            // Do Something after the task has finished

            Map<String, String> ret = new HashMap<String, String>();

            Boolean resolved = false;
            for (IEsptouchResult resultInList : result) {
              if(!resultInList.isCancelled() &&resultInList.getBssid() != null) {
                ret.put(resultInList.getBssid(), resultInList.getInetAddress().getHostAddress());
                resolved = true;
                if (!resultInList.isSuc())
                  break;

              }
            }

            if(resolved) {
              Log.d(TAG, "Success run smartconfig"+ret);
              // promise.resolve(ret);
              smResult.success(ret);
            } else {
              Log.d(TAG, "Error run smartconfig");
              // promise.reject("new IllegalViewOperationException()");
              smResult.error("Fail", "Smart config fail.", null);
            }

        }
    }).execute(ssid, bssid, pass, "YES", "1");
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("start")) {
      String ssid = call.argument("ssid");
      String bssid = call.argument("bssid");
      String pass = call.argument("pass");

      Log.d(TAG, "ssid " + ssid + ":pass " + pass);
      stop();
      start(ssid, bssid, pass, result);
      // result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else {
      result.notImplemented();
    }
  }



  public interface TaskListener {
    public void onFinished(List<IEsptouchResult> result);
  }

  private class EsptouchAsyncTask extends AsyncTask<String, Void, List<IEsptouchResult>> {

    //
    // public interface TaskListener {
    //     public void onFinished(List<IEsptouchResult> result);
    // }
    private final TaskListener taskListener;

    public EsptouchAsyncTask(TaskListener listener) {
      // The listener reference is passed in through the constructor
      this.taskListener = listener;
    }


    // without the lock, if the user tap confirm and cancel quickly enough,
    // the bug will arise. the reason is follows:
    // 0. task is starting created, but not finished
    // 1. the task is cancel for the task hasn't been created, it do nothing
    // 2. task is created
    // 3. Oops, the task should be cancelled, but it is running
    private final Object mLock = new Object();

    @Override
    protected void onPreExecute() {
      Log.d(TAG, "Begin task");
    }
    @Override
    protected List<IEsptouchResult> doInBackground(String... params) {
      Log.d(TAG, "doing task");
      int taskResultCount = -1;
      synchronized (mLock) {
        String apSsid = params[0];
        String apBssid =  params[1];
        String apPassword = params[2];
        String isSsidHiddenStr = params[3];
        String taskResultCountStr = params[4];
        boolean isSsidHidden = false;
        if (isSsidHiddenStr.equals("YES")) {
          isSsidHidden = true;
        }
        taskResultCount = Integer.parseInt(taskResultCountStr);
        Context context = activity.getApplicationContext();
        mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, context);
        mEsptouchTask.setPackageBroadcast(true);

        //mEsptouchTask.setEsptouchListener(myListener);
      }
      List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
      return resultList;
    }

    @Override
    protected void onPostExecute(List<IEsptouchResult> result) {

      IEsptouchResult firstResult = result.get(0);
      // check whether the task is cancelled and no results received
      if (!firstResult.isCancelled()) {
        if(this.taskListener != null) {

          // And if it is we call the callback function on it.
          this.taskListener.onFinished(result);
        }
      }
    }
  }
}

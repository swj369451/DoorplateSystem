package com.example.doorplatesystem.hardware;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import com.example.doorplatesystem.MainActivity;
import com.example.doorplatesystem.webrtc.MediaCommunication;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 判断网络连接状况.
 * @author nagsh
 *
 */
public class DoorplateUtil {
	static String doorid = null;
    public static String getDoorplateId(Context context,String macAddress){

		String url = "https://gcc.ppamatrix.com:443/wss/appController/getDoorplateInfo?macAddress=" + macAddress;
		OkHttpClient okHttpClient = new OkHttpClient();
		final Request request = new Request.Builder()
				.url(url)
				.get()//默认就是GET请求，可以不写
				.build();
		Call call = okHttpClient.newCall(request);
		call.enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				// 使用Toast来显示异常信息
				new Thread() {
					@Override
					public void run() {
						Looper.prepare();
						Toast.makeText(context, "访问服务失败，检查网络", Toast.LENGTH_LONG).show();
						Looper.loop();
					}
				}.start();
				return;
			}
			@Override
			public void onResponse(Call call, Response response) throws IOException {
				String date = null;
				try {
					date = response.body().string();
				} catch (IOException e) {
					// 使用Toast来显示异常信息
					new Thread() {
						@Override
						public void run() {
							Looper.prepare();
							Toast.makeText(context, "请求错误", Toast.LENGTH_LONG).show();
							Looper.loop();
						}
					}.start();
					return;
				}
				System.out.println(date);
				try {
					//String转JSONObject
					JSONObject result = new JSONObject(date);

					JSONObject deviceInfojson = result.getJSONObject("deviceInfo");
					//取数据
					String deviceId = deviceInfojson.getString("deviceId");

					// 使用Toast来显示异常信息
					new Thread() {
						@Override
						public void run() {
							Looper.prepare();
							Toast.makeText(context, "设备id获取成功，id为："+deviceId, Toast.LENGTH_LONG).show();
							MediaCommunication mediaCommunication = new MediaCommunication(context);
							mediaCommunication.init(deviceId);

							Looper.loop();
						}
					}.start();

					MainActivity.doorplateId = deviceId;
					MainActivity.startInit();
				} catch (JSONException e) {
					// 使用Toast来显示异常信息
					new Thread() {
						@Override
						public void run() {
							Looper.prepare();
							Toast.makeText(context, "请求错误", Toast.LENGTH_LONG).show();
							Looper.loop();
						}
					}.start();
				}
			}
		});
        return doorid;
    }

}
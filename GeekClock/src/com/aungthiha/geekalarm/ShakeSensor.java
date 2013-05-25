package com.aungthiha.geekalarm;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;
import android.util.Log;

public class ShakeSensor extends Activity implements SensorEventListener {
	
	private static final String TAG = "shakeSensor";
	
	private static final int INTERVAL = 200;
	private static final String DEFAULT_SNOOZE_SHAKE_COUNT = "5";
	private static final String DEFAULT_DISMISS_SHAKE_COUNT = "20";
	private static final long VIBERATE_TIME = 45;
	private SensorManager sensorManager;
	private TextView textview;
	private long lastUpdate;
	private int count = 0;
	private boolean running = false;
	private Vibrator shakeViberator;
	private int snoozeShakeCount;
	private int dismissShakeCount;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		Log.d(TAG, "on create");
		
		View rootView = getWindow().getDecorView();
		rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor);
		textview = (TextView) findViewById(R.id.shakeTextView);

		shakeViberator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

		sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		lastUpdate = System.currentTimeMillis();

		String snoozeCount = PreferenceManager
				.getDefaultSharedPreferences(this).getString(
						SettingsActivity.KEY_SNOOZE_SHAKE_TIMES,
						DEFAULT_SNOOZE_SHAKE_COUNT);
		snoozeShakeCount = Integer.parseInt(snoozeCount);

		String dismissCount = PreferenceManager.getDefaultSharedPreferences(
				this).getString(SettingsActivity.KEY_DISMISS_SHAKE_TIMES,
				DEFAULT_DISMISS_SHAKE_COUNT);
		dismissShakeCount = Integer.parseInt(dismissCount);
		textview.setText(String.format(
				"Shake %d times to Snooze %n %n   %d times to Dismiss ",
				snoozeShakeCount, dismissShakeCount));

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			getAccelerometer(event);
		}

	}

	private synchronized void getAccelerometer(SensorEvent event) {
		float[] values = event.values;

		float x = values[0];
		float y = values[1];
		float z = values[2];

		float accelationSquareRoot = (x * x + y * y + z * z)
				/ (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
		long actualTime = System.currentTimeMillis();
		if (accelationSquareRoot >= 5) //
		{
			if (actualTime - lastUpdate < INTERVAL) {
				return;
			}
			lastUpdate = actualTime;
			count++;

			shakeViberator.vibrate(VIBERATE_TIME);
			textview.setText("" + count);
			textview.setTextSize(40);

			if (count == snoozeShakeCount) {
				broadcastSnooze();
				dismissTest();

			} else if (count >= dismissShakeCount) {
				broadcastDismiss();
				dismissTest();
			}

		}
	}

	private synchronized void dismissTest() {

		new Thread() {

			@Override
			public void run() {
				running = true;
				try {
					while (running) {
						Thread.sleep(8 * INTERVAL);
						long actualTime = System.currentTimeMillis();
						if (actualTime - lastUpdate > 3 * INTERVAL) {

							endActivity();
						}
					}
				} catch (InterruptedException e) {

					e.printStackTrace();
				}

			}

		}.start();
	}

	private void broadcastSnooze() {
		Intent snoozeIntent = new Intent(Alarms.ALARM_SNOOZE_ACTION);
		sendBroadcast(snoozeIntent);

	}

	private void broadcastDismiss() {
		Intent dismissIntent = new Intent(Alarms.ALARM_DISMISS_ACTION);
		sendBroadcast(dismissIntent);

	}

	private void endActivity() {
		onPause();
		finish();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	protected void onResume() {
		super.onResume();

		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {

		running = false;
		super.onPause();
		sensorManager.unregisterListener(this);
	}
}

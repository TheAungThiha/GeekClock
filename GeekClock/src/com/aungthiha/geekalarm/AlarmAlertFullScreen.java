/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aungthiha.geekalarm;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;

import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;
import android.util.Log;

/**
 * Alarm Clock alarm alert: pops visible indicator and plays alarm tone. This
 * activity is the full screen version which shows over the lock screen with the
 * wallpaper as the background.
 */
public class AlarmAlertFullScreen extends Activity {

	// These defaults must match the values in res/xml/settings.xml
	private static final String DEFAULT_SNOOZE = "1";
	private static final String DEFAULT_VOLUME_BEHAVIOR = "2";
	protected static final String SCREEN_OFF = "screen_off";
	private static final int MAX_WRONG_COUNT = 3;
	private static final String TAG = "Alarmalartfullscreen";
	private static final String SAVED_ID = "last_id";

	protected Alarm mAlarm;
	private int mVolumeBehavior;
	boolean mFullscreenStyle;
	String answer;
	int wrongCount = 0;
	QuestionData questionData;
	long id = -1;
	boolean newQuestionFlag = false;
	TextView answerView;
	boolean ansDisable = false;
	// Receives the ALARM_KILLED action from the AlarmKlaxon,
	// and also ALARM_SNOOZE_ACTION / ALARM_DISMISS_ACTION from other
	// applications
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Alarms.ALARM_SNOOZE_ACTION)) {
				snooze();
			} else if (action.equals(Alarms.ALARM_DISMISS_ACTION)) {
				dismiss(false);
			} else {
				Alarm alarm = intent
						.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
				if (alarm != null && mAlarm.id == alarm.id) {
					dismiss(true);
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(TAG, "oncreate");
		if (savedInstanceState != null) {
			Log.d(TAG, "not null");
			if (savedInstanceState.containsKey(SAVED_ID)) {
				id = savedInstanceState.getInt(SAVED_ID);
				Log.d(TAG, "contains id  " + id);
			}
		} else {

			try {
				questionData = new QuestionData(getApplicationContext());
			} catch (IOException e) {

				e.printStackTrace();
			}

			Cursor cursor = questionData.query();
			cursor.moveToFirst();
			id = cursor.getLong(cursor.getColumnIndex(QuestionData.D_ID));
			Log.d(TAG, "new id " + id);
			cursor.close();
			;
		}

		mAlarm = getIntent().getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);

		// Get the volume/camera button behavior setting
		final String vol = PreferenceManager.getDefaultSharedPreferences(this)
				.getString(SettingsActivity.KEY_VOLUME_BEHAVIOR,
						DEFAULT_VOLUME_BEHAVIOR);
		mVolumeBehavior = Integer.parseInt(vol);

		final Window win = getWindow();
		win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		// Turn on the screen unless we are being launched from the AlarmAlert
		// subclass as a result of the screen turning off.
		if (!getIntent().getBooleanExtra(SCREEN_OFF, false)) {
			win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
					| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
					| WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
		}

		updateLayout();

		// Register to get the alarm killed/snooze/dismiss intent.
		IntentFilter filter = new IntentFilter(Alarms.ALARM_KILLED);
		filter.addAction(Alarms.ALARM_SNOOZE_ACTION);
		filter.addAction(Alarms.ALARM_DISMISS_ACTION);
		registerReceiver(mReceiver, filter);
	}

	private void setTitle() {
		final String titleText = mAlarm.getLabelOrDefault(this);

		TextView tv = (TextView) findViewById(R.id.alertTitle);
		tv.setText(titleText);

		setTitle(titleText);
	}

	protected int getLayoutResId() {
		return R.layout.alarm_alert_fullscreen;
	}

	private void updateLayout() {

		Log.d(TAG, " in updateLayout");
		LayoutInflater inflater = LayoutInflater.from(this);

		setContentView(inflater.inflate(getLayoutResId(), null));

		findViewById(R.id.topPanel).setVisibility(View.GONE);

		if (questionData == null) {
			try {
				questionData = new QuestionData(this);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		Cursor cursor = null;
		if (id == -1 || newQuestionFlag) {
			cursor = questionData.query();

		} else {
			cursor = questionData.query(id);
		}

		cursor.moveToFirst();
		id = cursor.getLong(cursor.getColumnIndex(QuestionData.D_ID));

		newQuestionFlag = false;

		String questionText = cursor.getString(cursor
				.getColumnIndex(QuestionData.QUESTION));
		answer = cursor.getString(cursor.getColumnIndex(QuestionData.ANSWER));
		String one = cursor.getString(cursor
				.getColumnIndex(QuestionData.CHOICE_1));
		String two = cursor.getString(cursor
				.getColumnIndex(QuestionData.CHOICE_2));
		String three = cursor.getString(cursor
				.getColumnIndex(QuestionData.CHOICE_3));
		String four = cursor.getString(cursor
				.getColumnIndex(QuestionData.CHOICE_4));
		String five = cursor.getString(cursor
				.getColumnIndex(QuestionData.CHOICE_5));
		String solution = cursor.getString(cursor
				.getColumnIndex(QuestionData.SOLUTION));
		cursor.close();
		questionData.db.close();

		TextView question = (TextView) findViewById(R.id.question);
		question.setText(questionText);

		/*
		 * snooze behavior: pop a snooze confirmation view, kick alarm manager.
		 */
		Button snooze = (Button) findViewById(R.id.snooze);
		snooze.requestFocus();

		((Button) findViewById(R.id.ch1)).setText(one);
		((Button) findViewById(R.id.ch2)).setText(two);
		((Button) findViewById(R.id.ch3)).setText(three);
		((Button) findViewById(R.id.ch4)).setText(four);
		((Button) findViewById(R.id.ch5)).setText(five);

		answerView = (TextView) findViewById(R.id.answer_view);
		answerView.setVisibility(View.GONE);
		answerView.setText(solution);
	}

	private void checkAnswer(View v) {
		String str = ((Button) v).getText().toString();
		if (ansDisable) {
			Toast.makeText(AlarmAlertFullScreen.this, "Try another Question",
					Toast.LENGTH_LONG).show();
			return;
		} else if (str.equals(answer)) {
			dismiss(false);
		} else {
			Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
			v.startAnimation(shake);
			v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
			wrongCount++;
			if (wrongCount >= MAX_WRONG_COUNT) {
				wrongCount = 0;
				findViewById(R.id.topPanel).setVisibility(View.GONE);
				showAns();
				Toast.makeText(
						AlarmAlertFullScreen.this,
						MAX_WRONG_COUNT
								+ " mistakes, You must try another Question ",
						Toast.LENGTH_LONG).show();
			}
		}

	}

	public void onClick(View v) {
		checkAnswer(v);
	}

	public void actionButton(View v) {
		if (v.getId() == R.id.snooze) {
			startActivity(new Intent(this, ShakeSensor.class));
		} else if (v.getId() == R.id.show_answer) {
			showAns();

		}
	}

	/*
	 * check the text in the button , show answer , otherwise next question.
	 */

	private void showAns() {
		ansDisable = true;
		wrongCount = 0;
		Button button = (Button) findViewById(R.id.show_answer);
		String currentText = (String) button.getText();
		if (currentText.equals("Show Answer")) {
			answerView.setVisibility(View.VISIBLE);
			button.setText("Next Question");
		} else if (currentText.equals("Next Question")) {
			nextQuestion();
		}

	}

	private void nextQuestion() {
		newQuestionFlag = true;
		ansDisable = false;
		Log.d(TAG, "next question flag TRUE");
		updateLayout();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

		outState.putInt(SAVED_ID, (int) id);

		Log.d(TAG, "saved id");
		super.onSaveInstanceState(outState);
	}

	// Attempt to snooze this alert.
	private void snooze() {

		// Do not snooze if the snooze button is disabled.
		if (!findViewById(R.id.snooze).isEnabled()) {
			dismiss(false);
			return;
		}
		final String snooze = PreferenceManager.getDefaultSharedPreferences(
				this).getString(SettingsActivity.KEY_ALARM_SNOOZE,
				DEFAULT_SNOOZE);
		int snoozeMinutes = Integer.parseInt(snooze);

		final long snoozeTime = System.currentTimeMillis()
				+ (1000 * 60 * snoozeMinutes);
		Alarms.saveSnoozeAlert(AlarmAlertFullScreen.this, mAlarm.id, snoozeTime);

		// Get the display time for the snooze and update the notification.
		final Calendar c = Calendar.getInstance();
		c.setTimeInMillis(snoozeTime);

		// Append (snoozed) to the label.
		String label = mAlarm.getLabelOrDefault(this);
		label = getString(R.string.alarm_notify_snooze_label, label);

		// Notify the user that the alarm has been snoozed.
		Intent cancelSnooze = new Intent(this, AlarmReceiver.class);
		cancelSnooze.setAction(Alarms.CANCEL_SNOOZE);
		cancelSnooze.putExtra(Alarms.ALARM_INTENT_EXTRA, mAlarm);
		PendingIntent broadcast = PendingIntent.getBroadcast(this, mAlarm.id,
				cancelSnooze, 0);
		NotificationManager nm = getNotificationManager();
		Notification n = new Notification(R.drawable.stat_notify_alarm, label,
				0);
		n.setLatestEventInfo(
				this,
				label,
				getString(R.string.alarm_notify_snooze_text,
						Alarms.formatTime(this, c)), broadcast);
		n.flags |= Notification.FLAG_AUTO_CANCEL
				| Notification.FLAG_ONGOING_EVENT;
		nm.notify(mAlarm.id, n);

		String displayTime = getString(R.string.alarm_alert_snooze_set,
				snoozeMinutes);
		// Intentionally log the snooze time for debugging.
		Log.d(TAG, "snooze");

		// Display the snooze minutes in a toast.
		Toast.makeText(AlarmAlertFullScreen.this, displayTime,
				Toast.LENGTH_LONG).show();
		stopService(new Intent(Alarms.ALARM_ALERT_ACTION));

	}

	private NotificationManager getNotificationManager() {
		return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	// Dismiss the alarm.
	private void dismiss(boolean killed) {
		// Log.i(killed ? "Alarm killed" : "Alarm dismissed by user");
		// The service told us that the alarm has been killed, do not modify
		// the notification or stop the service.
		if (!killed) {
			// Cancel the notification and stop playing the alarm
			NotificationManager nm = getNotificationManager();
			nm.cancel(mAlarm.id);
			stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
		}
		Intent cancelSnooze = new Intent(this, AlarmReceiver.class);
		cancelSnooze.setAction(Alarms.CANCEL_SNOOZE);
		cancelSnooze.putExtra(Alarms.ALARM_INTENT_EXTRA, mAlarm);
		sendBroadcast(cancelSnooze);
		finish();
	}

	/**
	 * this is called when a second alarm is triggered while a previous alert
	 * window is still active.
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		// if (Log.LOGV)
		// Log.v("AlarmAlert.OnNewIntent()");

		mAlarm = intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);

		setTitle();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// If the alarm was deleted at some point, disable snooze.
		if (Alarms.getAlarm(getContentResolver(), mAlarm.id) == null) {
			Button snooze = (Button) findViewById(R.id.snooze);
			snooze.setEnabled(false);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// if (Log.LOGV)
		// Log.v("AlarmAlert.onDestroy()");
		// No longer care about the alarm being killed.
		unregisterReceiver(mReceiver);
	}

	/*
	 * @Override public boolean dispatchKeyEvent(KeyEvent event) { // Do this on
	 * key down to handle a few of the system keys. boolean up =
	 * event.getAction() == KeyEvent.ACTION_UP; switch (event.getKeyCode()) { //
	 * Volume keys and camera keys dismiss the alarm case
	 * KeyEvent.KEYCODE_VOLUME_UP: case KeyEvent.KEYCODE_VOLUME_DOWN: case
	 * KeyEvent.KEYCODE_VOLUME_MUTE: case KeyEvent.KEYCODE_CAMERA: case
	 * KeyEvent.KEYCODE_FOCUS: if (up) { switch (mVolumeBehavior) { case 1:
	 * snooze(); break;
	 * 
	 * case 2: dismiss(false); break;
	 * 
	 * default: break; } } return true; default: break; } return
	 * super.dispatchKeyEvent(event); }
	 */

	@Override
	public void onBackPressed() {
		// Don't allow back to dismiss. This method is overriden by AlarmAlert
		// so that the dialog is dismissed.
		return;
	}

}

package com.aungthiha.geekalarm;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class GeekClock extends Activity {
	private TextView mDate;
	private TextView mNextAlarm;
	private String mDateFormat;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		View rootView = getWindow().getDecorView();
		rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

		setContentView(R.layout.geek_clock_view);

	}

	@Override
	protected void onResume() {

		super.onResume();
		refreshDate();
		refreshAlarm();
	}

	public void onClick(View v) {
		startActivity(new Intent(this, AlarmClock.class));
	}

	private void refreshDate() {
		mDate = (TextView) findViewById(R.id.alarm_date);

		final Date now = new Date();

		mDateFormat = getString(R.string.full_wday_month_day_no_year);
		mDate.setText(DateFormat.format(mDateFormat, now));
	}

	private void refreshAlarm() {
		if (mNextAlarm == null) {
			mNextAlarm = (TextView) findViewById(R.id.nextAlarm);
		}

		String nextAlarm = Settings.System.getString(getContentResolver(),
				Settings.System.NEXT_ALARM_FORMATTED);
		if (!TextUtils.isEmpty(nextAlarm)) {
			mNextAlarm.setText(getString(
					R.string.control_set_alarm_with_existing, nextAlarm));
			mNextAlarm.setVisibility(View.VISIBLE);

		} else {
			mNextAlarm.setText(R.string.control_set_alarm);

		}
	}
}

package otang.network.service;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.net.TrafficStats;
import android.os.Build;
import android.os.IBinder;
import android.content.Intent;
import android.app.Service;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.lifecycle.MutableLiveData;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import otang.network.R;
import otang.network.database.DatabaseHelper;
import otang.network.database.Usage;
import otang.network.model.Speed;
import otang.network.ui.activity.MainActivity;
import otang.network.util.AppUtils;
import otang.network.util.ImageUtils;
import otang.network.util.PrefUtils;
import otang.network.util.TrafficUtils;

public class TrafficService extends Service {

	public static String CHANNEL_ID = "Channel-01";
	public static String CHANNEL_NAME = "Notifikasi";
	public static int NOTIFICATION_ID = 1;
	private long mLastRxBytes = 0;
	private long mLastTxBytes = 0;
	private long mLastTime = 0;
	private Speed mSpeed;
	private Timer timer = new Timer();
	private DatabaseHelper helper;
	private PrefUtils prefUtils;
	private NotificationChannel notificationChannel;
	private NotificationCompat.Builder builder;
	private NotificationManager notificationManager;
	private String mSpeedToShow = "total";
	private String mSpeedToShow1 = "down";
	private String mSpeedToShow2 = "up";

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		init();
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		init();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				long currentRxBytes = TrafficStats.getTotalRxBytes();
				long currentTxBytes = TrafficStats.getTotalTxBytes();
				long currentTime = System.currentTimeMillis();
				long usedRxBytes = currentRxBytes - mLastRxBytes;
				long usedTxBytes = currentTxBytes - mLastTxBytes;
				long usedTime = currentTime - mLastTime;
				mLastRxBytes = currentRxBytes;
				mLastTxBytes = currentTxBytes;
				mLastTime = currentTime;
				mSpeed.calcSpeed(usedTime, usedRxBytes, usedTxBytes);
				String[] speed = TrafficUtils.getNetworkSpeed();
				saveToDatabase(Long.valueOf(speed[0]));
				Usage usage = helper.getTodayUsage(AppUtils.getDate());
				updateNotification(mSpeed, usage.total);
			}
		}, 0, 1000);
	}

	private void init() {
		prefUtils = new PrefUtils(this);
		helper = DatabaseHelper.getInstance(this);
		mSpeed = new Speed(this);
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
		//Create channel
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			if (notificationManager != null) {
				notificationManager.createNotificationChannel(notificationChannel);
			}
		}
		builder = new NotificationCompat.Builder(this, CHANNEL_ID);
		builder.setSmallIcon(IconCompat.createWithBitmap(ImageUtils.createBitmapFromString("0", "KB/s")));
		//Title
		builder.setContentTitle(getString(R.string.app_name));
		// Notification content
		builder.setContentText("Network traffic");
		//High priority
		builder.setPriority(NotificationCompat.PRIORITY_HIGH);
		//Pattern
		long[] pattern = { 0, 100, 200, 300 };
		builder.setVibrate(pattern);
		builder.setContentIntent(createPendingIntent());
		builder.setAutoCancel(true);
		builder.setOngoing(false);
		builder.setOnlyAlertOnce(true);
		startForeground(NOTIFICATION_ID, builder.build());
		//for reset
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 00);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.SECOND, 00);
		Intent intent = new Intent(this, TrafficService.class);
		PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,
				pIntent);
	}

	private void saveToDatabase(long bytes) {
		Usage usage = new Usage();
		usage.day = AppUtils.getDate();
		long mobile, wifi, total;
		if (AppUtils.isWifiConnection(this)) {
			mobile = prefUtils.getLong("mobile");
			wifi = bytes + prefUtils.getLong("wifi");
			prefUtils.saveAs("wifi", wifi);
			total = mobile + wifi;
			prefUtils.saveAs("total", total);
		} else {
			mobile = bytes + prefUtils.getLong("mobile");
			prefUtils.saveAs("mobile", mobile);
			wifi = prefUtils.getLong("wifi");
			total = mobile + wifi;
			prefUtils.saveAs("total", total);
		}
		usage.mobile = mobile;
		usage.wifi = wifi;
		usage.total = total;
		helper.addOrUpdateUser(usage);
	}

	private void updateNotification(Speed speed, long usage) {
		Speed.HumanSpeed speedToShow = speed.getHumanSpeed(mSpeedToShow);
		Bitmap bitmap = ImageUtils.createBitmapFromString(speedToShow.speedValue, speedToShow.speedUnit);
		IconCompat iconCompat = IconCompat.createWithBitmap(bitmap);
		builder.setSmallIcon(iconCompat);
		builder.setContentTitle("Today Usage : " + TrafficUtils.getMetricData(usage));
		Speed.HumanSpeed speedToShow1 = speed.getHumanSpeed(mSpeedToShow1);
		Speed.HumanSpeed speedToShow2 = speed.getHumanSpeed(mSpeedToShow2);
		builder.setContentText("Download : " + speedToShow1.speedValue + " " + speedToShow1.speedUnit + "  -  "
				+ "Upload : " + speedToShow2.speedValue + " " + speedToShow2.speedUnit);
		notificationManager.notify(NOTIFICATION_ID, builder.build());
	}

	private PendingIntent createPendingIntent() {
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
	}

}
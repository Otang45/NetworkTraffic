package otang.network.util;

import android.content.Context;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class TrafficUtils {

	public static final long GB = 1000000000;
	public static final long MB = 1000000;
	public static final long KB = 1000;

	public static String getDownloadSpeed() {
		String speed, unit;
		Long bytesPrevious = TrafficStats.getTotalRxBytes();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Long bytesCurrent = TrafficStats.getTotalRxBytes();
		long networkSpeed = bytesCurrent - bytesPrevious;
		float downSpeed;
		if (networkSpeed >= GB) {
			downSpeed = networkSpeed / GB;
			unit = "GB/s";
		} else if (networkSpeed >= MB) {
			downSpeed = networkSpeed / MB;
			unit = "MB/s";
		} else {
			downSpeed = networkSpeed / KB;
			unit = "KB/s";
		}
		speed = String.format("%.1f", downSpeed);
		return speed + unit;
	}

	public static String getUploadSpeed() {
		String speed, unit;
		Long bytesPrevious = TrafficStats.getTotalTxBytes();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Long bytesCurrent = TrafficStats.getTotalTxBytes();
		long networkSpeed = bytesCurrent - bytesPrevious;
		float upSpeed;
		if (networkSpeed >= GB) {
			upSpeed = networkSpeed / GB;
			unit = "GB/s";
		} else if (networkSpeed >= MB) {
			upSpeed = networkSpeed / MB;
			unit = "MB/s";
		} else {
			upSpeed = networkSpeed / KB;
			unit = "KB/s";
		}
		speed = String.format("%.1f", upSpeed);
		return speed + unit;
	}

	public static String[] getNetworkSpeed() {
		String downloadSpeedOutput = "";
		String unit;
		Long bytesPrevious = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Long bytesCurrent = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
		long networkSpeed = bytesCurrent - bytesPrevious;
		if (networkSpeed >= GB) {
			unit = "GB/s";
		} else if (networkSpeed >= MB) {
			unit = "MB/s";
		} else {
			unit = "KB/s";
		}
		downloadSpeedOutput = String.valueOf(networkSpeed);
		String[] result = new String[] { downloadSpeedOutput, unit };
		return result;
	}

	public static long convertToBytes(float value, String unit) {
		if (unit == "GB/s") {
			return (long) value * GB;
		} else if (unit == "MB/s") {
			return (long) value * MB;
		} else if (unit == "KB/s") {
			return (long) value * KB;
		}
		return 0;
	}

	public static String getMetricData(long bytes) {
		float dataWithDecimal;
		String units;
		if (bytes >= GB) {
			dataWithDecimal = (float) bytes / GB;
			units = " GB";
		} else if (bytes >= MB) {
			dataWithDecimal = (float) bytes / MB;
			units = " MB";
		} else {
			dataWithDecimal = (float) bytes / KB;
			units = " KB";
		}
		return String.format("%.1f", dataWithDecimal) + units;
	}
}
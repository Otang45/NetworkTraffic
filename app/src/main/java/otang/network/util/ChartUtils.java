package otang.network.util;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import otang.network.database.Usage;

public class ChartUtils {

	public static List<BarEntry> getDailyMobileEntry(List<Usage> list) {
		List<BarEntry> entries = new ArrayList<>();
		float xEntry = 0.0f;
		Collections.reverse(list);
		for (Usage usage : list) {
			BarEntry entry = new BarEntry(xEntry, (float) usage.mobile);
			xEntry += 1;
			entries.add(entry);
		}
		return entries;
	}

	public static List<BarEntry> getDailyWifiEntry(List<Usage> list) {
		List<BarEntry> entries = new ArrayList<>();
		float xEntry = 0.0f;
		Collections.reverse(list);
		for (Usage usage : list) {
			BarEntry entry = new BarEntry(xEntry, (float) usage.wifi);
			xEntry += 1;
			entries.add(entry);
		}
		return entries;
	}
}
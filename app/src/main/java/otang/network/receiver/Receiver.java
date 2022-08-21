package otang.network.receiver;

import android.content.Intent;
import android.content.Context;
import android.content.BroadcastReceiver;
import otang.network.service.TrafficService;

public class Receiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		context.startService(new Intent(context, TrafficService.class));
	}

}
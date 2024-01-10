package otang.network.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import otang.network.ui.fragment.SpeedFragment;
import otang.network.ui.fragment.UsageFragment;

public class ViewAdapter extends FragmentStateAdapter {

	public ViewAdapter(FragmentManager manager, Lifecycle lifecycle) {
		super(manager, lifecycle);
	}

	@Override
	public int getItemCount() {
		return 2;
	}

	@Override
	public Fragment createFragment(int position) {
		switch (position) {
		case 0:
			return new SpeedFragment();
		case 1:
			return new UsageFragment();
		}
		return null;
	}

}
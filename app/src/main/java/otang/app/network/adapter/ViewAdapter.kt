package otang.app.network.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import otang.app.network.ui.fragment.SpeedFragment
import otang.app.network.ui.fragment.UsageFragment

class ViewAdapter(manager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(manager, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return SpeedFragment()
            1 -> return UsageFragment()
        }
        return Fragment()
    }
}
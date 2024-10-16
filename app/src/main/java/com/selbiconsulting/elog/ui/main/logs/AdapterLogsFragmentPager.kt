package com.selbiconsulting.elog.ui.main.logs

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.selbiconsulting.elog.ui.main.dvir_page.FragmentDvirPage
import com.selbiconsulting.elog.ui.main.logs_page.FragmentLogsPage
import com.selbiconsulting.elog.ui.main.main_log_page.FragmentMainLogsPage

class AdapterLogsFragmentPager(
    fm: FragmentManager,
    lc: Lifecycle,
    private val tabs: List<String>
) : FragmentStateAdapter(fm, lc) {
    override fun getItemCount(): Int = tabs.size

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentLogsPage()
            1 -> FragmentMainLogsPage()
            2 -> FragmentDvirPage()
            else -> FragmentLogsPage()
        }
    }
}

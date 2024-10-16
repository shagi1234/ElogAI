package com.selbiconsulting.elog.ui.main.defects

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.selbiconsulting.elog.data.model.enums.DefectsType
import com.selbiconsulting.elog.ui.main.defects_list.FragmentDefectsListPage

class AdapterDefectsFragmentPager(
    fm: FragmentManager,
    lc: Lifecycle,
    private val tabs: List<String>,
    private val context: Context
) : FragmentStateAdapter(fm, lc) {
    override fun getItemCount(): Int = tabs.size
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentDefectsListPage.newInstance(context, DefectsType.UnitDefect)
            1 -> FragmentDefectsListPage.newInstance(context, DefectsType.TrailerDefect)

            else -> FragmentDefectsListPage.newInstance(context, DefectsType.UnitDefect)
        }
    }
}

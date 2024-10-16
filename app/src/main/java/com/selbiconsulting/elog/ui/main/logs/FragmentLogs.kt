package com.selbiconsulting.elog.ui.main.logs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.dto.DtoDate
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.databinding.FragmentLogsBinding
import com.selbiconsulting.elog.ui.main.logs_page.AdapterDateLogs
import com.selbiconsulting.elog.ui.main.logs_page.DateItemListener
import com.selbiconsulting.elog.ui.util.SharedViewModel
import com.selbiconsulting.elog.ui.util.UiHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class FragmentLogs : Fragment(), DateItemListener {
    private lateinit var binding: FragmentLogsBinding
    private var adapterLogsFragmentPager: AdapterLogsFragmentPager? = null
    private lateinit var adapterDate: AdapterDateLogs
    private val viewModelLogs: ViewModelLogs by activityViewModels()
    private val sharedViewModel by activityViewModels<SharedViewModel>()

    @Inject
    lateinit var sharedPref: SharedPreferencesHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentLogsBinding.inflate(inflater, container, false)

        getDates()
        setViewPager()
        setRecyclerDates()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observe()
    }

    private fun observe() {
        lifecycleScope.launch {
            viewModelLogs.daysIsExistLog.collect { days ->
                days.sortBy { it.formattedDate }

                adapterDate.dates = days
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            viewModelLogs.selectedDate.collect { updatedDate ->
                if (updatedDate.position < viewModelLogs.daysIsExistLog.value.size &&
                    updatedDate.isCertified
                ) {
                    viewModelLogs.daysIsExistLog.value[updatedDate.position].isCertified = true
                    sharedPref.certifiedDate = updatedDate.formattedDate

                    withContext(Dispatchers.Main) {
                        adapterDate.notifyItemChanged(updatedDate.position)
                    }
                }

            }
        }


    }

    private fun getDates() {
        lifecycleScope.launch {
            val dates = mutableListOf<DtoDate>()

            // Get today's date
            val today = LocalDate.now()
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            // Formatter for day of the week
            val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)

            // Formatter for day of the month
            val dayOfMonthFormatter = DateTimeFormatter.ofPattern("MMM d")

            // Get the last 8 days
            for (i in 0 until 8) {
                val date = today.minusDays(i.toLong())
                val dayOfWeek = date.format(dayOfWeekFormatter)
                val dayOfMonth = date.format(dayOfMonthFormatter)
                val dateReal = date.format(dateFormatter)

                dates.add(
                    DtoDate(
                        dayOfWeek,
                        dayOfMonth,
                        dateReal,
                        position = i,
                        isCertified = viewModelLogs.checkDailyLogsIsCertified(dateReal)
                    )
                )
            }

            viewModelLogs.checkThisDateExistLogs(dates.reversed())
        }
    }

    private fun setViewPager() {
        addDividerToTab()

        val tabs = resources.getStringArray(R.array.log_tabs).toList()

        adapterLogsFragmentPager = AdapterLogsFragmentPager(childFragmentManager, lifecycle, tabs)
        binding.viewPager.adapter = adapterLogsFragmentPager
        val tabLayoutMediator = TabLayoutMediator(
            binding.tabLayout,
            binding.viewPager,
            true,
            false
        ) { tab, position -> tab.text = tabs[position] }

        tabLayoutMediator.attach()

        sharedViewModel.isDvirCreated.observe(viewLifecycleOwner) {
            if (it) {
                binding.viewPager.setCurrentItem(2, false)
                sharedViewModel.isDvirCreated.value = false
            }
        }

    }

    private fun addDividerToTab() {
        val root: LinearLayout = binding.tabLayout.getChildAt(0) as LinearLayout
        root.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.tab_divider)
        root.dividerPadding = UiHelper(requireContext()).dpToPx(12)
        root.dividerDrawable = drawable
    }

    private fun setRecyclerDates() {
        adapterDate = AdapterDateLogs(requireContext(), this@FragmentLogs)
        binding.rvDate.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = adapterDate

            if (adapterDate.itemCount == 0) return
            scrollToPosition(adapterDate.itemCount - 1)
        }

    }

    override fun onDateClicked(date: DtoDate) {
        viewModelLogs.setDate(date)
    }
}
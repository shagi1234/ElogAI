package com.selbiconsulting.elog.ui.util

class Const {
    companion object {
        const val fuel = "Fuel"
        const val notePTI = "PTI"
        const val isDrivingTimeReset = "true"
        const val divider: Long = 60
        const val dotInspection = "Dot Inspection"
        const val ymNote = "Change status from YM to ON DUTY"
        const val pcNote = "Change status from PC to OFF DUTY"

        const val lessThan30Mins: Int =
            30 //seconds last warning cycles (shift,break,drive) 30 min actually
        const val driveTime = "11:00"//total drive time 11:00 actually
        const val breakTime = "08:00"//total break time 8:00 actually
        const val shiftTime = "14:00"//total shift time 14:00 actually
        const val resetTime = "10:00"//reset time actually 10:00 off duty
        const val smallBreakTime = "30:00"//smallBreak actually 30 mins
        const val minute15InMillis: Long = 15 * 1000 * 60 // 15 min
        const val ymAndPcStatusDurationsInMillis: Long = 5 * 1000 * 60


        const val PTI_15_MINUTE: Long = 15
        const val WEEKLY_HOURS_IN_MINUTES = 70 * 60

    }
}
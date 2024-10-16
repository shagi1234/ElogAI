
import com.selbiconsulting.elog.ui.main.common.ToastStates

enum class ViolationType(val message: String, val state: ToastStates, val serverName: String ="") {
    NON_VIOLATION(
        message = "Everything is on the way. Keep moving!",
        state = ToastStates.SUCCESS
    ),
    PTI(
        message = "PTI less than 15 minutes",
        state = ToastStates.ERROR,
        serverName = "pti"
    ),
    WARNING_PTI(
        message = "PTI should be 15 minute long",
        state = ToastStates.WARNING,
        serverName = "warningPti"
    ),

    DRIVING_WITHOUT_30_MINUTE_BREAK(
        message = "Driving for 8 hours or more, without a 30-minute break",
        state = ToastStates.ERROR,
        serverName = "drivingWithout30MinuteBreak"
    ),
    ON_DUTY_WITHOUT_UNINTERRUPTED_BREAK(
        message = "Driving more than 11 hours within a 14-hour shift without a full, uninterrupted 10-hour break",
        state = ToastStates.ERROR,
        serverName = "ondutyWithoutUninterruptedBreak"
    ),

    DRIVING_EXCEEDS_11_HOURS(
        message = "Driving beyond the limit set by hours driven out of the 11-hour limit",
        state = ToastStates.ERROR,
        serverName = "drivingExceeds11Hours"
    ),
    WARNING_30_MINUTE_BEFORE_DRIVING_ENDS(
        message = "Only 30 minute Driving time left",
        state = ToastStates.WARNING,
        serverName = "warning30MinuteBeforeDrivingEnds"
    ),
    WARNING_30_MINUTE_BEFORE_SHIFT_ENDS(
        message = "Only 30 minute Shift time left",
        state = ToastStates.WARNING,
        serverName = "warning30MinuteBeforeShiftEnds"
    ),
    WARNING_30_MINUTE_BEFORE_BREAK_ENDS(
        message = "You need to make 30 minute Break",
        state = ToastStates.WARNING,
        serverName = "warning30MinuteBeforeBreakEnds"
    ),

    EXCEEDED_70_HOUR_SHIFT(
        message = "Exceeded 70 hour shift without uninterrupted 34 hour break",
        state = ToastStates.ERROR,
        serverName = "exceeded70HourShift"
    );

}
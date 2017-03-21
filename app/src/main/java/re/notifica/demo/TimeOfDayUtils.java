package re.notifica.demo;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import re.notifica.model.NotificareTimeOfDay;
import re.notifica.model.NotificareTimeOfDayRange;

/**
 * Created by joel on 03/01/2017.
 */

public class TimeOfDayUtils {

    public static boolean endsInNextDay(NotificareTimeOfDay start, NotificareTimeOfDay end) {
        if (start == null || end == null) {
            return false;
        }

        if (end.getHour() < start.getHour()) {
            return true;
        } else if (end.getHour() == start.getHour()) {
            if (end.getMinute() < start.getMinute()) {
                return true;
            }
        }

        return false;
    }

    public static boolean isInDndPeriod(NotificareTimeOfDayRange dndRange) {
        if (dndRange == null || dndRange.getStart() == null || dndRange.getEnd() == null) {
            return false;
        }

        Date now = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);


        calendar.set(Calendar.HOUR_OF_DAY, dndRange.getStart().getHour());
        calendar.set(Calendar.MINUTE, dndRange.getStart().getMinute());
        Date startDate = calendar.getTime();


        calendar.set(Calendar.HOUR_OF_DAY, dndRange.getEnd().getHour());
        calendar.set(Calendar.MINUTE, dndRange.getEnd().getMinute());
        if (endsInNextDay(dndRange.getStart(), dndRange.getEnd())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        Date endDate = calendar.getTime();


        return (now.getTime() == startDate.getTime() || now.after(startDate)) &&
                (now.getTime() == endDate.getTime() || now.before(endDate));
    }
}

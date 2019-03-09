package app.bqlab.mediandbacks;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

class TodayString {
    static String get() {
        return new SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(Calendar.getInstance().getTime());
    }
}

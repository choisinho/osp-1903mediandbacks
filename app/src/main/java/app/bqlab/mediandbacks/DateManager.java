package app.bqlab.mediandbacks;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;

class DateManager {
    @SuppressLint("SimpleDateFormat")
    static String getToday() {
        return new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
    }
}

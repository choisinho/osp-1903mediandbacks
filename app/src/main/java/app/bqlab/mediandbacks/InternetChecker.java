package app.bqlab.mediandbacks;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.support.v7.app.AlertDialog;

class InternetChecker {
    static boolean isConnected(final Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (manager.getActiveNetwork() != null && manager.getActiveNetworkInfo().isConnected());
    }
}

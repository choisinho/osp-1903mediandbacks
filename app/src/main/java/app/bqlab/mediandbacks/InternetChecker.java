package app.bqlab.mediandbacks;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.support.v7.app.AlertDialog;

class InternetChecker {
    static boolean isConnected(final Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (manager.getActiveNetwork() != null && manager.getActiveNetworkInfo().isConnected());
    }
    static void showDialogAfterCheck(final Activity activity) {
        if (!isConnected(activity)) {
            new AlertDialog.Builder(activity)
                    .setMessage("인터넷과 연결되어 있지 않습니다.")
                    .setCancelable(false)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            activity.finishAffinity();
                        }
                    }).show();
        }
    }
}

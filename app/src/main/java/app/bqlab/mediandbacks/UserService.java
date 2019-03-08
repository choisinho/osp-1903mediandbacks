package app.bqlab.mediandbacks;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserService extends Service implements Runnable {

    //variables
    static int data;
    static int dataTotal;
    static int dataBad;
    static int dataGood;
    static int userBad;
    static int userGood;
    static int notifyDelay;
    static String weekTime;
    static String userId;
    static String userName;
    static String userSex;
    static String userBirthday;
    static String userRegisterDay;
    static String today;
    static boolean buzzable;
    static boolean threading;
    static boolean connected;
    //objects
    static DatabaseReference mDatabase;
    public static NotificationManager notificationManager;
    public static NotificationChannel notificationChannel;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setupNotificaition(intent);
        setupDatabase();
        return START_NOT_STICKY;
    }

    @Override
    public void run() {
        threading = true;
        while (threading) {
            try {
                processData(UserService.data);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        }
        stopSelf();
    }

    private void setupNotificaition(Intent intent) {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel("em", "알림", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("앱 알림");
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 100, 200});
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        String content = intent.getStringExtra("content");
        Intent i = new Intent(this, MainActivity.class);
        PendingIntent p = PendingIntent.getActivity(this, 0, i, 0);
        Notification notification = new NotificationCompat.Builder(this, "알림")
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(p)
                .build();
        startForeground(1, notification);
    }

    private void setupDatabase() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child(String.valueOf(userId.hashCode()));
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                checkToday(dataSnapshot.child("data").getChildren());
                loadData(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserService.this, "연결 상태가 원활하지 않습니다. ", Toast.LENGTH_LONG).show();
                stopSelf();
            }
        });
    }

    public boolean checkToday(Iterable<DataSnapshot> daySnapshots) {
        String today = Today.getString();
        List<String> days = new ArrayList<>();
        for (DataSnapshot snapshot : daySnapshots) {
            days.add(Objects.requireNonNull(snapshot.getKey()));
        }
        if (!days.contains(today)) {
            mDatabase.child("data").child(today).child("vibrate").setValue(0);
            mDatabase.child("data").child(today).child("total").setValue(0);
            mDatabase.child("data").child(today).child("good").setValue(0);
            mDatabase.child("data").child(today).child("bad").setValue(0);
            UserService.today = today;
            return false;
        }
        return true;
    }

    public void loadData(DataSnapshot dataSnapshot) {
        userName = Objects.requireNonNull(dataSnapshot.child("info").child("name").getValue()).toString();
        userSex = Objects.requireNonNull(dataSnapshot.child("info").child("sex").getValue()).toString();
        userBirthday = Objects.requireNonNull(dataSnapshot.child("info").child("birth").getValue()).toString();
        userRegisterDay = Objects.requireNonNull(dataSnapshot.child("info").child("register").getValue()).toString();
        data = ((Long) Objects.requireNonNull(dataSnapshot.child("data").child("realtime").getValue())).intValue();
        dataTotal = ((Long) Objects.requireNonNull(dataSnapshot.child("data").child(today).child("total").getValue())).intValue();
        dataGood = ((Long) Objects.requireNonNull(dataSnapshot.child("data").child(today).child("good").getValue())).intValue();
        dataBad = ((Long) Objects.requireNonNull(dataSnapshot.child("data").child(today).child("bad").getValue())).intValue();
        notifyDelay = ((Long) Objects.requireNonNull(dataSnapshot.child("setting").child("notify_delay").getValue())).intValue();
        weekTime = Objects.requireNonNull(dataSnapshot.child("setting").child("week_time").getValue()).toString();
        userGood = ((Long) Objects.requireNonNull(dataSnapshot.child("setting").child("good").getValue())).intValue();
        userBad = ((Long) Objects.requireNonNull(dataSnapshot.child("setting").child("bad").getValue())).intValue();
    }

    public void processData(int data) {
        if (dataTotal != 0) {
            mDatabase.child("data").child(today).child("total").setValue(dataTotal + 1);
            if (data > userGood - 10 && data < userGood + 10)
                mDatabase.child("data").child(today).child("good").setValue(dataGood + 1);
            if (data > userBad - 10 && data < userBad + 10) {
                mDatabase.child("data").child(today).child("badtime").setValue(dataBad + 1);
                mDatabase.child("data").child(today).child("vibrate").setValue(dataBad);
                makeNotification();
            }
        }
    }

    public void makeNotification() {
        if (buzzable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.notify(0, new NotificationCompat.Builder(this, "알림")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("알림")
                        .setContentText("현재 나쁜 자세를 취하고 있습니다.")
                        .setWhen(System.currentTimeMillis())
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .build());
            } else {
                notificationManager.notify(0, new Notification.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("알림")
                        .setContentText("현재 나쁜 자세를 취하고 있습니다.")
                        .setWhen(System.currentTimeMillis())
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .build());
            }
        }
    }
}

package app.bqlab.mediandbacks;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
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
    static int dataVibrate;
    static int dataBad;
    static int dataGood;
    static int badPose;
    static int goodPose;
    static int notifyDelay;
    static String weekGoal;
    static String userId;
    static String userKey;
    static String userName;
    static String userSex;
    static String userBirthday;
    static String userRegisterDate;
    static String today;
    static boolean buzzable;
    static boolean threading;
    static boolean deviceConnected;
    //objects
    static DatabaseReference mDatabase;
    public static NotificationManagerCompat notificationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setupDatabase();
        return START_NOT_STICKY;
    }

    @Override
    public void run() {
        threading = true;
        while (threading) {
            try {
                Log.d("UserService", "isRunning");
                processData(UserService.data);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        }
        stopSelf();
    }

    public void loadData(DataSnapshot dataSnapshot) {
        try {
            //data
            dataGood = ((Long) Objects.requireNonNull(dataSnapshot.child("data").child(today).child("good").getValue())).intValue();
            dataBad = ((Long) Objects.requireNonNull(dataSnapshot.child("data").child(today).child("bad").getValue())).intValue();
            dataTotal = ((Long) Objects.requireNonNull(dataSnapshot.child("data").child(today).child("total").getValue())).intValue();
            dataVibrate = ((Long) Objects.requireNonNull(dataSnapshot.child("data").child(today).child("vibrate").getValue())).intValue();
            //info
            userName = Objects.requireNonNull(dataSnapshot.child("info").child("name").getValue()).toString();
            userSex = Objects.requireNonNull(dataSnapshot.child("info").child("sex").getValue()).toString();
            userBirthday = Objects.requireNonNull(dataSnapshot.child("info").child("birthday").getValue()).toString();
            userRegisterDate = Objects.requireNonNull(dataSnapshot.child("info").child("register_date").getValue()).toString();
            //setting
            notifyDelay = ((Long) Objects.requireNonNull(dataSnapshot.child("setting").child("notify_delay").getValue())).intValue();
            weekGoal = Objects.requireNonNull(dataSnapshot.child("setting").child("week_goal").getValue()).toString();
            goodPose = ((Long) Objects.requireNonNull(dataSnapshot.child("setting").child("good_pose").getValue())).intValue();
            badPose = ((Long) Objects.requireNonNull(dataSnapshot.child("setting").child("bad_pose").getValue())).intValue();
        } catch (NullPointerException e) {
            checkToday(dataSnapshot.child("data").getChildren());
        }
    }

    public void processData(int data) {
        if (dataTotal != 0 && deviceConnected) {
            mDatabase.child("data").child(today).child("total").setValue(dataTotal + 1);
            if (data > goodPose - 10 && data < goodPose + 10)
                mDatabase.child("data").child(today).child("good").setValue(dataGood + 1);
            if (data > badPose - 10 && data < badPose + 10) {
                mDatabase.child("data").child(today).child("bad").setValue(dataBad + 1);
                mDatabase.child("data").child(today).child("vibrate").setValue(dataBad);
                makeNotification();
            }
        }
    }

    public void checkToday(Iterable<DataSnapshot> daySnapshots) {
        String today = TodayString.get();
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
        }
    }

    private void setupDatabase() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child(String.valueOf(userId.hashCode()));
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                loadData(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserService.this, "연결 상태가 원활하지 않습니다. ", Toast.LENGTH_LONG).show();
                stopSelf();
            }
        });
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                new Thread(UserService.this).start();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void makeNotification() {
        try {
            Thread.sleep(1000 * UserService.notifyDelay);
            notificationManager = NotificationManagerCompat.from(this);
            Notification notification = new Notification.Builder(this, App.CHANNAL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle("알림")
                    .setContentText("현재 나쁜 자세를 취하고 있습니다.")
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .build();
            notificationManager.notify(1, notification);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

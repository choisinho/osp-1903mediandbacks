package app.bqlab.mediandbacks;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    //constants
    final int LAYOUT_MAIN_DASHBOARD = 0;
    final int LAYOUT_MAIN_ANALYSIS = 1;
    final int LAYOUT_MAIN_STRETCH = 2;
    final int LAYOUT_MAIN_SETTING = 3;
    final int LAYOUT_MAIN_SETTING_CONNECT = 4;
    final int LAYOUT_MAIN_SETTING_NOTIFY = 5;
    final int LAYOUT_MAIN_SETTING_PROFILE = 6;
    final int LAYOUT_MAIN_SETTING_VERSION = 7;
    final int LAYOUT_MAIN_SETTING_NOTICE = 8;
    //variables
    int layoutIndex;
    String totalTimeText, rightTimeText, badTimeText;
    //objects
    Bluetooth mBluetooth;
    DatabaseReference mDatabase;
    //layouts
    PieChart mainChart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InternetCheck.showDialogAfterCheck(this);
        showRefreshDialog();
        init();
        new AlertDialog.Builder(this)
                .setMessage("연결?")
                .setPositiveButton("ㅇㅇ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        connectDevice();
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            switch (requestCode) {
                case Bluetooth.REQUEST_ENABLE_BLUETOOTH:
                    break;
                case Bluetooth.REQUEST_DISCOVERABLE:
                    break;

            }
        } else {
            switch (requestCode) {
                case Bluetooth.REQUEST_ENABLE_BLUETOOTH:
                    mBluetooth.setup();
                    break;
                case Bluetooth.REQUEST_DISCOVERABLE:
                    Toast.makeText(this, "장치와 연결중입니다.", Toast.LENGTH_LONG).show();
                    mBluetooth.autoConnect();
                    break;

            }
        }
    }

    private void init() {
        //initialize
        mBluetooth = new Bluetooth(this, this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //setup
        ((SwipeRefreshLayout) findViewById(R.id.main_refresh_layout)).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
    }

    private void connectDevice() {
        if (!ServiceCheck.isRunning(this, UserService.class.getName())) {
            Log.d("MainActivity", "Userservice not running");
            new AlertDialog.Builder(this)
                    .setMessage("사용자의 정보를 불러올 수 없습니다. 다시 로그인하시길 바랍니다.")
                    .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        }
                    }).show();
        } else if (UserService.deviceConnected) {
            Log.d("MainActivity", "Already device connected");
            Toast.makeText(this, "이미 장치와 연결되어 있습니다.", Toast.LENGTH_LONG).show();
        } else {
            Log.d("MainActivity", "Start to connect device");
            ChildrenEnable.set(false, (ViewGroup) findViewById(R.id.main));
            Toast.makeText(this, "장치와의 연결을 시작합니다.", Toast.LENGTH_LONG).show();
            mBluetooth.getSetting().setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
                @Override
                public void onDataReceived(byte[] data, String message) {
                    UserService.deviceConnected = true;
                    UserService.data = Integer.valueOf(message) - 90;
                    mDatabase.child(UserService.userKey).child("data").child("realtime").setValue(UserService.data);
                    Log.d("MainActivity", "Realtime data: " + String.valueOf(UserService.data));
                }
            });
            mBluetooth.getSetting().setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
                @Override
                public void onDeviceConnected(String name, String address) {
                    UserService.dataTotal = 1;
                    ChildrenEnable.set(true, (ViewGroup) findViewById(R.id.main));
                    Log.d("MainActivity", "Connected to device");
                    Toast.makeText(MainActivity.this, "장치와 연결되었습니다.", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onDeviceDisconnected() {
                    UserService.deviceConnected = false;
                    Log.d("MainActivity", "Disconnected to device");
                    Toast.makeText(MainActivity.this, "장치와의 연결이 끊겼습니다.", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onDeviceConnectionFailed() {
                    Log.d("MainActivity", "Failed to connect device");
                    Toast.makeText(MainActivity.this, "장치와 연결할 수 없습니다.", Toast.LENGTH_LONG).show();
                }
            });
            mBluetooth.checkup();
        }

    }

    private void refresh() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
        Toast.makeText(MainActivity.this, "새로고침이 완료되었습니다.", Toast.LENGTH_LONG).show();
        ((SwipeRefreshLayout) findViewById(R.id.main_refresh_layout)).setRefreshing(false);
    }

    private void showRefreshDialog() {
        if (getSharedPreferences("setting", MODE_PRIVATE).getBoolean("SHOW_REFRESH_DAILOG", true)) {
            new AlertDialog.Builder(this)
                    .setTitle("그래프가 잘 보이지 않나요?")
                    .setMessage("새로고침을 원하시면 손가락을 화면에 대고 쓸어내려보세요.")
                    .setPositiveButton("새로고침", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            refresh();
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setNeutralButton("다시는 나타나지 않음", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getSharedPreferences("setting", MODE_PRIVATE).edit().putBoolean("SHOW_REFRESH_DAILOG", false).apply();
                        }
                    })
                    .show();
        }
    }
}

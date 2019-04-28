package app.bqlab.mediandbacks;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
import java.util.Objects;

public class InitialActivity extends AppCompatActivity {

    //constants
    private static final int REQUEST_ENABLE_BLUETOOTH = 0;
    private static final int REQUEST_DISCOVERABLE = 1;
    private static final int ACCESS_COARSE_LOCATION = 2;
    //virables
    boolean mainToInitial;
    //objects
    DatabaseReference mDatabase;
    Bluetooth mBluetooth;
    //layouts
    ActionBar actionBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);
        setupActionbar();
        init();
    }

    private void init() {
        //check
        InternetCheck.showDialogAfterCheck(this);
        InternetCheck.showDialogAfterCheck(this);
        PermissionCheck.checkLocationPermission(this);
        //initialize
        mBluetooth = new Bluetooth(this, this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mainToInitial = getIntent().getBooleanExtra("mainToInitial", false);
        //call method
        checkEnterOption();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            switch (requestCode) {
                case Bluetooth.REQUEST_ENABLE_BLUETOOTH:
                    Toast.makeText(InitialActivity.this, "준비가 끝나면 다시 시도하세요.", Toast.LENGTH_LONG).show();
                    showInitialFirst();
                    break;
                case Bluetooth.REQUEST_DISCOVERABLE:
                    Toast.makeText(InitialActivity.this, "준비가 끝나면 다시 시도하세요.", Toast.LENGTH_LONG).show();
                    showInitialFirst();
                    break;

            }
        } else {
            switch (requestCode) {
                case Bluetooth.REQUEST_ENABLE_BLUETOOTH:
                    mBluetooth.setup();
                    break;
                case Bluetooth.REQUEST_DISCOVERABLE:
                    ((Button) findViewById(R.id.initial_second_button)).setText(getResources().getString(R.string.initial_second_button2));
                    mBluetooth.autoConnect();
                    break;

            }
        }
    }

    private void showInitialFirst() {
        //check userservice running
        if (!ServiceCheck.isRunning(this, UserService.class.getName())) {
            Log.d("MainActivity", "Userservice not running");
            new android.app.AlertDialog.Builder(this)
                    .setMessage("사용자의 정보를 불러올 수 없습니다. 다시 로그인하시길 바랍니다.")
                    .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(InitialActivity.this, LoginActivity.class));
                            mBluetooth.getSetting().disconnect();
                            finish();
                        }
                    }).show();
        }
        //attribute
        findViewById(R.id.initial_first).setVisibility(View.VISIBLE);
        findViewById(R.id.initial_second).setVisibility(View.GONE);
        findViewById(R.id.initial_third).setVisibility(View.GONE);
        findViewById(R.id.initial_fourth).setVisibility(View.GONE);
        findViewById(R.id.initial_fifth).setVisibility(View.GONE);
        //event
        ((TextView) findViewById(R.id.initial_actionbar)).setText(getResources().getString(R.string.initial_first_title));
        ((CheckBox) findViewById(R.id.initial_first_check1)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (((CheckBox) findViewById(R.id.initial_first_check2)).isChecked())
                        findViewById(R.id.initial_first_button).setBackground(getDrawable(R.drawable.app_button_red));
                } else {
                    if (((CheckBox) findViewById(R.id.initial_first_check2)).isChecked())
                        findViewById(R.id.initial_first_button).setBackground(getDrawable(R.drawable.app_button_gray));
                }
            }
        });
        ((CheckBox) findViewById(R.id.initial_first_check2)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (((CheckBox) findViewById(R.id.initial_first_check1)).isChecked())
                        findViewById(R.id.initial_first_button).setBackground(getDrawable(R.drawable.app_button_red));
                } else {
                    if (((CheckBox) findViewById(R.id.initial_first_check1)).isChecked())
                        findViewById(R.id.initial_first_button).setBackground(getDrawable(R.drawable.app_button_gray));
                }
            }
        });
        findViewById(R.id.initial_first_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) findViewById(R.id.initial_first_check1)).isChecked() && ((CheckBox) findViewById(R.id.initial_first_check2)).isChecked())
                    showInitialSecond();
            }
        });
    }

    private void showInitialSecond() {
        //attribute
        findViewById(R.id.initial_first).setVisibility(View.GONE);
        findViewById(R.id.initial_second).setVisibility(View.VISIBLE);
        findViewById(R.id.initial_third).setVisibility(View.GONE);
        findViewById(R.id.initial_fourth).setVisibility(View.GONE);
        findViewById(R.id.initial_fifth).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.initial_actionbar)).setText(getResources().getString(R.string.initial_second_title));
        //event
        if (UserService.deviceConnected) {
            if (UserService.dataTotal == 0)
                UserService.dataTotal = 1;
            Log.d("InitialActivity", "Already Connected");
            ((Button) findViewById(R.id.initial_second_button)).setText(getResources().getString(R.string.initial_second_button3));
            findViewById(R.id.initial_second_button).setBackground(getResources().getDrawable(R.drawable.app_button_red));
        } else {
            mBluetooth.getSetting().setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
                @Override
                public void onDataReceived(byte[] data, String message) {
                    UserService.deviceConnected = true;
                    UserService.data = Integer.valueOf(message) - 90;
                    mDatabase.child(UserService.userKey).child("data").child("realtime").setValue(UserService.data);
                    Log.d("InitialiActivity", "Realtime data: " + String.valueOf(UserService.data));
                }
            });
            mBluetooth.getSetting().setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
                @Override
                public void onDeviceConnected(String name, String address) {
                    if (UserService.dataTotal == 0)
                        UserService.dataTotal = 1;
                    Log.d("InitialActivity", "Connected to device");
                    ((Button) findViewById(R.id.initial_second_button)).setText(getResources().getString(R.string.initial_second_button3));
                    findViewById(R.id.initial_second_button).setBackground(getResources().getDrawable(R.drawable.app_button_red));
                }

                @Override
                public void onDeviceDisconnected() {
                    UserService.deviceConnected = false;
                    mBluetooth.getSetting().disconnect();
                    mBluetooth.getSetting().cancelDiscovery();
                    Log.d("MainActivity", "Disconnected to device");
                    Toast.makeText(InitialActivity.this, "장치와의 연결이 끊겼습니다.", Toast.LENGTH_LONG).show();
                    ((Button) findViewById(R.id.initial_second_button)).setText(getResources().getString(R.string.initial_second_button));
                    findViewById(R.id.initial_second_button).setBackground(getResources().getDrawable(R.drawable.app_button_gray));
                    showInitialFirst();
                }

                @Override
                public void onDeviceConnectionFailed() {
                    UserService.deviceConnected = false;
                    mBluetooth.getSetting().disconnect();
                    mBluetooth.getSetting().cancelDiscovery();
                    Log.d("InitialActivity", "Failed to connect device");
                    Toast.makeText(InitialActivity.this, "장치와 연결할 수 없습니다.", Toast.LENGTH_LONG).show();
                    ((Button) findViewById(R.id.initial_second_button)).setText(getResources().getString(R.string.initial_second_button));
                    findViewById(R.id.initial_second_button).setBackground(getResources().getDrawable(R.drawable.app_button_gray));
                    showInitialFirst();
                }
            });
            findViewById(R.id.initial_second_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (UserService.deviceConnected) {
                        showSetposeLayout();
                    }
                }
            });
            //call method
            mBluetooth.checkup();
        }
    }

    private void showInitialThird() {
        //attribute
        findViewById(R.id.initial_first).setVisibility(View.GONE);
        findViewById(R.id.initial_second).setVisibility(View.GONE);
        findViewById(R.id.initial_third).setVisibility(View.VISIBLE);
        findViewById(R.id.initial_fourth).setVisibility(View.GONE);
        findViewById(R.id.initial_fifth).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.initial_actionbar)).setText(getResources().getString(R.string.initial_third_title));
        //event
        findViewById(R.id.initial_third_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("InitialActivity", "Good pose set to " + String.valueOf(UserService.data));
                mDatabase.child(UserService.userKey).child("setting").child("good_pose").setValue(UserService.data);
                showInitialFourth();
            }
        });
    }

    private void showInitialFourth() {
        //attribute
        findViewById(R.id.initial_first).setVisibility(View.GONE);
        findViewById(R.id.initial_second).setVisibility(View.GONE);
        findViewById(R.id.initial_third).setVisibility(View.GONE);
        findViewById(R.id.initial_fourth).setVisibility(View.VISIBLE);
        findViewById(R.id.initial_fifth).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.initial_actionbar)).setText(getResources().getString(R.string.initial_fourth_title));
        //event
        findViewById(R.id.initial_fourth_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("InitialActivity", "Bad pose set to " + String.valueOf(UserService.data));
                mDatabase.child(UserService.userKey).child("setting").child("bad_pose").setValue(UserService.data);
                showInitialFifth();
            }
        });
    }

    private void showInitialFifth() {
        //attribute
        findViewById(R.id.initial_first).setVisibility(View.GONE);
        findViewById(R.id.initial_second).setVisibility(View.GONE);
        findViewById(R.id.initial_third).setVisibility(View.GONE);
        findViewById(R.id.initial_fourth).setVisibility(View.GONE);
        findViewById(R.id.initial_fifth).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.initial_actionbar)).setText(getResources().getString(R.string.initial_fifth_title));
        //event
        final NumberPicker picker = findViewById(R.id.initial_fifth_picker);
        final String[] delays = new String[]{"즉시", "5초 후", "10초 후"};
        final String[] goals = new String[]{"1일 60분 주4회", "1일 30분 주4회", "1일 10분 주4회"};
        picker.setMaxValue(2);
        picker.setDisplayedValues(delays);
        findViewById(R.id.initial_fifth_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String s = "나쁜 자세가 감지되면 " + delays[picker.getValue()] + " 진동으로 알려줍니다.";
                new AlertDialog.Builder(InitialActivity.this)
                        .setTitle("이 시간으로 설정하시겠습니까?")
                        .setMessage(s)
                        .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //set notify delay
                                int i = picker.getValue() * 5;
                                Log.d("InitialActivity", "Notify delay set to " + String.valueOf(i));
                                mDatabase.child(UserService.userKey).child("setting").child("notify_delay").setValue(i);
                                //load week goal setting
                                ((TextView) findViewById(R.id.initial_actionbar)).setText("주간목표 설정하기");
                                picker.setDisplayedValues(goals);
                                v.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        new AlertDialog.Builder(InitialActivity.this)
                                                .setTitle("이 목표로 설정하시겠습니까?")
                                                .setMessage("주간목표 달성률은 대쉬보드에 표시됩니다.")
                                                .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                })
                                                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        UserService.dataTotal += 1;
                                                        UserService.buzzable = true;
                                                        getSharedPreferences("setting", MODE_PRIVATE).edit().putBoolean("FIRST_RUN", false).apply();
                                                        Toast.makeText(InitialActivity.this, "지금부터 자세 분석을 시작됩니다.", Toast.LENGTH_LONG).show();
                                                        mDatabase.child(UserService.userKey).child("setting").child("week_goal").setValue(goals[picker.getValue()]);
                                                        Intent i = new Intent(InitialActivity.this, MainActivity.class);
                                                        startActivity(i);
                                                        finish();
                                                    }
                                                }).show();
                                    }
                                });
                            }
                        }).show();
            }
        });
    }

    private void showSetposeLayout() {
        FrameLayout initial = findViewById(R.id.initial);
        ChildrenEnable.set(false, initial);
        final SetposeLayout dialog = new SetposeLayout(this);
        dialog.getButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInitialThird();
                dialog.close();
            }
        });
        initial.addView(dialog);
    }

    private void setupActionbar() {
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_initial);
    }

    private void checkEnterOption() {
        if (mainToInitial)
            showInitialThird();
        else {
            showInitialFirst();
        }
    }
}

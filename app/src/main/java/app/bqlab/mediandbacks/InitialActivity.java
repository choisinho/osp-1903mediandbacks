package app.bqlab.mediandbacks;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.internal.StringResourceValueReader;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;

public class InitialActivity extends AppCompatActivity {

    //constants
    private static final int REQUEST_ENABLE_BLUETOOTH = 0;
    private static final int REQUEST_DISCOVERABLE = 1;
    private static final int ACCESS_COARSE_LOCATION = 2;
    //objects
    DatabaseReference mDatabase;
    BluetoothSetting mBluetooth;
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
        PermissionCheck.checkLocationPermission(this);
        //initialize
        mBluetooth = new BluetoothSetting(this, this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //call
        showInitialFirst();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            switch (requestCode) {
                case BluetoothSetting.REQUEST_ENABLE_BLUETOOTH:
                    Toast.makeText(InitialActivity.this, "준비가 끝나면 다시 시도하세요.", Toast.LENGTH_LONG).show();
                    showInitialFirst();
                    break;
                case BluetoothSetting.REQUEST_DISCOVERABLE:
                    Toast.makeText(InitialActivity.this, "준비가 끝나면 다시 시도하세요.", Toast.LENGTH_LONG).show();
                    showInitialFirst();
                    break;

            }
        } else {
            switch (requestCode) {
                case BluetoothSetting.REQUEST_ENABLE_BLUETOOTH:
                    mBluetooth.setup();
                    break;
                case BluetoothSetting.REQUEST_DISCOVERABLE:
                    mBluetooth.autoConnect();
                    break;

            }
        }
    }

    private void showInitialFirst() {
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
        Log.d("InitialActivity", UserService.userKey);
        ((TextView) findViewById(R.id.initial_actionbar)).setText(getResources().getString(R.string.initial_second_title));
        mBluetooth.getSetting().setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            @Override
            public void onDataReceived(byte[] data, String message) {
                UserService.deviceConnected = true;
                UserService.data = Integer.valueOf(message) - 90;
                mDatabase.child(UserService.userKey).child("data").child("realtime").setValue(UserService.data);
                Log.d("InitialiActivity", "Realtime data -> "+ String.valueOf(UserService.data));
            }
        });
        mBluetooth.getSetting().setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            @Override
            public void onDeviceConnected(String name, String address) {
                UserService.dataTotal = 1;
                Log.d("InitialActivity", "Connected to device");
                ((Button) findViewById(R.id.initial_second_button)).setText(getResources().getString(R.string.initial_second_button3));
                findViewById(R.id.initial_second_button).setBackground(getResources().getDrawable(R.drawable.app_button_red));
            }

            @Override
            public void onDeviceDisconnected() {
                Log.d("InitialActivity", "Disconnected to device");
                Toast.makeText(InitialActivity.this, "장치와의 연결이 끊겼습니다.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onDeviceConnectionFailed() {
                Log.d("InitialActivity", "Failed to connect device");
                Toast.makeText(InitialActivity.this, "장치와의 연결이 끊겼습니다.", Toast.LENGTH_LONG).show();
            }
        });
        mBluetooth.checkup();
    }

    private void setupActionbar() {
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_initial);
    }
}

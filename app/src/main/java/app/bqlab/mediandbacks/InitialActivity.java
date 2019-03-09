package app.bqlab.mediandbacks;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;

import java.util.Objects;
import java.util.Set;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;

public class InitialActivity extends AppCompatActivity {

    //constants
    private static final int REQUEST_ENABLE_BLUETOOTH = 0;
    private static final int REQUEST_DISCOVERABLE = 1;
    private static final int ACCESS_COARSE_LOCATION = 2;
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
        PermissionCheck.checkLocationPermission(this);
        //initialize
        mBluetooth = new Bluetooth(this, this);
        //call
        showInitialFirst();
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
        ((TextView) findViewById(R.id.initial_actionbar)).setText(getResources().getString(R.string.initial_second_title));
        mBluetooth.setup();
    }

    private void setupActionbar() {
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_initial);
    }
}

package app.bqlab.mediandbacks;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;

import java.util.Set;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;

public class InitialActivity extends AppCompatActivity {

    //constants
    private static final int REQUEST_ENABLE_BLUETOOTH = 0;
    private static final int REQUEST_DISCOVERABLE = 1;
    private static final int ACCESS_COARSE_LOCATION = 2;
    //objects
    DatabaseReference mDatabase;
    BluetoothSPP mBluetooth;
    BluetoothAdapter mBTAdapter;
    Set<BluetoothDevice> pairedDevices;
    //layouts
    ActionBar actionBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);
        init();
    }

    private void init() {
        //check
        InternetCheck.showDialogAfterCheck(this);
        PermissionCheck.checkLocationPermission(this);
        //initialize
        actionBar = getSupportActionBar();
        mBluetooth = new BluetoothSPP(this);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    
}

package app.bqlab.mediandbacks;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;

import java.util.Set;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

public class Bluetooth {
    //constants
    static final int REQUEST_ENABLE_BLUETOOTH = 1000;
    static final int REQUEST_DISCOVERABLE = 1001;
    //objects
    private Context mContext;
    private Activity mActivity;
    private BluetoothSPP mBluetooth;
    private BluetoothAdapter mBTAdapter;
    private BluetoothDevice mBTDevice;
    private Set<BluetoothDevice> pairedDevices;

    Bluetooth(Activity activity, Context context) {
        mContext = context;
        mActivity = activity;
        mBluetooth = new BluetoothSPP(context);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = mBTAdapter.getBondedDevices();
    }

    void setup() {
        if (!mBluetooth.isBluetoothAvailable()) {
            new AlertDialog.Builder(mContext)
                    .setCancelable(false)
                    .setMessage("지원하지 않는 기기입니다.")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mActivity.finishAffinity();
                        }
                    }).show();
        } else if (!mBluetooth.isBluetoothEnabled()) {
            mActivity.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BLUETOOTH);
        } else if (!mBluetooth.isServiceAvailable()) {
            mBluetooth.setupService();
            mBluetooth.startService(BluetoothState.DEVICE_OTHER);
            setup();
        }
    }

    private void autoConnect(String name) {
        if (isPaired(name)) {
            if (mBTAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                mActivity.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE), REQUEST_DISCOVERABLE);
            } else {
                if (mBTAdapter.isDiscovering())
                    mBTAdapter.cancelDiscovery();
                IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                mActivity.registerReceiver(broadcastReceiver, filter);
                mBTAdapter.startDiscovery();
            }
        } else {
            mBluetooth.connect(mBTDevice.getAddress());
        }
    }

    public boolean isPaired(String name) {
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals("mediandbacks")) {
                return true;
            }
        }
        return false;
    }

    public BluetoothDevice getPairedDevice(String name) {
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals("mediandbacks")) {
                return device;
            }
        }
        return null;
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName().equals("mediandbacks")) {
                    mBTDevice = device;
                    mBTAdapter.cancelDiscovery();
                    mBluetooth.connect(device.getAddress());
                    mActivity.unregisterReceiver(broadcastReceiver);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                new AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setMessage("장치를 찾을 수 없습니다.")
                        .setPositiveButton("재시도", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setup();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(mContext, "준비가 끝나면 다시 시도하세요.", Toast.LENGTH_LONG).show();
                                mActivity.unregisterReceiver(broadcastReceiver);
                                setup();
                            }
                        }).show();
            }
        }
    };
}

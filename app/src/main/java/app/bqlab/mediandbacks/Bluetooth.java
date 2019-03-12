package app.bqlab.mediandbacks;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

class Bluetooth implements Serializable {
    //constants
    static final int REQUEST_ENABLE_BLUETOOTH = 1000;
    static final int REQUEST_DISCOVERABLE = 1001;
    //objects
    private Context mContext;
    private Activity mActivity;
    private BluetoothSPP mBluetooth;
    private BluetoothAdapter mBTAdapter;
    private BluetoothDevice mBTDevice;
    private BluetoothManager mBTManager;
    private Set<BluetoothDevice> pairedDevices;
    private List<BluetoothDevice> connectedDevices;

    Bluetooth(Activity activity, Context context) {
        mContext = context;
        mActivity = activity;
        mBluetooth = new BluetoothSPP(context);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        mBTManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        pairedDevices = mBTAdapter.getBondedDevices();
        connectedDevices = mBTManager.getConnectedDevices(BluetoothGatt.GATT);
    }

    void checkup() {
        if (!mBluetooth.isBluetoothAvailable()) {
            Log.d("Bluetooth", "Checking up");
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
        } else
            setup();
    }

    void setup() {
        Log.d("Bluetooth", "Setting up");
        if (!mBluetooth.isServiceAvailable()) {
            mBluetooth.setupService();
            mBluetooth.startService(BluetoothState.DEVICE_OTHER);
            setup();
        } else
            autoConnect();
    }

    void autoConnect() {
        if (!isPaired()) {
            Log.d("Bluetooth", "Not paired device");
            if (mBTAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                Log.d("Bluetooth", "Request SCAN_MODE permission");
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
            mBluetooth.connect(getPairedDevice().getAddress());
        }
    }

    boolean isPaired() {
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals("HC-06")) {
                return true;
            }
        }
        return false;
    }

    BluetoothSPP getSetting() {
        return mBluetooth;
    }

    BluetoothDevice getPairedDevice() {
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals("HC-06")) {
                return device;
            }
        }
        return null;
    }

    BluetoothDevice getConnectedDevice() {
        return mBTDevice;
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName().equals("HC-06")) {
                    mBTDevice = device;
                    mBTAdapter.cancelDiscovery();
                    mBluetooth.connect(device.getAddress());
                    mActivity.unregisterReceiver(broadcastReceiver);
                    Log.d("Bluetooth", "Found the device");
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d("Bluetooth", "Cannot found the device");
                new AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setMessage("장치를 찾을 수 없습니다.")
                        .setPositiveButton("재시도", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                checkup();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(mContext, "준비가 끝나면 다시 시도하세요.", Toast.LENGTH_LONG).show();
                                mActivity.unregisterReceiver(broadcastReceiver);
                            }
                        }).show();
            }
        }
    };
}

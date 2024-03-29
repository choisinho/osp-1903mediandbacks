/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.bqlab.mediandbacks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.util.Log;
import android.widget.TabHost;
import android.widget.Toast;

@SuppressLint("NewApi")
public class BluetoothService {
    //constants
    private static final String TAG = "Bluetooth Service";
    private static final String NAME_SECURE = "Bluetooth Secure";
    private static final UUID UUID_ANDROID_DEVICE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID UUID_OTHER_DEVICE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //variables
    private int mState;
    private boolean isAndroid = BluetoothState.DEVICE_ANDROID;
    //objects
    private Activity mActivity;
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mSecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    BluetoothService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = BluetoothState.STATE_NONE;
        mHandler = handler;
    }

    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        mHandler.obtainMessage(BluetoothState.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    synchronized int getState() {
        return mState;
    }

    synchronized void start(boolean isAndroid) {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(BluetoothState.STATE_LISTEN);

        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(isAndroid);
            mSecureAcceptThread.start();
            BluetoothService.this.isAndroid = isAndroid;
        }
    }

    synchronized void connect(BluetoothDevice device) {
        if (mState == BluetoothState.STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(BluetoothState.STATE_CONNECTING);
    }

    private synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();
        Message msg = mHandler.obtainMessage(BluetoothState.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothState.DEVICE_NAME, device.getName());
        bundle.putString(BluetoothState.DEVICE_ADDRESS, device.getAddress());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        setState(BluetoothState.STATE_CONNECTED);
    }

    synchronized void stop() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread.kill();
            mSecureAcceptThread = null;
        }
        setState(BluetoothState.STATE_NONE);
    }

    void write(byte[] out) {
        ConnectedThread r;
        synchronized (this) {
            if (mState != BluetoothState.STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        r.write(out);
    }

    private void connectionFailed() {
        BluetoothService.this.start(BluetoothService.this.isAndroid);
    }

    private void connectionLost() {
        BluetoothService.this.start(BluetoothService.this.isAndroid);
    }

    private class AcceptThread extends Thread {
        private BluetoothServerSocket mmServerSocket;
        private String mSocketType;
        boolean isRunning = true;

        AcceptThread(boolean isAndroid) {
            BluetoothServerSocket tmp = null;
            try {
                if (isAndroid)
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, UUID_ANDROID_DEVICE);
                else
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, UUID_OTHER_DEVICE);
            } catch (IOException ignored) {
            }
            mmServerSocket = tmp;
        }

        public void run() {
            setName("AcceptThread" + mSocketType);
            BluetoothSocket socket = null;
            while (mState != BluetoothState.STATE_CONNECTED && isRunning) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                } catch (NullPointerException e) {
                    break;
                }
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                            case BluetoothState.STATE_LISTEN:
                            case BluetoothState.STATE_CONNECTING:
                                connected(socket, socket.getRemoteDevice(),
                                        mSocketType);
                                break;
                            case BluetoothState.STATE_NONE:
                            case BluetoothState.STATE_CONNECTED:
                                try {
                                    socket.close();
                                } catch (IOException ignored) {
                                }
                                break;
                        }
                    }
                }
            }
        }

        void cancel() {
            try {
                mmServerSocket.close();
                mmServerSocket = null;
            } catch (IOException ignored) {
            } catch (NullPointerException ignored) {
            }
        }

        void kill() {
            isRunning = false;
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                if (BluetoothService.this.isAndroid)
                    tmp = device.createRfcommSocketToServiceRecord(UUID_ANDROID_DEVICE);
                else
                    tmp = device.createRfcommSocketToServiceRecord(UUID_OTHER_DEVICE);
            } catch (IOException ignored) {
            }
            mmSocket = tmp;
        }

        public void run() {
            mAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException e) {
                try {
                    mmSocket.close();
                } catch (IOException ignored) {
                }
                connectionFailed();
                return;
            }
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }
            connected(mmSocket, mmDevice, mSocketType);
        }

        void cancel() {
            try {
                mmSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        ConnectedThread(BluetoothSocket socket, String socketType) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException ignored) {
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer;
            ArrayList<Integer> arr_byte = new ArrayList<Integer>();
            while (true) {
                try {
                    int data = mmInStream.read();
                    if (data == 0x0A) {
                    } else if (data == 0x0D) {
                        buffer = new byte[arr_byte.size()];
                        for (int i = 0; i < arr_byte.size(); i++) {
                            buffer[i] = arr_byte.get(i).byteValue();
                        }
                        mHandler.obtainMessage(BluetoothState.MESSAGE_READ, buffer.length, -1, buffer).sendToTarget();
                        arr_byte = new ArrayList<Integer>();
                    } else {
                        arr_byte.add(data);
                    }
                } catch (IOException e) {
                    connectionLost();
                    BluetoothService.this.start(BluetoothService.this.isAndroid);
                    break;
                }
            }
        }

        void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                mHandler.obtainMessage(BluetoothState.MESSAGE_WRITE
                        , -1, -1, buffer).sendToTarget();
            } catch (IOException ignored) {
            }
        }

        void cancel() {
            try {
                mmSocket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
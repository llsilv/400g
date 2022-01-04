package com.example.a400g;

        import androidx.appcompat.app.AppCompatActivity;
        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;
        import android.bluetooth.BluetoothSocket;
        import android.content.Intent;
        import android.graphics.Color;
        import android.os.Bundle;
        import android.util.Log;
        import android.widget.Toast;
        import androidx.annotation.Nullable;

        import com.github.mikephil.charting.charts.LineChart;
        import com.github.mikephil.charting.data.Entry;
        import com.github.mikephil.charting.data.LineData;
        import com.github.mikephil.charting.data.LineDataSet;
        import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

        import java.io.IOException;
        import java.io.InputStream;
        import java.util.ArrayList;
        import java.util.Set;
        import java.util.UUID;

public class Main extends AppCompatActivity {
    private InputStream inputStream;
    private BluetoothAdapter BtAdapter;
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private final String DEVICE_ADDRESS = "00:13:EF:00:09:72";
    private final String DEVICE_NAME = "HC-06";
    LineChart mChart;
    LineDataSet xset, yset, zset;

    ArrayList<Entry> xdata = new ArrayList<>();
    ArrayList<Entry> ydata = new ArrayList<>();
    ArrayList<Entry> zdata = new ArrayList<>();
    int i = 0;
    int entry = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mChart = findViewById(R.id.chart);
        mChart.setTouchEnabled(true);
        mChart.setPinchZoom(true);

        peak(0,0,0);

        BtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (BtAdapter == null) {
            Toast.makeText(getApplicationContext(), "Device doesnt Support Bluetooth", Toast.LENGTH_SHORT).show();
        }
        if (!BtAdapter.isEnabled()) {
            Intent BtEnableIntend = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(BtEnableIntend, 1);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        }
        Set<BluetoothDevice> pairedDevices = BtAdapter.getBondedDevices();
        if (pairedDevices.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please Pair the Device first", Toast.LENGTH_SHORT).show();
        } else {
            for (BluetoothDevice iterator : pairedDevices) {
            /*if(iterator.getAddress().equals(DEVICE_ADDRESS))
            {
                device=iterator;
                break;
            }*/
                if (iterator.getName().equals(DEVICE_NAME)) {
                    device = iterator;
                      break;
                }
            }
        }
        ConnectThread myConnectThread = new ConnectThread(device, this);
        myConnectThread.start();

    }

    public void peak(float x_current_data, float y_current_data, float z_current_data)
    {
        if (i > 20)
        {
            xdata.remove(0);
            ydata.remove(0);
            zdata.remove(0);
        }
        xdata.add(new Entry(i, x_current_data));
        ydata.add(new Entry(i, y_current_data));
        zdata.add(new Entry(i, z_current_data));
        xset = new LineDataSet(xdata, "x values");
        yset = new LineDataSet(ydata, "y values");
        zset = new LineDataSet(zdata, "z values");

        xset.setColor(Color.BLUE);
        yset.setColor(Color.RED);
        zset.setColor(Color.GREEN);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(xset);
        dataSets.add(yset);
        dataSets.add(zset);
        LineData data = new LineData(dataSets);
        mChart.setData(data);
        mChart.invalidate();
        i++;
    }
}

    class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
        private Main main;
        ConnectThread(BluetoothDevice device, Main main) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            this.main = main;
            try {
                tmp = device.createRfcommSocketToServiceRecord(PORT_UUID);
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            //Log.d("fd", "run: qewf");
            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                Log.d("tag", "run: sup");
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                return;
            }
            DataTransfer myDataTransfer = new DataTransfer(mmSocket, main);
            myDataTransfer.start();
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    class DataTransfer extends Thread {
        final BluetoothSocket mmSocket;
        final InputStream mmIncData;
        private Main main;
        public DataTransfer(BluetoothSocket socket, Main main) {
            this.main = main;
            mmSocket = socket;
            InputStream tmpIn = null;
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.d("f", "DataTransfer: gerwehh"+e);
            }
            mmIncData = tmpIn;

        }

        public void run() {
            Log.d("uwu", "run: owo");
            byte[] buffer = new byte[1024];
            int begin = 0;
            int ByteCount = 0;
            while (true)
            {
                try
                {

                    begin = 0;
                    ByteCount += mmIncData.read(buffer, ByteCount, buffer.length - ByteCount);

                    for(int i = begin; i < ByteCount; i++)
                    {
                        if(buffer[i] == "#".getBytes()[0])
                        {
                            byte[] inc = (byte[])buffer;
                            String string = new String(inc, 0,i);
                            Log.d("inside1", "run: "+string);
                            String parts[] = string.split(";");
                            main.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    main.peak(Float.valueOf(parts[0]), Float.valueOf(parts[1]), Float.valueOf(parts[2]));
                                }
                            });

                            ByteCount = 0;
                            begin = 0;
                        }
                    }
                } catch (IOException e)
                {
                    Log.d("asdfadsf", "run: loop crash");
                    break;
                }
            }
        }
    }

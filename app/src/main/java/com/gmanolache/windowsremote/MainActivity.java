package com.gmanolache.windowsremote;

import android.app.Activity;
import android.app.Fragment;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.CompoundButton;
import android.widget.Toast;

import org.json.JSONArray;

import com.zsoft.signala.SendCallback;
import com.zsoft.signala.hubs.HubConnection;
import com.zsoft.signala.hubs.HubInvokeCallback;
import com.zsoft.signala.hubs.HubOnDataCallback;
import com.zsoft.signala.hubs.IHubProxy;
import com.zsoft.signala.transport.StateBase;
import com.zsoft.signala.transport.longpolling.LongPollingTransport;


import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ToggleButton;

import com.androidquery.AQuery;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private AQuery aq;
    protected HubConnection con = null;
    protected IHubProxy hub = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startSignalA();

        CompoundButton toggleMute = (CompoundButton) findViewById(R.id.toggleButton);
        toggleMute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                HubInvokeCallback callback = new HubInvokeCallback() {
                    @Override
                    public void OnResult(boolean succeeded, String response) {
                        Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void OnError(Exception ex) {
                        Toast.makeText(MainActivity.this, "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                };

                List<String> args = new ArrayList<String>(2);
                args.add("client");
                if(isChecked)
                    args.add("Mute;");
                else
                    args.add("Unmute;");
                hub.Invoke("Send", args, callback);
            }
        });

        SeekBar seekBar = (SeekBar) findViewById(R.id.VolumeSeek);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                UpdateProgress(progress - 64);
            }
        });

        // Discovery
        Connect(Uri.parse("http://192.168.1.7:8080/"));


    }

    public void ShutdownHandler(View target) {
        HubInvokeCallback callback = new HubInvokeCallback() {
            @Override
            public void OnResult(boolean succeeded, String response) {
                Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void OnError(Exception ex) {
                Toast.makeText(MainActivity.this, "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        List<String> args = new ArrayList<String>(2);
        args.add("client");
        args.add("Shutdown;");
        hub.Invoke("Send", args, callback);
    }

    public void Connect(Uri address) {

        con = new HubConnection(address.toString(), this, new LongPollingTransport())
        {
            @Override
            public void OnStateChanged(StateBase oldState, StateBase newState) {
                //tvStatus.setText(oldState.getState() + " -> " + newState.getState());

                switch(newState.getState())
                {
                    case Connected:

                        JoinGroup("clcl");
                        break;
                    default:

                        break;
                }
            }

            @Override
            public void OnError(Exception exception) {
                Toast.makeText(MainActivity.this, "On error: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            }

        };

        try {
            hub = con.CreateHubProxy("MyHub");
        } catch (OperationApplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        hub.On("DisplayMessage", new HubOnDataCallback()
        {
            @Override
            public void OnReceived(JSONArray args) {
                for(int i=0; i<args.length(); i++)
                {
                    Toast.makeText(MainActivity.this, "New message\n" + args.opt(i).toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });


        con.Start();
    }

    private void JoinGroup(String groupName) {
        HubInvokeCallback callback = new HubInvokeCallback() {
            @Override
            public void OnResult(boolean succeeded, String response) {
                if(succeeded)
                {
                    Toast.makeText(MainActivity.this, "Joined group", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Failed to join group", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void OnError(Exception ex) {
                Toast.makeText(MainActivity.this, "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        List<String> args = new ArrayList<String>(1);
        args.add(groupName);
        hub.Invoke("JoinGroup", args, callback);
    }

    public void startSignalA()
    {
        if(con!=null)
            con.Start();
    }

    public void stopSignalA()
    {
        if(con!=null)
            con.Stop();
    }

    public void UpdateProgress(int progress) {
        HubInvokeCallback callback = new HubInvokeCallback() {
            @Override
            public void OnResult(boolean succeeded, String response) {
                Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void OnError(Exception ex) {
                Toast.makeText(MainActivity.this, "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        List<String> args = new ArrayList<String>(2);
        args.add("client");
        String message = new String("SetVolume;").concat(Integer.toString(progress));
        args.add(message);
        hub.Invoke("Send", args, callback);
    }
}

package org.kde.nettytest;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;


public class Client extends ActionBarActivity {

    Button connect, disconnect, send;
    EditText port, remoteAddress1,remoteAddress2,remoteAddress3,remoteAddress4, textToSend;
    TextView messageReceived;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        connect = (Button)findViewById(R.id.bConnect);
        disconnect = (Button)findViewById(R.id.bDisconnect);
        send = (Button)findViewById(R.id.bCSend);

        port = (EditText)findViewById(R.id.etCPort);
        remoteAddress1 = (EditText)findViewById(R.id.etRemoteAddress1);
        remoteAddress2 = (EditText)findViewById(R.id.etRemoteAddress2);
        remoteAddress3 = (EditText)findViewById(R.id.etRemoteAddress3);
        remoteAddress4 = (EditText)findViewById(R.id.etRemoteAddress4);
        textToSend = (EditText)findViewById(R.id.etCTextToSend);

        messageReceived = (TextView)findViewById(R.id.tvCMessageReceived);

        disconnect.setEnabled(false);
        send.setEnabled(false);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                connect.setEnabled(false);
                disconnect.setEnabled(true);
                send.setEnabled(true);


            }
        });

        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                disconnect.setEnabled(false);
                connect.setEnabled(true);
                send.setEnabled(false);


            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_client, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

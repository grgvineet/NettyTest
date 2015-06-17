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
import java.util.HashMap;


public class Server extends ActionBarActivity {

    boolean binded = false;

    Button bind, unbind, send;
    EditText port, textToSend;
    TextView messageReceived;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        bind = (Button)findViewById(R.id.bBind);
        unbind = (Button)findViewById(R.id.bUnbind);
        send = (Button)findViewById(R.id.bSSend);

        port = (EditText)findViewById(R.id.etSPort);
        textToSend = (EditText)findViewById(R.id.etSTextToSend);

        messageReceived = (TextView)findViewById(R.id.tvSMessageReceived);

        unbind.setEnabled(false);
        send.setEnabled(false);


        bind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    if (!binded) {


                        bind.setEnabled(false);
                        unbind.setEnabled(true);
                        send.setEnabled(true);
                        binded = true;
                    }
                }catch (Exception e){
                    Toast.makeText(Server.this,"Error binding port",Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });

        unbind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binded) {

                    bind.setEnabled(true);
                    unbind.setEnabled(false);
                    send.setEnabled(false);
                    binded = false;
                }
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });


    }


    @Override
    public void finish() {
        super.finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_server, menu);
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

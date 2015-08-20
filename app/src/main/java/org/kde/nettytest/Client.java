package org.kde.nettytest;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.JdkSslClientContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;


public class Client extends Activity {

    Button connect, disconnect, send;
    EditText port, remoteAddress1,remoteAddress2,remoteAddress3,remoteAddress4, textToSend;
    TextView messageReceived;

    Thread clientThread;
    Channel channel;

    public class ClientThread extends Thread{

        String host;
        int port;

        ClientThread(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public void run() {
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap();
                b.group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ClientInitializer(host, port));

                // Start the connection attempt.
                channel = b.connect(host, port).sync().channel();

                // Wait until channel is closed
                channel.closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // The connection is closed automatically on shutdown.
                group.shutdownGracefully();
            }
        }
    }

    public class ClientHandler extends SimpleChannelInboundHandler<String> {

        @Override
        public void channelActive(final ChannelHandlerContext ctx) throws Exception {
            Log.e("Client Handler", "Channel Active");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), ctx.channel().remoteAddress().toString(),Toast.LENGTH_SHORT).show();
                    connect.setEnabled(false);
                    disconnect.setEnabled(true);
                    send.setEnabled(true);
                }
            });
        }


        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {

            Log.e("Client Handler", "Channel inactive");
            channel = null;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connect.setEnabled(true);
                    disconnect.setEnabled(false);
                    send.setEnabled(false);
                    Toast.makeText(getApplicationContext(), "Session destroyed", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            Log.e("Client Handler", "Exception caught");
            cause.printStackTrace();
            ctx.close();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, final String msg) throws Exception {
            Log.e("Client Handler", "Channel read");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    messageReceived.setText(msg);
                }
            });
        }
    }

    public class ClientInitializer extends ChannelInitializer<Channel> {

        String host;
        int port;

        public ClientInitializer (String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public void initChannel(Channel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();

            JdkSslClientContext sslContext = new JdkSslClientContext(InsecureTrustManagerFactory.INSTANCE);
            pipeline.addLast(sslContext.newHandler(ch.alloc(), host, port));

            pipeline.addLast(new DelimiterBasedFrameDecoder(65 * 1024, Delimiters.lineDelimiter()));
            pipeline.addLast(new StringDecoder());
            pipeline.addLast(new StringEncoder());

            // and then business logic.
            pipeline.addLast(new ClientHandler());
        }
    }

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

                String host = remoteAddress1.getText().toString() + "."
                        + remoteAddress2.getText().toString() + "."
                        + remoteAddress3.getText().toString() + "."
                        + remoteAddress4.getText().toString();

                try {
                    clientThread = new ClientThread(host, Integer.parseInt(port.getText().toString()));
                    clientThread.start();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Unable to connect", Toast.LENGTH_LONG);
                }

            }
        });

        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clientThread.interrupt();

            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (channel != null) {
                    Log.e("Client", "Writing  : " + textToSend.getText().toString());
                    channel.writeAndFlush(textToSend.getText().toString() + "\r\n");
                }
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

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

import java.net.InetAddress;

import javax.net.ssl.SSLEngine;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;


public class Server extends Activity {

    boolean binded = false;

    Button bind, unbind, send;
    EditText port, textToSend;
    TextView messageReceived;

    ServerThread thread;
    final static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public class ServerHandler extends SimpleChannelInboundHandler<String> {

        @Override
        public void channelActive(final ChannelHandlerContext ctx) {
            // Once session is secured, send a greeting and register the channel to the global channel
            // list so the channel received the messages from others.
            Log.e("Server", "Channel handler context name :" + ctx.name());
            ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(
                    new GenericFutureListener<Future<Channel>>() {
                        @Override
                        public void operationComplete(Future<Channel> future) throws Exception {
                            ctx.writeAndFlush(
                                    "Welcome to " + InetAddress.getLocalHost().getHostName() + " secure chat service!\n");
                            ctx.writeAndFlush(
                                    "Your session is protected by " +
                                            ctx.pipeline().get(SslHandler.class).engine().getSession().getCipherSuite() +
                                            " cipher suite.\n");

                            channels.add(ctx.channel());
                        }
                    });

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), ctx.channel().remoteAddress().toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }

        @Override
        protected void channelRead0(final ChannelHandlerContext ctx, final String msg) throws Exception {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    messageReceived.setText(ctx.channel().remoteAddress() + " : " + msg);
                }
            });

            // Close the connection if the client has sent 'bye'.
            if ("bye".equals(msg.toLowerCase())) {
                ctx.close();
            }
        }
    }

    public class ServerInitializer extends ChannelInitializer<SocketChannel> {

        private final SSLEngine sslEngine;

        public ServerInitializer(SSLEngine sslEngine) {
            this.sslEngine = sslEngine;
        }

        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();

            // Add SSL handler first to encrypt and decrypt everything.
            // In this example, we use a bogus certificate in the server side
            // and accept any invalid certificates in the client side.
            // You will need something more complicated to identify both
            // and server in the real world.

//            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
            pipeline.addLast(new SslHandler(sslEngine));

            // On top of the SSL handler, add the text line codec.
            pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
            pipeline.addLast(new StringDecoder());
            pipeline.addLast(new StringEncoder());

            // and then business logic.
            pipeline.addLast(new ServerHandler());
        }
    }

    public class ServerThread extends Thread{

        int port;

        ServerThread(int port) {
            this.port = port;
        }

        @Override
        public void run(){
            EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap(); // (2)
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class) // (3)
                        .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                final SSLEngine sslEngine = MySslContext.getSslContext(getApplicationContext(), false).createSSLEngine();

                                sslEngine.setUseClientMode(false);
                                ch.pipeline().addLast(new ServerInitializer(sslEngine));
                            }
                        })
                        .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                        .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

                // Bind and start to accept incoming connections.
                ChannelFuture f = b.bind(port).sync(); // (7)

                // Wait until the server socket is closed.
                // In this example, this does not happen, but you can do that to gracefully
                // shut down your server.
                f.channel().closeFuture().sync();
            }catch (Exception e){
                e.printStackTrace();
            } finally {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
        }
    }


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

                        thread = new ServerThread(Integer.parseInt(port.getText().toString()));
                        thread.start();
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

                    thread.interrupt();
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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (Channel c : channels){
                            c.writeAndFlush(textToSend.getText().toString() + "\r\n");
                        }
                    }
                }).start();
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

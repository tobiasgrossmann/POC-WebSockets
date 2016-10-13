package com.q_perior.jsocket.client;


import android.os.*;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;



import org.apache.http.message.BasicNameValuePair;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.client.plugins.rpc.Rpc;
import org.jwebsocket.client.plugins.rpc.RpcListener;
import org.jwebsocket.client.plugins.rpc.Rrpc;
import org.jwebsocket.client.token.BaseTokenClient;
import org.jwebsocket.token.Token;


import java.net.URI;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;



public class MainActivity extends Activity implements WebSocketClientTokenListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.
        ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);
        TextView txtInfo = (TextView)findViewById(R.id.lblInfo);
        txtInfo.setText((CharSequence) "High speed tcp socket communication client");

        try {
            //getWebSocketDebugListener();
            BaseTokenClient btc = new BaseTokenClient();//create a new instance os the base token client
            btc.addListener(this);//add this class as a listener
            btc.addListener(new RpcListener());//add an rpc listener
            Rpc.setDefaultBaseTokenClient(btc);//set it to the default btc
            Rrpc.setDefaultBaseTokenClient(btc);//same here
            btc.open("ws://10.0.2.2:8787/jWebSocket/jWebSocket");//try to open the connection to your server
        } catch (Exception e) {
            e.printStackTrace();
        }

       return;


    }

    /* Message handler is used to process incoming tokens from the server*/
    private Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message aMessage) {
            if(aMessage.what==0){//if it's a token
                Token aToken =(Token) aMessage.obj;//get the received token
                //if the namespace matches plugin
                if(aToken.getNS().equals("com.q_perior.jsocket.server")){
                    //and it's a command that the time has changed
                    if(aToken.getString("reqType").equals("timeHasChanged")){
                        long value=aToken.getLong("value");//get the  value
                        TextView txtInfo = (TextView)findViewById(R.id.lblTimer);

                        Date date = new Date(value);
                        Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
                        txtInfo.setText((CharSequence) Long.toString(value) + " " + format.format(date).toString() );
                    }
                }
            }

        }
    };

    @Override
    public void processOpened(WebSocketClientEvent aEvent) {
        System.out.println("Client is connected");//if the connection is established
    }

    @Override
    public void processClosed(WebSocketClientEvent aEvent) {
        System.out.println("Client is disconnected");//if the connection is closed
    }

    /* The following Methods are not needed now, but have to be there */
    @Override
    public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
    }


    @Override
    public void processToken(WebSocketClientEvent aEvent, Token aToken) {
        //for some reason you can't process the token directly
        //you have to use the messagehandler
        Message lMsg = new Message();//create a new mess
        lMsg.what = 0;
        lMsg.obj = aToken;
        messageHandler.sendMessage(lMsg);
    }

    @Override
    public void processOpening(WebSocketClientEvent aEvent) {
    }

    @Override
    public void processReconnecting(WebSocketClientEvent aEvent) {
    }



    //use this method for debugging. It starts a low level websocket implementation - quit useful as jWebsocket sometimes silently fails.
    WebSocketClient webSocketDebugClient = null;
    public void getWebSocketDebugListener() throws Exception{
        List<BasicNameValuePair> extraHeaders = Arrays.asList(
                new BasicNameValuePair("Cookie", "session=abcd")
        );
        //10.0.2.2:8787 is localhost from the android sdk emulator
        webSocketDebugClient = new WebSocketClient(URI.create("ws://10.0.2.2:8787/jWebSocket/jWebSocket"), new WebSocketClient.Listener() {
            @Override
            public void onConnect() {
               return;
            }
            @Override
            public void onMessage(String message) {
                return;
            }

            @Override
            public void onMessage(byte[] data) {
                return;
            }
            @Override
            public void onDisconnect(int code, String reason) {
                return;
            }
            @Override
            public void onError(Exception error) {
                disconnectDebugClient();
                return;
            }

        }, extraHeaders )   ;
        webSocketDebugClient.connect();

        //webSocketClient.send("hello!");
        //webSocketClient.send(new byte[] { 0xDE, 0xAD, 0xBE, 0xEF });
        //webSocketClient.disconnect();
        return;
    }

    public final void disconnectDebugClient () {
        webSocketDebugClient.connect();
        return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }





}

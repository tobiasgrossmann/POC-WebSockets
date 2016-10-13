package com.q_perior.jsocket.server;

import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import java.util.Collection;
import javolution.util.FastList;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.token.BaseToken;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;


public class TimePushServer extends TokenPlugIn {

    //change the Apache logger to your Classname
    private static Logger mLog = Logging.getLogger(TimePushServer.class);
    // if you change the namespace, don't forget to change the ns_sample!
    private final static String NS_QPerior = "com.q_perior.jsocket.server";

    //List of all connected clients
    private Collection<WebSocketConnector> mClients;


    //Constructor
    public TimePushServer(PluginConfiguration aConfiguration) {
        super(aConfiguration);
        if (mLog.isDebugEnabled()) {
            mLog.debug("Instantiating PlugIn ...");
        }
        // specify your namespace
        this.setNamespace(NS_QPerior);
        mClients = new FastList<WebSocketConnector>().shared();


        //start the time sending thread
        ( new Thread() {

            public void run() {
                try {
                while(true){
                    //wait one sec
                    java.util.Date date= new java.util.Date();
                    sendMessageAsLongValueToAllConnectedClients(date.getTime());
                    Thread.currentThread().sleep(100);
                }
                } catch (Exception e) {
                    //do nothing
                }
            }
        }

        ).start();
        return;
    }

    //Method broadcasts a string to all connected clients
    private void sendMessageAsLongValueToAllConnectedClients (long value) {

         //Broadcast the new Value to all other Clients
        Token lToken = TokenFactory.createToken(BaseToken.TT_EVENT);
        lToken.setString("ns", NS_QPerior);
        lToken.setString("reqType", "timeHasChanged");
        lToken.setLong("value", value);
        broadcastToAll(lToken);
    }

    //Method broadcasts a token to all connected clients
    public void broadcastToAll(Token aToken) {
        for (WebSocketConnector lConnector : mClients) {
            getServer().sendToken(lConnector, aToken);
        }
    }

    @Override
    public void connectorStarted(WebSocketConnector aConnector) {
        // this method is called every time when a client
        // connected to the server
        mClients.add(aConnector);
        if (mLog.isDebugEnabled()) {
            mLog.debug("new client has registered: " + aConnector.getId());
        }
    }

    @Override
    public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
        // ensure that we do not keep any dead connectors in the list
        mClients.remove(aConnector);
        if (mLog.isDebugEnabled()) {
            mLog.debug("client " + aConnector.getId() + " is gone");
        }
    }



}
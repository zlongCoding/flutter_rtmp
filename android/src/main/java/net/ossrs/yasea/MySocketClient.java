package net.ossrs.yasea;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * @author 张阳阳
 * @description:
 * @date :2020/7/31 12:50
 */
public abstract class MySocketClient extends WebSocketClient {
    public MySocketClient(URI serverUri) {

        super(serverUri,new Draft_6455());
    }
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.e("MySocketClient", "onOpen()");
    }



    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.e("MySocketClient", "onClose()");
    }

    @Override
    public void onError(Exception ex) {
        Log.e("MySocketClient", "onError()");
    }
}
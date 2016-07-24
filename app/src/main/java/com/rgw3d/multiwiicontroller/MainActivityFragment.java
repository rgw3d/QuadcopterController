package com.rgw3d.multiwiicontroller;

import android.os.Build;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private JoystickView joystick;
    private JoystickView joystick1;
    private TextView rollView;
    private TextView pitchView;
    private TextView yawView;
    private TextView throttleView;
    private TriToggleButton aux1View;
    private TriToggleButton aux2View;
    private TriToggleButton aux3View;
    private TriToggleButton aux4View;


    private WebSocketControl webSocketController;
    private WebSocketClient mWebSocketClient;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container,false);

        rollView = (TextView) view.findViewById(R.id.rollText);
        pitchView = (TextView) view.findViewById(R.id.pitchText);
        yawView = (TextView) view.findViewById(R.id.yawText);
        throttleView = (TextView) view.findViewById(R.id.throttleText);

        aux1View = (TriToggleButton) view.findViewById(R.id.auxButton1) ;
        aux2View = (TriToggleButton) view.findViewById(R.id.auxButton2) ;
        aux3View = (TriToggleButton) view.findViewById(R.id.auxButton3) ;
        aux4View = (TriToggleButton) view.findViewById(R.id.auxButton4) ;

        joystick = (JoystickView) view.findViewById(R.id.joystickView);

        joystick1 = (JoystickView) view.findViewById(R.id.joystickView1);


               webSocketController = new WebSocketControl();

        return view;
    }

    private class WebSocketControl{
        private int roll = 1500;
        private int pitch = 1500;
        private int yaw = 1500;
        private int throttle = 1500;
        private int aux1 = 1500;
        private int aux2 = 1500;
        private int aux3 = 1500;
        private int aux4 = 1500;

        private boolean isConnected = false;

        public WebSocketControl(){
            if(joystick== null || joystick1==null)//dont mess with null
                return;
            joystick.setOnJoystickMoveListener( new JoystickView.OnJoystickMoveListener() {
                @Override
                public void onValueChanged(int joystickRadius, int currx, int curry, int centerx, int centery) {
                    int t = (int)Math.round(1500+500.0*((centery-curry)/(double)joystickRadius));
                    int y = (int)Math.round(1500+500.0*((currx-centerx)/(double)joystickRadius));
                    t = roundToTens(t);
                    y = roundToTens(y);

                    if(t!=throttle || y!=throttle){
                        sendMessage();
                    }
                    if(t!=throttle) {
                        throttle = t;
                        setUIText(throttleView, String.format(Locale.US, "Throttle: %d", throttle));
                    }
                    if(y!=yaw){
                        yaw = y;
                        setUIText(yawView, String.format(Locale.US, "Yaw: %d", yaw));
                    }
                }
            });

            joystick1.setOnJoystickMoveListener( new JoystickView.OnJoystickMoveListener() {

                @Override
                public void onValueChanged(int joystickRadius, int currx, int curry, int centerx, int centery) {
                    //angle = Math.round(angle);
                    //Log.d("angle",angle+"");
                    int p = (int)Math.round(1500+500.0*((centery-curry)/((double)joystickRadius)));//(int) (1500+(500*(Math.sin(Math.toRadians(angle)) * power)));
                    int r = (int)Math.round(1500+500.0*((currx-centerx)/((double)joystickRadius)));//(int) (1500+(500*(Math.cos(Math.toRadians(angle)) * power)));
                    p = roundToTens(p);
                    r = roundToTens(r);

                    if(p!=pitch || r != roll)
                        sendMessage();
                    if (p != pitch){
                        pitch = p;
                        setUIText(pitchView,String.format(Locale.US,"Pitch: %d",pitch));
                    }
                    if(r!=roll){
                        roll = r;
                        setUIText(rollView,String.format(Locale.US,"Roll: %d",roll));
                    }
                }
            });

            aux1View.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TriToggleButton ttb = (TriToggleButton) v;
                    aux1 = ttb.getValue();
                    sendMessage();
                }
            });
            aux2View.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TriToggleButton ttb = (TriToggleButton) v;
                    aux2 = ttb.getValue();
                    sendMessage();
                }
            });
            aux3View.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TriToggleButton ttb = (TriToggleButton) v;
                    aux3 = ttb.getValue();
                    sendMessage();
                }
            });
            aux4View.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TriToggleButton ttb = (TriToggleButton) v;
                    aux4 = ttb.getValue();
                    sendMessage();
                }
            });

            new Thread(new Runnable() {
                @Override
                public void run() {
                    getConnected();
                }
            }).start();

        }

        private void sendData(int milliseconds){
            while(isConnected) {
                sendMessage();
                Log.d("sending message","send message at millisecond time");
                try {
                    Thread.sleep(milliseconds);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                /*new Thread(new Runnable() {
                    @Override
                    public void run() {
                    }
                }).start();
                try {
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                */
            }
        }

        private void getConnected(){
            while(!isConnected) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        isConnected = connectWebSocket();
                    }
                }).start();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //sendData(100);
        }

        private void setUIText(final TextView tv,final String text ){
            getActivity().runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    tv.setText(text);
                }
            });
        }

        private int roundToTens(int in){
            int out = in;
            out = (out/10)*10;

            int ones = in%10;
            if(ones>=5)//round up
                out+=10;

            //Log.d("in vs out",in + "  "+out);
            return out;
        }

        private boolean connectWebSocket() {
            URI uri;
            try {
                uri = new URI("ws://192.168.10.1:80");
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return false;
            }
            mWebSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    Log.i("Websocket", "Opened");
                    mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
                }

                @Override
                public void onMessage(String s) {
                    Log.i("Websocket","Got Message: " + s);
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    Log.i("Websocket", "Closed " + s);
                }

                @Override
                public void onError(Exception e) {
                    Log.i("Websocket", "Error " + e.getMessage());
                }
            };

            try {
                return mWebSocketClient.connectBlocking();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
        public void sendMessage() {
            if(isConnected)
                mWebSocketClient.send(String.format("%d,%d,%d,%d,%d,%d,%d,%d",roll,pitch,yaw,throttle,aux1,aux2,aux3,aux4));
        }
    }

}

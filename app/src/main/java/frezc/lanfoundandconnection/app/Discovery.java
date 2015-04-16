package frezc.lanfoundandconnection.app;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by freeze on 2015/4/14.
 */
public class Discovery{
    private int maxWaitTime;
    private int retryTimes = -1;
    private boolean isDiscoverying;
    private boolean serverRunning,clientRunning;
    private MulticastSocket postSocket,receiveSocket;
    private DatagramPacket ownData;
    private int ttl;
    private InetAddress broadcastAddress;
    private int port;
    private OnNewFoundListener onNewFoundListener;
    private OnDiscoveryStartListener onDiscoveryStartListener;
    private OnDiscoveryStopListener onDiscoveryStopListener;
    private DeviceInfo deviceInfo = new DeviceInfo("DefaultName");
    private DeviceInfo foundDevice;


    public interface OnNewFoundListener{
        void onFound(InetAddress address, DeviceInfo deviceInfo);
        void onFailed();
    }

    public interface OnDiscoveryStartListener{
        void onStartSucceed();
        void onStartFailed(String failMsg);
    }

    public interface OnDiscoveryStopListener{
        void onStopFailed(String failMsg);
        void onStopCompleted();
    }

    public static class DeviceInfo{
        public String deviceName;
        public DeviceInfo(String n){
            deviceName = n;
        }

        public DeviceInfo(byte[] buf){
            deviceName = new String(buf);
        }

        public boolean equals(DeviceInfo o) {
            return deviceName.equals(o);
        }

        public boolean equals(byte[] bytes){
            return deviceName.equals(new String(bytes));
        }

        public byte[] getBytes(){
            return deviceName.getBytes();
        }
    }

    /**
     * Single Server instance
     */
    private Thread Server;

    /**
     * Single Client instance
     */
    private Thread Client;

    /**
     * 224.0.0.0--239.255.255.255
     * is Available
     */
    public Discovery(InetAddress broadcastAddress, int maxWaitTime,
                     DeviceInfo deviceInfo, OnNewFoundListener listener) throws IOException {
        this.broadcastAddress = broadcastAddress;
        this.maxWaitTime = maxWaitTime;
        this.deviceInfo = deviceInfo;
        onNewFoundListener = listener;
        init();
    }

    public Discovery(InetAddress broadcastAddress, int maxWaitTime, OnNewFoundListener listener) throws IOException {
        this.broadcastAddress = broadcastAddress;
        this.maxWaitTime = maxWaitTime;
        onNewFoundListener = listener;
        init();
    }

    /**
     * default wait time 5000ms
     * @param broadcastAddress
     * @param listener
     */
    public Discovery(InetAddress broadcastAddress, OnNewFoundListener listener) throws IOException {
        this(broadcastAddress, 5000, listener);
    }

    private void init() throws IOException {
        isDiscoverying = false;
        serverRunning = false;
        clientRunning = false;
        port = 6877;
        ttl = 1;

        receiveSocket = new MulticastSocket(port);
        receiveSocket.joinGroup(broadcastAddress);
        receiveSocket.setLoopbackMode(false);
        receiveSocket.setTimeToLive(ttl);
        receiveSocket.setSoTimeout(maxWaitTime);

        postSocket = new MulticastSocket(port+11);
        postSocket.joinGroup(broadcastAddress);
        postSocket.setLoopbackMode(false);
        postSocket.setTimeToLive(ttl);
        postSocket.setSoTimeout(maxWaitTime);

        initThread();
    }

    private void initThread() {
        Server = new Thread(){
            @Override
            public void run() {
                serverRunning = true;
                super.run();
                byte buf[] = new byte[1024];
                DatagramPacket receiveData = new DatagramPacket(buf, 1024);
                int times = retryTimes;
                while(isDiscoverying){
                    try {
                        receiveSocket.receive(receiveData);
                        if(foundDevice == null || !foundDevice.equals(buf)){
                            foundDevice = new DeviceInfo(buf);
                            onNewFoundListener.onFound(receiveData.getAddress(), foundDevice);
                        }
                    } catch (IOException e) {
//                        e.printStackTrace();
                        if(times > 0){
                            times--;
                        }else if(times == -1){

                        }else{
                            onNewFoundListener.onFailed();
                            isDiscoverying = false;
                            break;
                        }
                    }
                }
                serverRunning = false;
                if(!clientRunning && onDiscoveryStopListener!=null){
                    onDiscoveryStopListener.onStopCompleted();
                }
            }
        };

        Client = new Thread(){
            @Override
            public void run() {
                clientRunning = true;
                super.run();
                ownData = new DatagramPacket(deviceInfo.getBytes(), deviceInfo.getBytes().length,
                        broadcastAddress, port);
                while(isDiscoverying){
                    try {
                        postSocket.send(ownData);
                        sleep(3000);
                    }catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                clientRunning = false;
                if(!serverRunning && onDiscoveryStopListener!=null){
                    onDiscoveryStopListener.onStopCompleted();
                }
            }
        };
    }

    /**
     * default port is 6777
     * @param port custom your app port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * default ttl is 1.
     * @param ttl
     */
    public void setTimeToLive(int ttl){
        this.ttl = ttl;
    }

    public void setSoTimeout(int milliseconds){
        this.maxWaitTime = milliseconds;
    }

    /**
     * default set -1
     * @param retryTimes infinite times as -1
     */
    public void setRetryTimes(int retryTimes){
        this.retryTimes = retryTimes;
    }

    public void addOnDiscoveryStartListener(OnDiscoveryStartListener onDiscoveryStartListener){
        this.onDiscoveryStartListener = onDiscoveryStartListener;
    }

    public void addOnDiscoveryStopListener(OnDiscoveryStopListener onDiscoveryStopListener){
        this.onDiscoveryStopListener = onDiscoveryStopListener;
    }

    public void startDiscovery() throws IOException {
        if(isDiscoverying || serverRunning || clientRunning){
            if(onDiscoveryStartListener != null) {
                onDiscoveryStartListener.onStartFailed("Start failed, discovery is running...");
            }
        }else {
            isDiscoverying = true;
            Server.start();
            Client.start();
            if(onDiscoveryStartListener != null){
                onDiscoveryStartListener.onStartSucceed();
            }
        }
    }

    public void stopDiscovery(){
        if(isDiscoverying){
            isDiscoverying = false;
        }else {
            if(onDiscoveryStopListener != null){
                onDiscoveryStopListener.onStopFailed("Discovery has been stopped! Invalid operation.");
            }
        }
    }

    public boolean isDiscovery(){
        return isDiscoverying;
    }


}

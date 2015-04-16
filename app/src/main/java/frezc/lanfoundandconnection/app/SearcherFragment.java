package frezc.lanfoundandconnection.app;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;
import com.skyfishjy.library.RippleBackground;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Created by freeze on 2015/4/15.
 */
public class SearcherFragment extends Fragment{
    private Discovery discovery;
    private ImageView foundDevice;
    private RippleBackground rippleBackground;
//    private ConnectivityManager connectivityManager;
//    private boolean hasConnecting=false;

    public static SearcherFragment newInstance(String deviceName){
        SearcherFragment fragment = new SearcherFragment();
        Bundle bundle = new Bundle();
        bundle.putString("info", deviceName);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        final Handler handler = new Handler();
//        connectivityManager =
//                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        String name = bundle.getString("info");
        if(name == null){
            name = "Default";
        }
        Discovery.DeviceInfo deviceInfo = new Discovery.DeviceInfo(name);
        try {
            discovery = new Discovery(InetAddress.getByName("230.12.12.12"), 5000, deviceInfo,
                    new Discovery.OnNewFoundListener() {
                @Override
                public void onFound(InetAddress address, Discovery.DeviceInfo deviceInfo) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            foundDevice();
                        }
                    });
                }

                @Override
                public void onFailed() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(),"found failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_searcher, container, false);
        foundDevice = (ImageView) v.findViewById(R.id.foundDevice);
        rippleBackground = (RippleBackground) v.findViewById(R.id.content);
        v.findViewById(R.id.ownDevice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (!hasConnecting)
//                    Toast.makeText(getActivity(), "当前无网络连接", Toast.LENGTH_SHORT).show();
                if (discovery.isDiscovery()) {
                    discovery.stopDiscovery();
                    rippleBackground.stopRippleAnimation();
                } else {
                    try {
                        discovery.startDiscovery();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    rippleBackground.startRippleAnimation();
                }
            }
        });
        return v;
    }



    private void foundDevice(){
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(400);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        ArrayList<Animator> animatorList = new ArrayList<Animator>();
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(foundDevice, "ScaleX", 0f, 1.2f, 1f);
        animatorList.add(scaleXAnimator);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(foundDevice, "ScaleY", 0f, 1.2f, 1f);
        animatorList.add(scaleYAnimator);
        animatorSet.playTogether(animatorList);
        foundDevice.setVisibility(View.VISIBLE);
        animatorSet.start();
    }

//    @Override
//    public void run() {
//        while(!hasConnecting){
//            if(connectivityManager.getActiveNetworkInfo() != null)
//                hasConnecting = true;
//            try {
//                Thread.currentThread().sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}

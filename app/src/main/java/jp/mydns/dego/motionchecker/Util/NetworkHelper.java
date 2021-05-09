package jp.mydns.dego.motionchecker.Util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import jp.mydns.dego.motionchecker.InstanceHolder;

public class NetworkHelper {

    // ---------------------------------------------------------------------------------------------
    // Constant Value
    // ---------------------------------------------------------------------------------------------
    private static final String TAG = "NetworkHelper";

    // ---------------------------------------------------------------------------------------------
    // Private Fields
    // ---------------------------------------------------------------------------------------------

    // ---------------------------------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------------------------------

    // ---------------------------------------------------------------------------------------------
    // Public Method
    // ---------------------------------------------------------------------------------------------

    /**
     * netWorkCheck
     *
     * @return can access network
     */
    public static boolean netWorkCheck() {
        DebugLog.d(TAG, "netWorkCheck");

        Context context = InstanceHolder.getInstance().getApplicationContext();
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.isConnected();
        } else {
            return false;
        }
    }
}

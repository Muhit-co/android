package co.muhit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Class to help listen to changes in internet connectivity
 * http://stackoverflow.com/questions/31689513/broadcastreceiver-to-detect-network-is-connected
 */
public abstract class NetworkReceiver extends BroadcastReceiver {

    private final Context context;

    protected NetworkReceiver(Context context) {
        this.context = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                onInternetAvailable();
            } else if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                onDisconnected();
            }
        }
    }

    abstract void onInternetAvailable();

    abstract void onDisconnected();

    /**
     * Determines whether there is a network connection available
     *
     * @return
     */
    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}

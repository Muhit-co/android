package co.muhit;

import android.app.Application;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

/**
 * The Application class. It's used to configure cookie usage.
 */
public class MuhitApplication extends Application {

    public MuhitApplication() {
        // this method fires only once per application start.
        // getApplicationContext still returns null during the constructor

        Log.d("MuhitApplication", "Constructor fired");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // this method also only fires once
        // but the application has a context at this point

        CookieManager.setAcceptFileSchemeCookies(true);
        CookieSyncManager.createInstance(getApplicationContext());
        Log.d("MuhitApplication", "CookieSyncManager created");
    }
}

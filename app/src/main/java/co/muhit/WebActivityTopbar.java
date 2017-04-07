//package co.muhit;
//
//import android.net.Uri;
//import android.os.Bundle;
//import android.support.customtabs.CustomTabsIntent;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
//
///**
// * This activity shows the website in a Chrome Custom Tab
// */
//public class WebActivityTopbar extends AppCompatActivity {
//    private String url;
//
//    public void onBackPressed() {
//        super.onBackPressed();
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        url = getString(R.string.web_url);
//
//        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
//        builder.setToolbarColor(ContextCompat.getColor(getApplicationContext(), R.color.muhitPrimary));
//
//        CustomTabsIntent customTabsIntent = builder.build();
//        customTabsIntent.launchUrl(this, Uri.parse(url));
//
//        // Finish this activity and let the Chrome Custom Tab handle the rest
//        finish();
//    }
//}
package admob.plus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.provider.Settings;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class AdMobHelper {
    private final Adapter mAdapter;

    public AdMobHelper(Adapter adapter) {
        mAdapter = adapter;
    }

    public static double dpToPx(double dp) {
        return dp * Resources.getSystem().getDisplayMetrics().density;
    }

    public static int pxToDp(int px) {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

    private static String md5(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            BigInteger bigInt = new BigInteger(1, digest.digest());
            return String.format("%32s", bigInt.toString(16)).replace(' ', '0');
        } catch (NoSuchAlgorithmException ignore) {
        }
        return "";
    }

    public boolean isRunningInTestLab() {
        String testLabSetting = Settings.System.getString(mAdapter.getActivity().getContentResolver(), "firebase.test.lab");
        return "true".equals(testLabSetting);
    }

    public void configForTestLab() {
        if (!isRunningInTestLab()) {
            return;
        }
        RequestConfiguration config = MobileAds.getRequestConfiguration();
        List<String> testDeviceIds = config.getTestDeviceIds();

        final String deviceId = getDeviceId();
        if (testDeviceIds.contains(deviceId)) {
            return;
        }
        testDeviceIds.add(deviceId);

        RequestConfiguration.Builder builder = config.toBuilder();
        builder.setTestDeviceIds(testDeviceIds);
        MobileAds.setRequestConfiguration(builder.build());
    }

    public RequestConfiguration buildRequestConfiguration(JSONObject cfg) {
        RequestConfiguration.Builder builder = new RequestConfiguration.Builder();
        if (cfg.has("maxAdContentRating")) {
            builder.setMaxAdContentRating(cfg.optString("maxAdContentRating"));
        }
        Integer tagForChildDirectedTreatment = intFromBool(cfg, "tagForChildDirectedTreatment",
                RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED,
                RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE,
                RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE);
        if (tagForChildDirectedTreatment != null) {
            builder.setTagForChildDirectedTreatment(tagForChildDirectedTreatment);
        }
        Integer tagForUnderAgeOfConsent = intFromBool(cfg, "tagForUnderAgeOfConsent",
                RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_UNSPECIFIED,
                RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE,
                RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_FALSE);
        if (tagForUnderAgeOfConsent != null) {
            builder.setTagForUnderAgeOfConsent(tagForUnderAgeOfConsent);
        }
        if (cfg.has("testDeviceIds")) {
            List<String> testDeviceIds = new ArrayList<String>();
            JSONArray ids = cfg.optJSONArray("testDeviceIds");
            for (int i = 0; i < ids.length(); i++) {
                String testDeviceId = ids.optString(i);
                if (testDeviceId != null) {
                    testDeviceIds.add(testDeviceId);
                }
            }
            builder.setTestDeviceIds(testDeviceIds);
        }
        return builder.build();
    }

    @NonNull
    private String getDeviceId() {
        // This will request test ads on the emulator and device by passing this hashed device ID.
        @SuppressLint("HardwareIds") String ANDROID_ID = Settings.Secure.getString(mAdapter.getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
        return md5(ANDROID_ID).toUpperCase();
    }

    @Nullable
    private Integer intFromBool(JSONObject cfg, String name, int vNull, int vTrue, int vFalse) {
        if (!cfg.has(name)) {
            return null;
        }
        if (cfg.opt(name) == null) {
            return vNull;
        }
        if (cfg.optBoolean(name)) {
            return vTrue;
        }
        return vFalse;
    }

    public interface Adapter {
        Activity getActivity();
    }
}

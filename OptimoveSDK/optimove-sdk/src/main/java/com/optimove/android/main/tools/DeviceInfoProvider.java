package com.optimove.android.main.tools;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.optimove.android.main.tools.opti_logger.OptiLoggerStreamsContainer;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.WIFI_SERVICE;

public class DeviceInfoProvider {

    private final Context context;

    public DeviceInfoProvider(Context context) {
        this.context = context;
    }

    public String getDeviceLanguage() {
        return Locale.getDefault().toString();
    }

    @Nullable
    public String getIP(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        int ipAddress;
        try {
            ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        } catch (SecurityException e) {
            return null;
        } catch (Exception e) {
            return null;
        }

        // Convert little-endian to big-endianif needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            ipAddressString = null;
        }

        return ipAddressString;
    }

    @Nullable
    public Location getDeviceLocation(Context context) {
        LocationManager locationManager =
                (LocationManager) context.getApplicationContext().getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);

        if (bestProvider == null) {
            return null;
        }
        try {
            return locationManager.getLastKnownLocation(bestProvider);
        } catch (SecurityException e) {
            return null;
        }
    }

    public String getCityNameFromLocation(Context context, @NonNull Location location){
        Geocoder gcd = new Geocoder(context, Locale.ENGLISH);

        try {
            List<Address> addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses.size() > 0) {
                return addresses.get(0).getLocality();
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public String getUserAgent(){
        try {
            return System.getProperty("http.agent");
        } catch (Throwable throwable) {
            OptiLoggerStreamsContainer.error("Cannot get user agent - %s", throwable.getMessage());
            return null;
        }
    }

}

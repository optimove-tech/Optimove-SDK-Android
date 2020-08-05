package com.optimove.sdk.optimove_sdk.main.tools;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLogger;
import com.optimove.sdk.optimove_sdk.optitrack.OptistreamEvent;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OptiUtils {

  public static long currentTimeSeconds() {
    return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
  }

  public static void runOnMainThreadIfOnWorker(Runnable runnable) {
    if (!isRunningOnMainThread()) {
      runOnMainThread(runnable);
    } else {
      runnable.run();
    }
  }

  public static void runOnMainThread(Runnable runnable) {
    new Handler(Looper.getMainLooper()).post(runnable);
  }

  public static boolean isRunningOnMainThread() {
    Looper currentLooper = Looper.myLooper();
    return currentLooper != null && currentLooper.equals(Looper.getMainLooper());
  }

  public static boolean isEmptyOrWhitespace(String suspect) {
    return suspect == null || suspect.trim().isEmpty();
  }

  public static boolean isNullNoneOrUndefined(String suspect) {
    if (OptiUtils.isEmptyOrWhitespace(suspect)) {
      return true;
    }
    String lowerCase = suspect.trim().toLowerCase();
    return lowerCase.equals("null")
        || lowerCase.equals("undefined")
        || lowerCase.equals("none")
        || lowerCase.contains("undefine");
  }

  public static boolean isValidEmailAddress(String emailStr) {
    Matcher matcher = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE).matcher(emailStr);
    return matcher.find();
  }


  @Nullable
  public static Object getBuildConfig(String packageName, String key) {
    return getBuildConfig(packageName, key, null);
  }

  public static Object getBuildConfig(String packageName, String key, Object defaultValue) {
    Object result = defaultValue;
    try {
      Class<?> buildConfig = Class.forName(packageName.concat(".BuildConfig"));
      Field field = buildConfig.getDeclaredField(key);
      result = field.get(null);
    } catch (ClassNotFoundException e) {
      OptiLogger.f172();
    } catch (NoSuchFieldException e) {
      OptiLogger.f173(key);
    } catch (IllegalAccessException e) {
      OptiLogger.f174();
    } catch (Exception e) {
      OptiLogger.f175(e.getMessage());
    }
    return result;
  }

  //For a consumer of the SDK, will always return PROD, unless the consumer set the
  // OPTIMOVE_SDK_RUNTIME_ENV flag explicitly
  public static String getSdkEnv(String packageName) {
    String defaultValue = "prod";
    if (packageName == null) {
      return defaultValue;
    } else {
      return (String) OptiUtils.getBuildConfig(packageName,"OPTIMOVE_SDK_RUNTIME_ENV",defaultValue);
    }
  }

  public static String SHA1(String text) {
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("SHA-1");
      md.update(text.getBytes(StandardCharsets.ISO_8859_1), 0, text.length());
    } catch (NoSuchAlgorithmException e) {
      OptiLogger.f176();
      return text;
    }
    byte[] sha1hash = md.digest();
    return convertToHex(sha1hash);
  }

  private static String convertToHex(byte[] data) {
    StringBuilder buf = new StringBuilder();
    for (byte b : data) {
      int halfbyte = (b >>> 4) & 0x0F;
      int two_halfs = 0;
      do {
        buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
        halfbyte = b & 0x0F;
      } while (two_halfs++ < 1);
    }
    return buf.toString();
  }

}

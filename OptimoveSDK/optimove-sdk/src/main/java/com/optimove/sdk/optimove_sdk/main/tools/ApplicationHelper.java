package com.optimove.sdk.optimove_sdk.main.tools;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class ApplicationHelper {

  private static final Object LOCK = new Object();

  private static String cachedBasePackageName = null;

  public static String getFullPackageName(@NonNull Context context) {
    return context.getPackageName();
  }

  @Nullable
  public static String getBasePackageName(@NonNull Context context) {
    if (cachedBasePackageName != null) {
      return cachedBasePackageName;
    }
    synchronized (LOCK) {
      if (cachedBasePackageName == null) {
        String fullPackageName = getFullPackageName(context);
        // Most of the time, the full package name is also the base package name, so to be efficient, start by checking the full package name
        try {
          Class.forName(fullPackageName.concat(".BuildConfig"));
          // Hit!
          cachedBasePackageName = fullPackageName;
        } catch (ClassNotFoundException ignored) {
        }
        // The full package is not a match, break it up and search for the first minimal hit.
        if (cachedBasePackageName == null) {
          StringBuilder buildConfigFullNameBuilder = new StringBuilder(fullPackageName.length());
          for (String packageNameComponent : fullPackageName.split("\\.")) {
            buildConfigFullNameBuilder.append(packageNameComponent);
            try {
              String candidate = buildConfigFullNameBuilder.toString();
              Class.forName(candidate.concat(".BuildConfig"));
              // Hit!
              cachedBasePackageName = candidate;
              break;
            } catch (ClassNotFoundException e) {
              // BuildConfig was not found, we still haven't hit the base package name
              buildConfigFullNameBuilder.append(".");
            }
          }
        }
      }
    }
    return cachedBasePackageName;
  }
}

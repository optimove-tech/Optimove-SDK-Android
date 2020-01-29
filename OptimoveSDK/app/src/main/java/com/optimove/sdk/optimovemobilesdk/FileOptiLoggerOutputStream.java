package com.optimove.sdk.optimovemobilesdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;

import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.tools.FileUtils;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.LogLevel;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerOutputStream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An {@link OptiLoggerOutputStream} that stores logs in a local file.
 */
@SuppressLint("DefaultLocale")
public class FileOptiLoggerOutputStream implements OptiLoggerOutputStream {

  private static final Object lock = new Object();
  private static final String LOG_FILE_NAME = "log.txt";
  private static FileOptiLoggerOutputStream shared;
  private StringBuffer sessionLogBuilder;
  private int lastLogLength;
  private Timer autoSaveTimer;
  private FileUtils fileUtils;

  private FileOptiLoggerOutputStream(FileUtils fileUtils) {
    lastLogLength = 0;
    sessionLogBuilder = new StringBuffer();
    autoSaveTimer = new Timer();
    this.fileUtils = fileUtils;

  }

  public static FileOptiLoggerOutputStream getInstance(Context context, FileUtils fileUtils) {
    if (shared != null)
      return shared;
    synchronized (lock) {
      if (shared != null)
        return shared;
      shared = new FileOptiLoggerOutputStream(fileUtils);
      shared.init(context.getApplicationContext());
    }
    return shared;
  }

  private void init(Context context) {
    if (fileUtils.getFileSize(LOG_FILE_NAME, FileUtils.SourceDir.INTERNAL, context) >= 3) {
      fileUtils.deleteFile(context).named(LOG_FILE_NAME).from(FileUtils.SourceDir.INTERNAL).now();
    }

    sessionLogBuilder.append("~~~~~~~~===================== Starting new Log =====================~~~~~~~~\n");

    TimerTask timerTask = new TimerTask() {
      @Override
      public void run() {
        if (sessionLogBuilder.length() == lastLogLength)
          return;
        save();
        lastLogLength = sessionLogBuilder.length();
      }
    };
    autoSaveTimer.scheduleAtFixedRate(timerTask, 8000, 8000);
  }

  /**
   * Called to store any pending logs to the file
   */
  public void save() {
    synchronized (lock) {
      fileUtils.write(Optimove.getInstance().getApplicationContext(), sessionLogBuilder.toString())
          .to(LOG_FILE_NAME, true)
          .in(FileUtils.SourceDir.INTERNAL)
          .now();
      sessionLogBuilder = new StringBuffer();
    }
  }

  /**
   * Collects all stored logs (current session is excluded) and returns them via the {@link LogReadyListener}.
   *
   * @param logReadyListener receives the logs once they're ready
   */
  public void sendAndCleanLogs(final LogReadyListener logReadyListener) {
    if (sessionLogBuilder.length() > 0) {
      save();
    }

    final Context context = Optimove.getInstance().getApplicationContext();
    String log = fileUtils.readFile(context).named(LOG_FILE_NAME).from(FileUtils.SourceDir.INTERNAL).asString();
    if (log == null) {
      logReadyListener.onLogReady(false);
      return;
    }

    File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    File outputLogFile = new File(downloadsDir, LOG_FILE_NAME);
    new Thread(() -> {
      BufferedWriter bufferedWriter = null;
      try {
        bufferedWriter = new BufferedWriter(new FileWriter(outputLogFile));
        bufferedWriter.write(log);
        bufferedWriter.flush();
      } catch (IOException e) {
        logReadyListener.onLogReady(false);
      } finally {
        if (bufferedWriter != null) {
          try {
            bufferedWriter.close();
          } catch (IOException e) {
          }
        }
      }
      logReadyListener.onLogReady(true);
    }).start();
  }

  @Override
  public boolean isVisibleToClient() {
    return false;
  }

  @Override
  public void reportLog(LogLevel logLevel, String logClass, String logMethod, String message) {
    sessionLogBuilder.append(getLog(logLevel.name(), logClass, logMethod, message));
    sessionLogBuilder.append("\n");
  }

  @SuppressLint("SimpleDateFormat")
  private StringBuilder getLog(String level, String logClass, String logMethod, String message) {
    String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    StringBuilder logBuilder = new StringBuilder(now);
    logBuilder.append(" -> ");
    logBuilder.append(level);
    logBuilder.append(": ");
    logBuilder.append(logClass);
    logBuilder.append("/");
    logBuilder.append(logMethod);
    logBuilder.append(" - ");
    logBuilder.append(message);
    logBuilder.append("\n");

    logBuilder.append("\n");
    return logBuilder;
  }

  public interface LogReadyListener {
    void onLogReady(boolean success);
  }
}

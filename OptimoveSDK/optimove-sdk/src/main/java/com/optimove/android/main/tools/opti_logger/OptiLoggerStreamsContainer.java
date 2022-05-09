package com.optimove.android.main.tools.opti_logger;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import com.optimove.android.main.constants.TenantConfigsKeys;

import java.util.ArrayList;
import java.util.List;

import static com.optimove.android.main.constants.TenantConfigsKeys.TenantInfoKeys.TENANT_ID;

/**
 * Manages {@code logging} of the <i>Optimove SDK</i>.
 */
public final class OptiLoggerStreamsContainer {

    private static final String OPTI_LOGGER_STREAMS_CONTAINER_CLASS_NAME =
            String.format("%s.java", OptiLoggerStreamsContainer.class.getSimpleName());

    private static List<OptiLoggerOutputStream> loggerOutputStreams;
    private static LogLevel minLogLevelToShow; // Applies to client visible Logs only
    private static LogLevel minLogLevelRemote;

    static {
        loggerOutputStreams = new ArrayList<>();
        //default values, unless overridden
        minLogLevelToShow = LogLevel.WARN;
        minLogLevelRemote = LogLevel.FATAL;
    }

    public static void initializeLogger(Context context) {
        SharedPreferences coreSharedPreferences =
                context.getSharedPreferences(TenantConfigsKeys.CORE_SP_FILE, Context.MODE_PRIVATE);
        OptiLoggerStreamsContainer.addOutputStream(new RemoteLogsServiceOutputStream(context,
                coreSharedPreferences.getInt(TENANT_ID, -1)));
        OptiLoggerStreamsContainer.addOutputStream(new LogcatOptiLoggerOutputStream());
    }

    /*****
     * Output Streams
     *****/

    public static void addOutputStream(OptiLoggerOutputStream outputStream) {
        loggerOutputStreams.add(outputStream);
    }

    public static List<OptiLoggerOutputStream> getLoggerOutputStreams() {
        return new ArrayList<>(loggerOutputStreams); // Clone the list before iteration to prevent race conditions
    }

    public static void removeOutputStream(OptiLoggerOutputStream outputStream) {
        loggerOutputStreams.remove(outputStream);
    }

    public static void setMinLogLevelToShow(LogLevel minLogLevelToShow) {
        OptiLoggerStreamsContainer.minLogLevelToShow = minLogLevelToShow;
    }

    public static void setMinLogLevelRemote(LogLevel minLogLevelToShow) {
        OptiLoggerStreamsContainer.minLogLevelRemote = minLogLevelToShow;
    }

    public static LogLevel getMinLogLevelRemote(){
        return OptiLoggerStreamsContainer.minLogLevelRemote;
    }

    /* ****************************************************
     * Log Functions
     * ****************************************************/

    public static void info(String message, Object... args) {
        Pair<String, String> classAndMethodName = getClassAndMethodName();
        OptiLoggerStreamsContainer.sendLogToStreams(LogLevel.INFO, classAndMethodName.first,
                classAndMethodName.second, message, true, args);
    }

    public static void debug(String message, Object... args) {
        Pair<String, String> classAndMethodName = getClassAndMethodName();
        OptiLoggerStreamsContainer.sendLogToStreams(LogLevel.DEBUG, classAndMethodName.first,
                classAndMethodName.second, message, true, args);
    }

    public static void warn(String message, Object... args) {
        Pair<String, String> classAndMethodName = getClassAndMethodName();
        OptiLoggerStreamsContainer.sendLogToStreams(LogLevel.WARN, classAndMethodName.first,
                classAndMethodName.second, message, true, args);
    }

    public static void error(String message, Object... args) {
        Pair<String, String> classAndMethodName = getClassAndMethodName();
        OptiLoggerStreamsContainer.sendLogToStreams(LogLevel.ERROR, classAndMethodName.first,
                classAndMethodName.second, message, true, args);
    }

    public static void fatal(String message, Object... args) {
        Pair<String, String> classAndMethodName = getClassAndMethodName();
        OptiLoggerStreamsContainer.sendLogToStreams(LogLevel.FATAL, classAndMethodName.first,
                classAndMethodName.second, message, true, args);
    }

    public static void businessLogicError(String message, Object... args) {
        Pair<String, String> classAndMethodName = getClassAndMethodName();
        OptiLoggerStreamsContainer.sendLogToStreams(LogLevel.ERROR, classAndMethodName.first,
                classAndMethodName.second, message, false, args);
    }

    private static void sendLogToStreams(LogLevel logLevel, String logClass, String logMethod, String message,
                                         boolean potentiallyRemote, Object... args) {
        String logMessage;
        if (args.length == 0) {
            logMessage = message;
        } else {
            logMessage = String.format(message, args);
        }

        List<OptiLoggerOutputStream> outputStreams =
                new ArrayList<>(loggerOutputStreams); // Clone the list before iteration to prevent race conditions
        for (OptiLoggerOutputStream outputStream : outputStreams) {
            if (outputStream == null) { // Guard against nullified streams
                continue;
            }
            if (outputStream.isVisibleToClient() && logLevel.getRawLevel() < minLogLevelToShow.getRawLevel()) {
                // The client shouldn't see logs less important than the minLogLevel
                continue;
            }
            if (!outputStream.isVisibleToClient() && ((logLevel.getRawLevel() < minLogLevelRemote.getRawLevel()) || !potentiallyRemote)) {
                continue;
            }
            outputStream.reportLog(logLevel, logClass, logMethod, logMessage);
        }
    }


    /* ****************************************************
     * Helper Private Functions
     * ****************************************************/

    private static Pair<String, String> getClassAndMethodName() {
        StackTraceElement[] stackTrace = Thread.currentThread()
                .getStackTrace();

        // At index 0 we have the "getStackTrace()" method, on index 1 we have this function, index 2 is the first candidate for the desired trace
        for (int i = 2; i < stackTrace.length; i++) {
            StackTraceElement candidate = stackTrace[i];
            if (candidate.getFileName() != null && !candidate.getFileName()
                    .equals(OPTI_LOGGER_STREAMS_CONTAINER_CLASS_NAME)) {
                return new Pair<>(candidate.getFileName(), candidate.getMethodName());
            }
        }
        throw new IllegalStateException("Get a stack trace that never leaves the OptiLoggerStreamsContainer class!");
    }
}

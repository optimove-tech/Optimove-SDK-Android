package com.optimove.sdk.optimove_sdk.main_tests;

import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.LogLevel;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerOutputStream;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoggerTests {

    @Mock
    private OptiLoggerOutputStream optiLoggerOutputStream;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        OptiLoggerStreamsContainer.setMinLogLevelRemote(LogLevel.FATAL);
        OptiLoggerStreamsContainer.setMinLogLevelToShow(LogLevel.WARN);

    }

    @Test
    public void fatalLogLevelShouldReachRemoteLogger() {
        when(optiLoggerOutputStream.isVisibleToClient()).thenReturn(false);
        OptiLoggerStreamsContainer.addOutputStream(optiLoggerOutputStream);

        OptiLoggerStreamsContainer.fatal("Some fatal error");

        verify(optiLoggerOutputStream).reportLog(eq(LogLevel.FATAL), any(), any(), any());
    }

    @Test
    public void errorLogLevelShouldntReachRemoteLoggerByDefault() {
        when(optiLoggerOutputStream.isVisibleToClient()).thenReturn(false);
        OptiLoggerStreamsContainer.addOutputStream(optiLoggerOutputStream);

        OptiLoggerStreamsContainer.error("Some fatal error");

        verify(optiLoggerOutputStream, never()).reportLog(any(), any(), any(), any());
    }

    @Test
    public void errorLogLevelShouldReachRemoteLoggerIfMinRemoteLogIsError() {
        when(optiLoggerOutputStream.isVisibleToClient()).thenReturn(false);
        OptiLoggerStreamsContainer.addOutputStream(optiLoggerOutputStream);
        OptiLoggerStreamsContainer.setMinLogLevelRemote(LogLevel.ERROR);
        OptiLoggerStreamsContainer.error("Some error");

        verify(optiLoggerOutputStream).reportLog(eq(LogLevel.ERROR), any(), any(), any());
    }

    @Test
    public void warnLogLevelShouldReachLocalLogger() {
        when(optiLoggerOutputStream.isVisibleToClient()).thenReturn(true);
        OptiLoggerStreamsContainer.addOutputStream(optiLoggerOutputStream);

        OptiLoggerStreamsContainer.warn("Some fatal error");

        verify(optiLoggerOutputStream).reportLog(eq(LogLevel.WARN), any(), any(), any());
    }
    @Test
    public void debugLogLevelShouldntReachLocalLoggerByDefault() {
        when(optiLoggerOutputStream.isVisibleToClient()).thenReturn(true);
        OptiLoggerStreamsContainer.addOutputStream(optiLoggerOutputStream);

        OptiLoggerStreamsContainer.debug("Some fatal error");

        verify(optiLoggerOutputStream, never()).reportLog(any(), any(), any(), any());
    }
}

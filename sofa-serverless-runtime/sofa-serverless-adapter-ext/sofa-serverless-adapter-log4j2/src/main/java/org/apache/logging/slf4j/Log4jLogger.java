/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.logging.slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.util.LoaderUtil;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.impl.StaticMarkerBinder;
import org.slf4j.spi.LocationAwareLogger;

/**
 * SLF4J logger implementation that uses Log4j.
 */
public class Log4jLogger implements LocationAwareLogger, Serializable {

    public static final String FQCN = Log4jLogger.class.getName();

    private static final long serialVersionUID = 7869000638091304316L;
    private static final Marker EVENT_MARKER = MarkerFactory.getMarker("EVENT");
    private static final EventDataConverter CONVERTER = createConverter();

    private final boolean eventLogger;
    private transient final Map<ClassLoader, ExtendedLogger> loggerMap = new ConcurrentHashMap<>();
    private static final Map<ClassLoader, LoggerContext> LOGGER_CONTEXT_MAP = new ConcurrentHashMap<>();
    private final String name;

    public Log4jLogger(final ExtendedLogger logger, final String name) {
        this.eventLogger = "EventLogger".equals(name);
        this.name = name;
    }

    @Override
    public void trace(final String format) {
        getLogger().logIfEnabled(FQCN, Level.TRACE, null, format);
    }

    @Override
    public void trace(final String format, final Object o) {
        getLogger().logIfEnabled(FQCN, Level.TRACE, null, format, o);
    }

    @Override
    public void trace(final String format, final Object arg1, final Object arg2) {
        getLogger().logIfEnabled(FQCN, Level.TRACE, null, format, arg1, arg2);
    }

    @Override
    public void trace(final String format, final Object... args) {
        getLogger().logIfEnabled(FQCN, Level.TRACE, null, format, args);
    }

    @Override
    public void trace(final String format, final Throwable t) {
        getLogger().logIfEnabled(FQCN, Level.TRACE, null, format, t);
    }

    @Override
    public boolean isTraceEnabled() {
        return getLogger().isEnabled(Level.TRACE, null, null);
    }

    @Override
    public boolean isTraceEnabled(final Marker marker) {
        return getLogger().isEnabled(Level.TRACE, getMarker(marker), null);
    }

    @Override
    public void trace(final Marker marker, final String s) {
        getLogger().logIfEnabled(FQCN, Level.TRACE, getMarker(marker), s);
    }

    @Override
    public void trace(final Marker marker, final String s, final Object o) {
        getLogger().logIfEnabled(FQCN, Level.TRACE, getMarker(marker), s, o);
    }

    @Override
    public void trace(final Marker marker, final String s, final Object o, final Object o1) {
        getLogger().logIfEnabled(FQCN, Level.TRACE, getMarker(marker), s, o, o1);
    }

    @Override
    public void trace(final Marker marker, final String s, final Object... objects) {
        getLogger().logIfEnabled(FQCN, Level.TRACE, getMarker(marker), s, objects);
    }

    @Override
    public void trace(final Marker marker, final String s, final Throwable throwable) {
        getLogger().logIfEnabled(FQCN, Level.TRACE, getMarker(marker), s, throwable);
    }

    @Override
    public void debug(final String format) {
        getLogger().logIfEnabled(FQCN, Level.DEBUG, null, format);
    }

    @Override
    public void debug(final String format, final Object o) {
        getLogger().logIfEnabled(FQCN, Level.DEBUG, null, format, o);
    }

    @Override
    public void debug(final String format, final Object arg1, final Object arg2) {
        getLogger().logIfEnabled(FQCN, Level.DEBUG, null, format, arg1, arg2);
    }

    @Override
    public void debug(final String format, final Object... args) {
        getLogger().logIfEnabled(FQCN, Level.DEBUG, null, format, args);
    }

    @Override
    public void debug(final String format, final Throwable t) {
        getLogger().logIfEnabled(FQCN, Level.DEBUG, null, format, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return getLogger().isEnabled(Level.DEBUG, null, null);
    }

    @Override
    public boolean isDebugEnabled(final Marker marker) {
        return getLogger().isEnabled(Level.DEBUG, getMarker(marker), null);
    }

    @Override
    public void debug(final Marker marker, final String s) {
        getLogger().logIfEnabled(FQCN, Level.DEBUG, getMarker(marker), s);
    }

    @Override
    public void debug(final Marker marker, final String s, final Object o) {
        getLogger().logIfEnabled(FQCN, Level.DEBUG, getMarker(marker), s, o);
    }

    @Override
    public void debug(final Marker marker, final String s, final Object o, final Object o1) {
        getLogger().logIfEnabled(FQCN, Level.DEBUG, getMarker(marker), s, o, o1);
    }

    @Override
    public void debug(final Marker marker, final String s, final Object... objects) {
        getLogger().logIfEnabled(FQCN, Level.DEBUG, getMarker(marker), s, objects);
    }

    @Override
    public void debug(final Marker marker, final String s, final Throwable throwable) {
        getLogger().logIfEnabled(FQCN, Level.DEBUG, getMarker(marker), s, throwable);
    }

    @Override
    public void info(final String format) {
        getLogger().logIfEnabled(FQCN, Level.INFO, null, format);
    }

    @Override
    public void info(final String format, final Object o) {
        getLogger().logIfEnabled(FQCN, Level.INFO, null, format, o);
    }

    @Override
    public void info(final String format, final Object arg1, final Object arg2) {
        getLogger().logIfEnabled(FQCN, Level.INFO, null, format, arg1, arg2);
    }

    @Override
    public void info(final String format, final Object... args) {
        getLogger().logIfEnabled(FQCN, Level.INFO, null, format, args);
    }

    @Override
    public void info(final String format, final Throwable t) {
        getLogger().logIfEnabled(FQCN, Level.INFO, null, format, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return getLogger().isEnabled(Level.INFO, null, null);
    }

    @Override
    public boolean isInfoEnabled(final Marker marker) {
        return getLogger().isEnabled(Level.INFO, getMarker(marker), null);
    }

    @Override
    public void info(final Marker marker, final String s) {
        getLogger().logIfEnabled(FQCN, Level.INFO, getMarker(marker), s);
    }

    @Override
    public void info(final Marker marker, final String s, final Object o) {
        getLogger().logIfEnabled(FQCN, Level.INFO, getMarker(marker), s, o);
    }

    @Override
    public void info(final Marker marker, final String s, final Object o, final Object o1) {
        getLogger().logIfEnabled(FQCN, Level.INFO, getMarker(marker), s, o, o1);
    }

    @Override
    public void info(final Marker marker, final String s, final Object... objects) {
        getLogger().logIfEnabled(FQCN, Level.INFO, getMarker(marker), s, objects);
    }

    @Override
    public void info(final Marker marker, final String s, final Throwable throwable) {
        getLogger().logIfEnabled(FQCN, Level.INFO, getMarker(marker), s, throwable);
    }

    @Override
    public void warn(final String format) {
        getLogger().logIfEnabled(FQCN, Level.WARN, null, format);
    }

    @Override
    public void warn(final String format, final Object o) {
        getLogger().logIfEnabled(FQCN, Level.WARN, null, format, o);
    }

    @Override
    public void warn(final String format, final Object arg1, final Object arg2) {
        getLogger().logIfEnabled(FQCN, Level.WARN, null, format, arg1, arg2);
    }

    @Override
    public void warn(final String format, final Object... args) {
        getLogger().logIfEnabled(FQCN, Level.WARN, null, format, args);
    }

    @Override
    public void warn(final String format, final Throwable t) {
        getLogger().logIfEnabled(FQCN, Level.WARN, null, format, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return getLogger().isEnabled(Level.WARN, null, null);
    }

    @Override
    public boolean isWarnEnabled(final Marker marker) {
        return getLogger().isEnabled(Level.WARN, getMarker(marker), null);
    }

    @Override
    public void warn(final Marker marker, final String s) {
        getLogger().logIfEnabled(FQCN, Level.WARN, getMarker(marker), s);
    }

    @Override
    public void warn(final Marker marker, final String s, final Object o) {
        getLogger().logIfEnabled(FQCN, Level.WARN, getMarker(marker), s, o);
    }

    @Override
    public void warn(final Marker marker, final String s, final Object o, final Object o1) {
        getLogger().logIfEnabled(FQCN, Level.WARN, getMarker(marker), s, o, o1);
    }

    @Override
    public void warn(final Marker marker, final String s, final Object... objects) {
        getLogger().logIfEnabled(FQCN, Level.WARN, getMarker(marker), s, objects);
    }

    @Override
    public void warn(final Marker marker, final String s, final Throwable throwable) {
        getLogger().logIfEnabled(FQCN, Level.WARN, getMarker(marker), s, throwable);
    }

    @Override
    public void error(final String format) {
        getLogger().logIfEnabled(FQCN, Level.ERROR, null, format);
    }

    @Override
    public void error(final String format, final Object o) {
        getLogger().logIfEnabled(FQCN, Level.ERROR, null, format, o);
    }

    @Override
    public void error(final String format, final Object arg1, final Object arg2) {
        getLogger().logIfEnabled(FQCN, Level.ERROR, null, format, arg1, arg2);
    }

    @Override
    public void error(final String format, final Object... args) {
        getLogger().logIfEnabled(FQCN, Level.ERROR, null, format, args);
    }

    @Override
    public void error(final String format, final Throwable t) {
        getLogger().logIfEnabled(FQCN, Level.ERROR, null, format, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return getLogger().isEnabled(Level.ERROR, null, null);
    }

    @Override
    public boolean isErrorEnabled(final Marker marker) {
        return getLogger().isEnabled(Level.ERROR, getMarker(marker), null);
    }

    @Override
    public void error(final Marker marker, final String s) {
        getLogger().logIfEnabled(FQCN, Level.ERROR, getMarker(marker), s);
    }

    @Override
    public void error(final Marker marker, final String s, final Object o) {
        getLogger().logIfEnabled(FQCN, Level.ERROR, getMarker(marker), s, o);
    }

    @Override
    public void error(final Marker marker, final String s, final Object o, final Object o1) {
        getLogger().logIfEnabled(FQCN, Level.ERROR, getMarker(marker), s, o, o1);
    }

    @Override
    public void error(final Marker marker, final String s, final Object... objects) {
        getLogger().logIfEnabled(FQCN, Level.ERROR, getMarker(marker), s, objects);
    }

    @Override
    public void error(final Marker marker, final String s, final Throwable throwable) {
        getLogger().logIfEnabled(FQCN, Level.ERROR, getMarker(marker), s, throwable);
    }

    @Override
    public void log(final Marker marker, final String fqcn, final int level, final String message, final Object[] params, Throwable throwable) {
        final Level log4jLevel = getLevel(level);
        final org.apache.logging.log4j.Marker log4jMarker = getMarker(marker);
        ExtendedLogger logger = getLogger();

        if (!logger.isEnabled(log4jLevel, log4jMarker, message, params)) {
            return;
        }
        final Message msg;
        if (CONVERTER != null && eventLogger && marker != null && marker.contains(EVENT_MARKER)) {
            msg = CONVERTER.convertEvent(message, params, throwable);
        } else if (params == null) {
            msg = new SimpleMessage(message);
        } else {
            msg = new ParameterizedMessage(message, params, throwable);
            if (throwable != null) {
                throwable = msg.getThrowable();
            }
        }
        logger.logMessage(fqcn, log4jLevel, log4jMarker, msg, throwable);
    }

    private static org.apache.logging.log4j.Marker getMarker(final Marker marker) {
        if (marker == null) {
            return null;
        } else if (marker instanceof Log4jMarker) {
            return ((Log4jMarker) marker).getLog4jMarker();
        } else {
            final Log4jMarkerFactory factory = (Log4jMarkerFactory) StaticMarkerBinder.SINGLETON.getMarkerFactory();
            return ((Log4jMarker) factory.getMarker(marker)).getLog4jMarker();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Always treat de-serialization as a full-blown constructor, by validating the final state of
     * the de-serialized object.
     */
    private void readObject(final ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
        // always perform the default de-serialization first
        aInputStream.defaultReadObject();
    }

    /**
     * This is the default implementation of writeObject. Customise if necessary.
     */
    private void writeObject(final ObjectOutputStream aOutputStream) throws IOException {
        // perform the default serialization for all non-transient, non-static fields
        aOutputStream.defaultWriteObject();
    }

    private static EventDataConverter createConverter() {
        try {
            LoaderUtil.loadClass("org.slf4j.ext.EventData");
            return new EventDataConverter();
        } catch (final ClassNotFoundException cnfe) {
            return null;
        }
    }

    private static Level getLevel(final int i) {
        switch (i) {
            case TRACE_INT:
                return Level.TRACE;
            case DEBUG_INT:
                return Level.DEBUG;
            case INFO_INT:
                return Level.INFO;
            case WARN_INT:
                return Level.WARN;
            case ERROR_INT:
                return Level.ERROR;
        }
        return Level.ERROR;
    }

    /**
     * 根据当前线程上下文类加载器获取logger
     *
     * @return ExtendedLogger
     */
    private ExtendedLogger getLogger() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        LoggerContext loggerContext = LOGGER_CONTEXT_MAP.get(classLoader);
        if (loggerContext == null) {
            loggerContext = LogManager.getContext(Thread.currentThread().getContextClassLoader(), false);
            LOGGER_CONTEXT_MAP.put(classLoader, loggerContext);
        }
        ExtendedLogger extendedLogger = loggerMap.get(classLoader);
        if (extendedLogger == null) {
            extendedLogger = loggerContext.getLogger(this.name);
            loggerMap.put(classLoader, extendedLogger);
        }
        return extendedLogger;
    }
}

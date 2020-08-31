package com.constellio.data.utils.systemLogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.util.HashSet;
import java.util.Set;

public class SystemLogger {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemLogger.class);

	public static String getName() {
		return LOGGER.getName();
	}

	public static boolean isTraceEnabled() {
		return LOGGER.isTraceEnabled();
	}

	public static void trace(String msg) {
		LOGGER.trace(msg);
	}

	public static void trace(String format, Object arg) {
		LOGGER.trace(format, arg);
	}

	public static void trace(String format, Object arg1, Object arg2) {
		LOGGER.trace(format, arg1, arg2);
	}

	public static void trace(String format, Object... arguments) {
		LOGGER.trace(format, arguments);
	}

	public static void trace(String msg, Throwable t) {
		LOGGER.trace(msg, t);
	}

	public static boolean isTraceEnabled(Marker marker) {
		return LOGGER.isTraceEnabled(marker);
	}

	public static void trace(Marker marker, String msg) {
		LOGGER.trace(marker, msg);
	}

	public static void trace(Marker marker, String format, Object arg) {
		LOGGER.trace(marker, format, arg);
	}

	public static void trace(Marker marker, String format, Object arg1, Object arg2) {
		LOGGER.trace(marker, format, arg1, arg2);
	}

	public static void trace(Marker marker, String format, Object... argArray) {
		LOGGER.trace(marker, format, argArray);
	}

	public static void trace(Marker marker, String msg, Throwable t) {
		LOGGER.trace(marker, msg, t);
	}

	public static boolean isDebugEnabled() {
		return LOGGER.isDebugEnabled();
	}

	public static void debug(String msg) {
		LOGGER.debug(msg);
	}

	public static void debug(String format, Object arg) {
		LOGGER.debug(format, arg);
	}

	public static void debug(String format, Object arg1, Object arg2) {
		LOGGER.debug(format, arg1, arg2);
	}

	public static void debug(String format, Object... arguments) {
		LOGGER.debug(format, arguments);
	}

	public static void debug(String msg, Throwable t) {
		LOGGER.debug(msg, t);
	}

	public static boolean isDebugEnabled(Marker marker) {
		return LOGGER.isDebugEnabled(marker);
	}

	public static void debug(Marker marker, String msg) {
		LOGGER.debug(marker, msg);
	}

	public static void debug(Marker marker, String format, Object arg) {
		LOGGER.debug(marker, format, arg);
	}

	public static void debug(Marker marker, String format, Object arg1, Object arg2) {
		LOGGER.debug(marker, format, arg1, arg2);
	}

	public static void debug(Marker marker, String format, Object... arguments) {
		LOGGER.debug(marker, format, arguments);
	}

	public static void debug(Marker marker, String msg, Throwable t) {
		LOGGER.debug(marker, msg, t);
	}

	public static boolean isInfoEnabled() {
		return LOGGER.isInfoEnabled();
	}

	public static void info(String msg) {
		LOGGER.info(msg);
	}

	public static void info(String format, Object arg) {
		LOGGER.info(format, arg);
	}

	public static void info(String format, Object arg1, Object arg2) {
		LOGGER.info(format, arg1, arg2);
	}

	public static void info(String format, Object... arguments) {
		LOGGER.info(format, arguments);
	}

	public static void info(String msg, Throwable t) {
		LOGGER.info(msg, t);
	}

	public static boolean isInfoEnabled(Marker marker) {
		return LOGGER.isInfoEnabled(marker);
	}

	public static void info(Marker marker, String msg) {
		LOGGER.info(marker, msg);
	}

	public static void info(Marker marker, String format, Object arg) {
		LOGGER.info(marker, format, arg);
	}

	public static void info(Marker marker, String format, Object arg1, Object arg2) {
		LOGGER.info(marker, format, arg1, arg2);
	}

	public static void info(Marker marker, String format, Object... arguments) {
		LOGGER.info(marker, format, arguments);
	}

	public static void info(Marker marker, String msg, Throwable t) {
		LOGGER.info(marker, msg, t);
	}

	public static boolean isWarnEnabled() {
		return LOGGER.isWarnEnabled();
	}

	public static void warn(String msg) {
		LOGGER.warn(msg);
	}

	public static void warn(String format, Object arg) {
		LOGGER.warn(format, arg);
	}

	public static void warn(String format, Object... arguments) {
		LOGGER.warn(format, arguments);
	}

	public static void warn(String format, Object arg1, Object arg2) {
		LOGGER.warn(format, arg1, arg2);
	}

	public static void warn(String msg, Throwable t) {
		LOGGER.warn(msg, t);
	}

	public static boolean isWarnEnabled(Marker marker) {
		return LOGGER.isWarnEnabled(marker);
	}

	public static void warn(Marker marker, String msg) {
		LOGGER.warn(marker, msg);
	}

	public static void warn(Marker marker, String format, Object arg) {
		LOGGER.warn(marker, format, arg);
	}

	public static void warn(Marker marker, String format, Object arg1, Object arg2) {
		LOGGER.warn(marker, format, arg1, arg2);
	}

	public static void warn(Marker marker, String format, Object... arguments) {
		LOGGER.warn(marker, format, arguments);
	}

	public static void warn(Marker marker, String msg, Throwable t) {
		LOGGER.warn(marker, msg, t);
	}

	public static boolean isErrorEnabled() {
		return LOGGER.isErrorEnabled();
	}

	public static void error(String msg) {
		LOGGER.error(msg);
	}

	public static void error(String format, Object arg) {
		LOGGER.error(format, arg);
	}

	public static void error(String format, Object arg1, Object arg2) {
		LOGGER.error(format, arg1, arg2);
	}

	public static void error(String format, Object... arguments) {
		LOGGER.error(format, arguments);
	}

	public static void error(String msg, Throwable t) {
		LOGGER.error(msg, t);
	}

	public static boolean isErrorEnabled(Marker marker) {
		return LOGGER.isErrorEnabled(marker);
	}

	public static void error(Marker marker, String msg) {
		LOGGER.error(marker, msg);
	}

	public static void error(Marker marker, String format, Object arg) {
		LOGGER.error(marker, format, arg);
	}

	public static void error(Marker marker, String format, Object arg1, Object arg2) {
		LOGGER.error(marker, format, arg1, arg2);
	}

	public static void error(Marker marker, String format, Object... arguments) {
		LOGGER.error(marker, format, arguments);
	}

	public static void error(Marker marker, String msg, Throwable t) {
		LOGGER.error(marker, msg, t);
	}

	static Set<String> importantWarningsLogged = new HashSet<>();

	public static void logImportantWarningOnce(String message) {
		//Spamming them isn't better
		if (importantWarningsLogged.size() < 50 && !importantWarningsLogged.contains(message)) {
			synchronized (SystemLogger.class) {
				if (!importantWarningsLogged.contains(message)) {
					importantWarningsLogged.add(message);
					warn(message);
				}
			}
		}
	}
}

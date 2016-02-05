package com.constellio.app.modules.es.services;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ConnectorsUtils {

	public static String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

}

package com.constellio.app.modules.es.connectors.spi;

import java.util.Map;

public interface LoggedException {

	String getMessage();

	Map<String, String> getParameters();

}

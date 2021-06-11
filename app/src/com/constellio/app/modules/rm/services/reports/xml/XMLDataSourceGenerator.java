package com.constellio.app.modules.rm.services.reports.xml;

import java.io.InputStream;

public interface XMLDataSourceGenerator {

	InputStream generate(XMLDataSourceGeneratorParams params);

}

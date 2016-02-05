package com.constellio.app.ui.framework.reports;

import java.io.IOException;
import java.io.OutputStream;

public interface ReportBuilder {

	String getFileExtension();

	void build(OutputStream output)
			throws IOException;

}

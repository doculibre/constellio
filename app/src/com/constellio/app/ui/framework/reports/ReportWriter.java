package com.constellio.app.ui.framework.reports;

import java.io.IOException;
import java.io.OutputStream;

public interface ReportWriter {

	String getFileExtension();

	void write(OutputStream output)
			throws IOException;

}

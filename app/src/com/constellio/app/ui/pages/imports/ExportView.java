package com.constellio.app.ui.pages.imports;

import java.io.InputStream;

import com.constellio.app.ui.pages.base.BaseView;

public interface ExportView extends BaseView {
	
	String getExportedIds();
	
	void startDownload(String filename, InputStream inputStream, String mimeType);
	
}

package com.constellio.app.ui.pages.imports;

import com.constellio.app.ui.pages.base.BaseView;

import java.io.InputStream;

public interface ExportView extends BaseView {

	String getExportedIds();

	void startDownload(String filename, InputStream inputStream, String mimeType);

}

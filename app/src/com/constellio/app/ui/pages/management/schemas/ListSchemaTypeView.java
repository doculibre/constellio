package com.constellio.app.ui.pages.management.schemas;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

import java.io.InputStream;

public interface ListSchemaTypeView extends BaseView, AdminViewGroup {
	void startDownload(String filename, InputStream inputStream, String mimeType);
}

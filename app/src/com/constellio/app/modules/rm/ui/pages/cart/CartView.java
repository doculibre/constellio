package com.constellio.app.modules.rm.ui.pages.cart;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.CartViewGroup;

import java.io.InputStream;

public interface CartView extends BaseView, CartViewGroup {
	void startDownload(InputStream stream, String filename);

	void filterFolderTable();

	void filterDocumentTable();

	void filterContainerTable();

	String getCurrentSchemaType();

}

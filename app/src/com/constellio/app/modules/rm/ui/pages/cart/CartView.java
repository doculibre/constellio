package com.constellio.app.modules.rm.ui.pages.cart;

import java.io.InputStream;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.CartViewGroup;

public interface CartView extends BaseView, CartViewGroup {
	void startDownload(InputStream stream);
	void filterTable();

}

package com.constellio.app.modules.rm.services.menu;

import com.constellio.app.services.factories.AppLayerFactory;

public class CartMenuItemServices {

	private String collection;
	private AppLayerFactory appLayerFactory;

	public CartMenuItemServices(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	enum CartMenuItemActionType {
		CART_BATCH_DOCUMENT,
		CART_BATCH_FOLDER,
		CART_BATCH_CONTAINER,
		CART_LABEL_DOCUMENT,
		CART_LABEL_FOLDER,
		CART_LABEL_CONTAINER,
		CART_EMPTY,
		CART_SHARE,
		CART_DECOMMISSIONING_LIST,
		CART_GENERATE_REPORT,
		CART_SIP_ARCHIVE,
		CART_CREATE_PDF;
	}

}

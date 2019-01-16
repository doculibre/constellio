package com.constellio.app.ui.framework.containers.exception;

import static com.constellio.app.ui.i18n.i18n.$;

public class ContainerException extends RuntimeException {

	public ContainerException(String message) {
		super(message);
	}

	public static class ContainerException_ItemListChanged
			extends ContainerException {
		public ContainerException_ItemListChanged() {
			super($("ContainerException.ItemListChanged"));
		}
	}
}

package com.constellio.app.modules.rm.extensions.api;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.server.Resource;

public class DocumentExtension {

	public void addMenuItems(DocumentExtensionAddMenuItemsParams params) {

	}

	public static abstract class DocumentExtensionAddMenuItemsParams {

		public abstract Document getDocument();

		public abstract RecordVO getRecordVO();

		public abstract BaseViewImpl getView();

		public abstract void registerMenuItem(String caption, Resource icon, Runnable runnable);

	}

}

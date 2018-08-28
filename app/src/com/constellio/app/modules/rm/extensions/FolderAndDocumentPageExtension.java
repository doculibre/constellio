package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.PagesComponentsExtension;
import com.constellio.app.api.extensions.params.DecorateMainComponentAfterInitExtensionParams;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.pages.base.UIContext;

public class FolderAndDocumentPageExtension extends PagesComponentsExtension {


	@Override
	public void decorateMainComponentBeforeViewInstanciated(DecorateMainComponentAfterInitExtensionParams params) {
		super.decorateMainComponentBeforeViewInstanciated(params);

//		Component mainComponent = params.getMainComponent();
//
//		if(!(mainComponent instanceof DisplayFolderViewImpl)
//		   && !(mainComponent instanceof DisplayDocumentViewImpl)
//		   && !(mainComponent instanceof DecommissioningBuilderViewImpl)
//		   && !(mainComponent instanceof AddEditFolderViewImpl)
//		   && !(mainComponent instanceof AddEditDocumentViewImpl)
//		   && !(mainComponent instanceof  DecommissioningBuilderViewImpl)) {
//			getUIContext().clearAttribute(SearchViewImpl.DECOMMISSIONING_BUILDER_TYPE);
//			getUIContext().clearAttribute(SearchViewImpl.SAVE_SEARCH_DECOMMISSIONING);
//		}
	}

	private UIContext getUIContext() {
		return ConstellioUI.getCurrent();
	}
}

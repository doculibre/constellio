package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.PagesComponentsExtension;
import com.constellio.app.api.extensions.params.DecorateMainComponentAfterInitExtensionParams;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderViewImpl;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.conversations.DisplayFolderConversationTab;
import com.vaadin.ui.Component;

public class RMFolderConversationExtension extends PagesComponentsExtension {

	private final AppLayerFactory appLayerFactory;

	public RMFolderConversationExtension(String collection, AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public void decorateMainComponentAfterViewAssembledOnViewEntered(
			DecorateMainComponentAfterInitExtensionParams params) {
		super.decorateMainComponentAfterViewAssembledOnViewEntered(params);

		Component mainComponent = params.getMainComponent();

		if (mainComponent instanceof DisplayFolderViewImpl) {
			DisplayFolderViewImpl displayFolderView = (DisplayFolderViewImpl) mainComponent;
			String collection = displayFolderView.getCollection();

			RecordVO summaryRecord = displayFolderView.getSummaryRecord();

			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
			Folder folder = rm.getFolder(summaryRecord.getId());

			String conversationId = folder.getConversation();

			if (conversationId != null) {
				DisplayFolderConversationTab displayFolderConversationTab = new DisplayFolderConversationTab(folder, appLayerFactory);
				displayFolderConversationTab.addTabToThisView(displayFolderView);
			}
		}
	}
}

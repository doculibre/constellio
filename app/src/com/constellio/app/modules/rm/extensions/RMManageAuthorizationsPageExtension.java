package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.PagesComponentsExtension;
import com.constellio.app.api.extensions.params.DecorateMainComponentAfterInitExtensionParams;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.pages.management.authorizations.ListAuthorizationsView;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.ui.Component;

import static com.constellio.app.ui.i18n.i18n.$;

/**
 * Created by Constellio on 2017-03-16.
 */
public class RMManageAuthorizationsPageExtension extends PagesComponentsExtension {

	String collection;
	AppLayerFactory appLayerFactory;

	public RMManageAuthorizationsPageExtension(String collection,
											   AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public void decorateMainComponentAfterViewAssembledOnViewEntered(
			DecorateMainComponentAfterInitExtensionParams params) {
		super.decorateMainComponentAfterViewAssembledOnViewEntered(params);
		Component mainComponent = params.getMainComponent();
		if (mainComponent instanceof ListAuthorizationsView ) {
			ListAuthorizationsView view = (ListAuthorizationsView ) mainComponent;
			updateReadOnlyStatus(view);
		}
	}

	private void updateReadOnlyStatus(ListAuthorizationsView view) {
		Record autorizationTarget = view.getAutorizationTarget();
		User user = appLayerFactory.getModelLayerFactory().newUserServices()
				.getUserInCollection(view.getSessionContext().getCurrentUser().getUsername(), collection);
		if(autorizationTarget.isOfSchemaType(Folder.SCHEMA_TYPE) &&
		   (!user.has(RMPermissionsTo.MANAGE_FOLDER_AUTHORIZATIONS).on(autorizationTarget) || !user.hasWriteAccess().on(autorizationTarget))) {
			view.setViewReadOnly(true);
		} else if(autorizationTarget.isOfSchemaType(Document.SCHEMA_TYPE) &&
				  (!user.has(RMPermissionsTo.MANAGE_DOCUMENT_AUTHORIZATIONS).on(autorizationTarget) || !user.hasWriteAccess().on(autorizationTarget))) {
			view.setViewReadOnly(true);
		}
	}
}

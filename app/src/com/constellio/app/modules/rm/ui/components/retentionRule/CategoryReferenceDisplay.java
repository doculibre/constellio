package com.constellio.app.modules.rm.ui.components.retentionRule;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.users.UserServices;

public class CategoryReferenceDisplay extends ReferenceDisplay {

	public CategoryReferenceDisplay(String recordId) {
		super(recordId, hasDisplayCategoryPageAccess());
	}

	private static boolean hasDisplayCategoryPageAccess() {
		ConstellioFactories constellioFactories = ConstellioUI.getCurrent().getConstellioFactories();
		SessionContext sessionContext = ConstellioUI.getCurrent().getSessionContext();
		String username = sessionContext.getCurrentUser().getUsername();
		String collection = sessionContext.getCurrentCollection();

		UserServices userServices = constellioFactories.getModelLayerFactory().newUserServices();
		User user = userServices.getUserInCollection(username, collection);

		return user.hasAny(RMPermissionsTo.MANAGE_CLASSIFICATION_PLAN).globally() || user.has(RMPermissionsTo.CONSULT_CLASSIFICATION_PLAN).globally();
	}
}

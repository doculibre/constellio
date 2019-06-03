package com.constellio.app.modules.rm.ui.components.retentionRule;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.users.UserServices;

public class RetentionRuleReferenceDisplay extends ReferenceDisplay {

	public RetentionRuleReferenceDisplay(RecordVO recordVO) {
		super(recordVO, hasDisplayRetentionRulePageAccess());
	}

	public RetentionRuleReferenceDisplay(String recordId) {
		super(recordId, hasDisplayRetentionRulePageAccess());
	}

	private static boolean hasDisplayRetentionRulePageAccess() {
		ConstellioFactories constellioFactories = ConstellioUI.getCurrent().getConstellioFactories();
		SessionContext sessionContext = ConstellioUI.getCurrent().getSessionContext();
		String username = sessionContext.getCurrentUser().getUsername();
		String collection = sessionContext.getCurrentCollection();

		UserServices userServices = constellioFactories.getModelLayerFactory().newUserServices();
		User user = userServices.getUserInCollection(username, collection);

		return user.hasAny(RMPermissionsTo.MANAGE_RETENTIONRULE).globally() || user.has(RMPermissionsTo.CONSULT_RETENTIONRULE).globally();
	}

	@Override
	protected String getNiceTitle(Record record, MetadataSchemaTypes types) {
		return new RetentionRuleInfoBuilder(record).getInfo();
	}

}

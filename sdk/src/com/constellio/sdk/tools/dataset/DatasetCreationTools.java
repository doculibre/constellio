package com.constellio.sdk.tools.dataset;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;

import static java.util.Arrays.asList;

public class DatasetCreationTools {

	public static RetentionRule newTestRule(String collection, AppLayerFactory appLayerFactory) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.sequential(appLayerFactory);
		CopyRetentionRule principal888_5_C = copyBuilder.newPrincipal(asList(rm.PA(), rm.DM()), "888-5-C");
		CopyRetentionRule secondary888_0_D = copyBuilder.newSecondary(asList(rm.PA(), rm.DM()), "888-0-D");
		RetentionRule retentionRule = rm.newRetentionRule().setCode("R1").setTitle("Retention rule")
				.setApproved(true).setResponsibleAdministrativeUnits(true)
				.setCopyRetentionRules(asList(principal888_5_C, secondary888_0_D));
		return retentionRule;
	}

}

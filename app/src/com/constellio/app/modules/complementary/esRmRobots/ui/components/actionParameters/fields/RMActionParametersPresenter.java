package com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields.category.ActionParametersCategoryField;
import com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields.retentionRule.ActionParametersRetentionRuleField;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.factories.ModelLayerFactory;

public class RMActionParametersPresenter {

	public RMActionParametersPresenter(ClassifyConnectorFolderActionParameters actionParameters, RMActionParametersForm form) {
		
		ActionParametersCategoryField categoryField = form.getCategoryField();
		
		ActionParametersRetentionRuleField retentionRuleField = form.getRetentionRuleField();
		
		ConstellioFactories constellioFactories = form.getConstellioFactories();
		SessionContext sessionContext = form.getSessionContext();
		String collection = sessionContext.getCurrentCollection();
		
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		
		String defaultCategory = categoryField.getValue();
		if (StringUtils.isNotBlank(defaultCategory)) {
			
		} else {
			retentionRuleField.setOptions(new ArrayList<String>());
		}
	}

}

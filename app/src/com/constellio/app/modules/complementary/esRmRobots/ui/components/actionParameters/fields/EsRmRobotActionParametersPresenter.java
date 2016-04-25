package com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields.category.ActionParametersCategoryField;
import com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields.retentionRule.ActionParametersRetentionRuleField;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;

public class EsRmRobotActionParametersPresenter {
	
	private EsRmRobotActionParametersFields fields;

	public EsRmRobotActionParametersPresenter(EsRmRobotActionParametersFields fields) {
		this.fields = fields;
	}
	
	void rmFieldsCreated() {
		ActionParametersCategoryField categoryField = fields.getCategoryField();
		if (categoryField != null) {
			categoryField.addValueChangeListener(new ActionParametersCategoryField.CategoryValueChangeListener() {
				@Override
				public void valueChanged(String newValue) {
					updateFields(newValue);
				}
			});
		}
	}
	
	private void updateFields(String categoryId) {
		ActionParametersRetentionRuleField retentionRuleField = fields.getRetentionRuleField();
		
		ConstellioFactories constellioFactories = fields.getConstellioFactories();
		SessionContext sessionContext = fields.getSessionContext();
		String collection = sessionContext.getCurrentCollection();
		
		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		
		if (StringUtils.isNotBlank(categoryId)) {
			Category defaultCategory = rm.getCategory(categoryId);
			List<String> retentionRules = defaultCategory.getRententionRules();
			retentionRuleField.setOptions(retentionRules);
			if (retentionRules.size() == 1) {
				retentionRuleField.setFieldValue(retentionRules.get(0));
			}
		} else {
			retentionRuleField.setOptions(new ArrayList<String>());
		}
	}
	
}

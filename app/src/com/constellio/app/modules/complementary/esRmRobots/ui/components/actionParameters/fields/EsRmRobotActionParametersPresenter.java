package com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields;

import com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields.category.ActionParametersCategoryField;
import com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields.retentionRule.ActionParametersRetentionRuleField;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.data.Property;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

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

	void subdivisionFieldCreated() {
		LookupRecordField uniformSubdivisionField = fields.getUniformSubdivision();
		if(uniformSubdivisionField != null) {
			uniformSubdivisionField.addValueChangeListener(new Property.ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					String uniformSubdivisionId = (String) event.getProperty().getValue();
					updateFieldsWithUniformSubdivision(uniformSubdivisionId);
				}
			});
		}
	}
	
	private void updateFields(String categoryId) {
		ConstellioFactories constellioFactories = fields.getConstellioFactories();
		SessionContext sessionContext = fields.getSessionContext();
		String collection = sessionContext.getCurrentCollection();

		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
		LookupRecordField uniformSubdivisionField = fields.getUniformSubdivision();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		ActionParametersRetentionRuleField retentionRuleField = fields.getRetentionRuleField();

		if(uniformSubdivisionField == null || uniformSubdivisionField.getValue() == null || uniformSubdivisionField.getValue().equals("")) {
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
		} else {
			UniformSubdivision defaultSubdivision = rm.getUniformSubdivision(uniformSubdivisionField.getValue());
			retentionRuleField.setOptions(defaultSubdivision.getRetentionRules());
		}
	}

	private void updateFieldsWithUniformSubdivision(String uniformSubdivisionId) {
		ConstellioFactories constellioFactories = fields.getConstellioFactories();
		SessionContext sessionContext = fields.getSessionContext();
		String collection = sessionContext.getCurrentCollection();

		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
		LookupRecordField uniformSubdivisionField = fields.getUniformSubdivision();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		ActionParametersRetentionRuleField retentionRuleField = fields.getRetentionRuleField();

		if(uniformSubdivisionField == null || uniformSubdivisionField.getValue() == null || uniformSubdivisionField.getValue().equals("")) {
			String categoryId = fields.getCategoryField().getFieldValue();
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
		} else {
			UniformSubdivision defaultSubdivision = rm.getUniformSubdivision(uniformSubdivisionId);
			retentionRuleField.setOptions(defaultSubdivision.getRetentionRules());
		}
	}

	public boolean areUniformSubdivisionsEnabled() {
		return new RMConfigs(fields.getConstellioFactories().getModelLayerFactory().getSystemConfigurationsManager()).areUniformSubdivisionEnabled();
	}
}

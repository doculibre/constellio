package com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields;

import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderDirectlyInThePlanActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields.category.ActionParametersCategoryField;
import com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields.category.ActionParametersCategoryFieldImpl;
import com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields.retentionRule.ActionParametersRetentionRuleField;
import com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields.retentionRule.ActionParametersRetentionRuleFieldImpl;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.ui.Field;

import java.util.Arrays;
import java.util.Locale;

import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderDirectlyInThePlanActionParameters.DEFAULT_UNIFORM_SUBDIVISION;
import static com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters.DELIMITER;

public class EsRmRobotActionParametersFieldFactory extends RecordFieldFactory implements EsRmRobotActionParametersFields {
	private static final String DEFAULT_RETENTION_RULE = "defaultRetentionRule";
	private static final String DEFAULT_CATEGORY = "defaultCategory";
	private static final String DEFAULT_PARENT_FOLDER = "defaultParentFolder";
	private static final String IN_TAXONOMY = "inTaxonomy";
	private static final String DEFAULT_COPY_STATUS = "defaultCopyStatus";
	private static final String PATH_PREFIX = "pathPrefix";

	private ActionParametersCategoryFieldImpl categoryField;

	private ActionParametersRetentionRuleFieldImpl retentionRuleField;

	private LookupRecordField uniformSubdivisionField;

	private EsRmRobotActionParametersPresenter presenter;

	private static final String[] CUSTOM_FIELDS = {
			ClassifyConnectorFolderDirectlyInThePlanActionParameters.DEFAULT_CATEGORY,
			ClassifyConnectorFolderInTaxonomyActionParameters.DEFAULT_CATEGORY,
			ClassifyConnectorFolderDirectlyInThePlanActionParameters.DEFAULT_RETENTION_RULE,
			ClassifyConnectorFolderInTaxonomyActionParameters.DEFAULT_RETENTION_RULE,
			};

	public EsRmRobotActionParametersFieldFactory() {
		this.presenter = new EsRmRobotActionParametersPresenter(this);
	}

	@Override
	public Field<?> build(RecordVO recordVO, MetadataVO metadataVO, Locale locale) {
		Field<?> field;
		if (DEFAULT_UNIFORM_SUBDIVISION.equals(metadataVO.getLocalCode()) && !presenter.areUniformSubdivisionsEnabled()) {
			return null;
		}

		if (DEFAULT_COPY_STATUS.equals(metadataVO.getLocalCode())) {
			return null;
		}

		if (IN_TAXONOMY.equals(metadataVO.getLocalCode())) {
			return null;
		}

		if (DEFAULT_CATEGORY.equals(metadataVO.getLocalCode()) && !metadataVO.isRequired()) {
			return null;
		}

		if (DEFAULT_RETENTION_RULE.equals(metadataVO.getLocalCode()) && !metadataVO.isRequired()) {
			return null;
		}

		if (PATH_PREFIX.equals(metadataVO.getLocalCode())) {
			return null;
		}

		String code = MetadataVO.getCodeWithoutPrefix(metadataVO.getCode());
		if (Arrays.asList(CUSTOM_FIELDS).contains(code)) {
			if (categoryField == null) {
				categoryField = new ActionParametersCategoryFieldImpl();
				retentionRuleField = new ActionParametersRetentionRuleFieldImpl();
				presenter.rmFieldsCreated();
			}
			if (ClassifyConnectorFolderDirectlyInThePlanActionParameters.DEFAULT_CATEGORY.equals(code) ||
				ClassifyConnectorFolderInTaxonomyActionParameters.DEFAULT_CATEGORY.equals(code)) {
				field = categoryField;
			} else {
				field = retentionRuleField;
			}
			super.postBuild(field, recordVO, metadataVO);
		} else {
			if (code.equals(DEFAULT_UNIFORM_SUBDIVISION)) {
				uniformSubdivisionField = (LookupRecordField) super.build(recordVO, metadataVO, locale);
				presenter.subdivisionFieldCreated();
				return uniformSubdivisionField;
			}
			if (metadataVO.getLocalCode().equals(DELIMITER)) {
				String inputMask = metadataVO.getInputMask();
				BaseTextField textField = new BaseTextField(false);
				textField.setInputMask(inputMask);
				if (textField != null) {
					super.postBuild(textField, recordVO, metadataVO);
				}
				return textField;
			}
			field = super.build(recordVO, metadataVO, locale);
		}

		return field;
	}

	@Override
	public SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

	@Override
	public ConstellioFactories getConstellioFactories() {
		return ConstellioUI.getCurrent().getConstellioFactories();
	}

	@Override
	public ActionParametersCategoryField getCategoryField() {
		return categoryField;
	}

	@Override
	public ActionParametersRetentionRuleField getRetentionRuleField() {
		return retentionRuleField;
	}

	@Override
	public LookupRecordField getUniformSubdivision() {
		return uniformSubdivisionField;
	}

}

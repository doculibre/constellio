package com.constellio.app.modules.rm.ui.pages.retentionRule.extensions;

import com.constellio.app.api.extensions.MetadataDisplayCustomValueExtention;
import com.constellio.app.api.extensions.MetadataFieldFactoryExtension;
import com.constellio.app.api.extensions.params.MetadataDisplayCustomValueExtentionParams;
import com.constellio.app.api.extensions.params.MetadataFieldFactoryBuildExtensionParams;
import com.constellio.app.entities.schemasDisplay.enums.MetadataSortingType;
import com.constellio.app.extensions.records.RecordAppExtension;
import com.constellio.app.extensions.records.params.AddSyntheticMetadataValuesParams;
import com.constellio.app.extensions.records.params.AddSyntheticMetadataValuesParams.SyntheticMetadataVOBuildingArgs;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.pages.legalrequirement.component.LegalRequirementReferenceEditableRecordTablePresenter;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.LegalRequirementReference;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.table.field.EditableRecordsTableField;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.ui.Field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.schemas.Schemas.LOGICALLY_DELETED_STATUS;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class DisplayRetentionRuleExtensions {
	private static final String RETENTION_RULE_LEGAL_REQUIREMENT_SYNTHETIC_LOCAL_CODE = "legalRequirements";
	private static final String RETENTION_RULE_LEGAL_REQUIREMENT_SYNTHETIC_CODE = RetentionRule.DEFAULT_SCHEMA + "_" + RETENTION_RULE_LEGAL_REQUIREMENT_SYNTHETIC_LOCAL_CODE;

	public static class DisplayRetentionRuleRecordAppExtension extends RecordAppExtension {
		public final AppLayerFactory appLayerFactory;
		public final String collection;

		public DisplayRetentionRuleRecordAppExtension(
				String collection,
				AppLayerFactory appLayerFactory) {
			this.appLayerFactory = appLayerFactory;
			this.collection = collection;
		}

		@Override
		public List<MetadataValueVO> addSyntheticMetadataValues(AddSyntheticMetadataValuesParams params) {
			List<MetadataValueVO> extraMetadataValues;

			Record record = params.getRecord();
			String schemaCode = record.getSchemaCode();
			String schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaCode);

			if (schemaTypeCode.equals(RetentionRule.SCHEMA_TYPE) && Toggle.DISPLAY_LEGAL_REQUIREMENTS.isEnabled()) {
				extraMetadataValues = new ArrayList<>();

				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
				SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();

				List<Category> allCategories = rm.getAllCategories();

				Set<String> retentionRuleCategoryLegalRequirements = new HashSet<>();
				allCategories.stream()
						.filter(category -> category.getRententionRules().contains(record.getId()))
						.flatMap(category -> ((List<String>) category.get(Category.LEGAL_REQUIREMENTS)).stream())
						.forEach(retentionRuleCategoryLegalRequirements::add);

				Set<String> distinctLegalRequirementReference = new HashSet<>();
				retentionRuleCategoryLegalRequirements.stream().flatMap(retentionRuleCategoryLegalRequirement ->
						searchServices.searchRecordIds(from(rm.legalRequirementReference.schemaType())
								.where(rm.legalRequirementReference.ruleRequirement()).isEqualTo(retentionRuleCategoryLegalRequirement)
								.andWhere(LOGICALLY_DELETED_STATUS).isFalseOrNull()).stream()
				)
						.forEach(distinctLegalRequirementReference::add);

				MetadataVO RetentionRuleLegalRequirementReferenceSyntheticMetadata = params.getSyntheticMetadataVOBuilder().build(new SyntheticMetadataVOBuildingArgs(
						RetentionRule.DEFAULT_SCHEMA + "_" + RETENTION_RULE_LEGAL_REQUIREMENT_SYNTHETIC_LOCAL_CODE,
						LegalRequirementReference.SCHEMA_TYPE,
						LegalRequirementReference.DEFAULT_SCHEMA,
						$("RetentionRules.legalRequirement"),
						MetadataSortingType.ALPHANUMERICAL_ORDER) {
					@Override
					public boolean isReadOnly() {
						return true;
					}
				});
				extraMetadataValues.add(new MetadataValueVO(RetentionRuleLegalRequirementReferenceSyntheticMetadata, new ArrayList<>(distinctLegalRequirementReference)));
			} else {
				extraMetadataValues = super.addSyntheticMetadataValues(params);
			}

			return extraMetadataValues;
		}
	}

	public static class DisplayRetentionRuleMetadataDisplayCustomValueExtention extends MetadataDisplayCustomValueExtention {
		public final AppLayerFactory appLayerFactory;
		public final String collection;

		public DisplayRetentionRuleMetadataDisplayCustomValueExtention(
				String collection,
				AppLayerFactory appLayerFactory) {
			this.appLayerFactory = appLayerFactory;
			this.collection = collection;
		}

		@Override
		public Object getCustomDisplayValue(
				MetadataDisplayCustomValueExtentionParams metadataDisplayCustomValueExtentionParams) {
			Object result;
			RecordVO recordVO = metadataDisplayCustomValueExtentionParams.getRecordVO();
			MetadataVO metadataVO = metadataDisplayCustomValueExtentionParams.getMetadataVO();

			if (recordVO.getSchema().getTypeCode().equals(RetentionRule.SCHEMA_TYPE) && metadataVO.getCode().equals(RETENTION_RULE_LEGAL_REQUIREMENT_SYNTHETIC_CODE)) {
				List<String> values = recordVO.get(metadataVO);

				EditableRecordsTableField editableRecordTable = buildEditableRecordTable(metadataVO, recordVO.get(metadataVO), ConstellioUI.getCurrentSessionContext(), appLayerFactory);

				if (values.isEmpty()) {
					editableRecordTable.setVisible(false);
				}

				result = editableRecordTable;
			} else {
				result = super.getCustomDisplayValue(metadataDisplayCustomValueExtentionParams);
			}

			return result;
		}
	}

	public static class DisplayRetentionRuleMetadataFieldFactoryExtension extends MetadataFieldFactoryExtension {
		public final AppLayerFactory appLayerFactory;
		public final String collection;

		public DisplayRetentionRuleMetadataFieldFactoryExtension(
				String collection,
				AppLayerFactory appLayerFactory) {
			this.appLayerFactory = appLayerFactory;
			this.collection = collection;
		}

		@Override
		public Field<?> build(MetadataFieldFactoryBuildExtensionParams params) {
			Field<?> field;

			MetadataVO metadataVO = params.getMetadataVO();

			if (metadataVO.getCode().equals(RETENTION_RULE_LEGAL_REQUIREMENT_SYNTHETIC_CODE)) {
				field = buildEditableRecordTable(metadataVO, Collections.emptyList(), ConstellioUI.getCurrentSessionContext(), appLayerFactory);
			} else {
				field = super.build(params);
			}

			return field;
		}
	}

	private static EditableRecordsTableField buildEditableRecordTable(MetadataVO metadataVO,
																	  List<String> values,
																	  SessionContext sessionContext,
																	  AppLayerFactory appLayerFactory) {

		LegalRequirementReferenceEditableRecordTablePresenter presenter = new LegalRequirementReferenceEditableRecordTablePresenter(appLayerFactory, sessionContext, "") {
			@Override
			protected List<MetadataVO> getMetadatasThatMustBeHiddenByDefault(List<MetadataVO> availableMetadatas) {
				List<MetadataVO> metadataThatMustBeHiddenByDefault = new ArrayList<>();

				super.getMetadatasThatMustBeHiddenByDefault(availableMetadatas).stream()
						.filter(availableMetadata -> availableMetadata.getLocalCode().equals(LegalRequirementReference.TITLE))
						.forEach(metadataThatMustBeHiddenByDefault::add);

				return metadataThatMustBeHiddenByDefault;
			}

			@Override
			protected RecordVODataProvider buildRecordDataProvider(List<Record> loadedRecords,
																   List<MetadataSchemaVO> schemas,
																   Map<String, RecordToVOBuilder> builders) {

				return new RecordVODataProvider(schemas, builders, getAppLayerFactory().getModelLayerFactory(), getSessionContext()) {
					@Override
					public LogicalSearchQuery getQuery() {
						RMSchemasRecordsServices rm = new RMSchemasRecordsServices(sessionContext.getCurrentCollection(), appLayerFactory);
						return new LogicalSearchQuery(from(rm.legalRequirementReference.schemaType()).where(Schemas.IDENTIFIER).isIn(values));
					}
				};
			}
		};

		EditableRecordsTableField editableRecordsTable =
				new EditableRecordsTableField(presenter);
		editableRecordsTable.setReadOnly(metadataVO.isReadOnly());
		editableRecordsTable.setResizedIfRowsDoesNotFillHeight(true);

		return editableRecordsTable;
	}
}

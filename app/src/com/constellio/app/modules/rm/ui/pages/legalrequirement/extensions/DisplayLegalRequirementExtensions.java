package com.constellio.app.modules.rm.ui.pages.legalrequirement.extensions;

import com.constellio.app.entities.schemasDisplay.enums.MetadataSortingType;
import com.constellio.app.extensions.records.RecordAppExtension;
import com.constellio.app.extensions.records.params.AddSyntheticMetadataValuesParams;
import com.constellio.app.extensions.records.params.AddSyntheticMetadataValuesParams.SyntheticMetadataVOBuildingArgs;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.LegalRequirement;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.schemas.SchemaUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;

public class DisplayLegalRequirementExtensions {
	private static final String LEGAL_REQUIREMENT_CATEGORIES_SYNTHETIC_LOCAL_CODE = "categories";
	private static final String LEGAL_REQUIREMENT_CATEGORIES_SYNTHETIC_CODE = LegalRequirement.DEFAULT_SCHEMA + "_" + LEGAL_REQUIREMENT_CATEGORIES_SYNTHETIC_LOCAL_CODE;

	private static final String LEGAL_REQUIREMENT_RETENTION_RULES_SYNTHETIC_LOCAL_CODE = "retentionRules";
	private static final String LEGAL_REQUIREMENT_RETENTION_RULES_SYNTHETIC_CODE = LegalRequirement.DEFAULT_SCHEMA + "_" + LEGAL_REQUIREMENT_RETENTION_RULES_SYNTHETIC_LOCAL_CODE;


	public static class DisplayLegalRequirementRecordAppExtension extends RecordAppExtension {
		private final AppLayerFactory appLayerFactory;
		private final String collection;

		public DisplayLegalRequirementRecordAppExtension(String collection, AppLayerFactory appLayerFactory) {

			this.appLayerFactory = appLayerFactory;
			this.collection = collection;
		}

		@Override
		public List<MetadataValueVO> addSyntheticMetadataValues(AddSyntheticMetadataValuesParams params) {
			List<MetadataValueVO> extraMetadataValues;

			Record record = params.getRecord();
			String schemaCode = record.getSchemaCode();
			String schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaCode);

			if (schemaTypeCode.equals(LegalRequirement.SCHEMA_TYPE)) {
				extraMetadataValues = new ArrayList<>();
				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

				List<Category> allCategories = rm.getAllCategories();
				List<Category> legalRequirementCategories = allCategories.stream()
						.filter(category -> ((List<String>) category.get(Category.LEGAL_REQUIREMENTS))
								.contains(record.getId()))
						.collect(Collectors.toList());



				Set<String> distinctRetentionRules = new HashSet<>();
				legalRequirementCategories.stream()
						.flatMap(category -> category.getRententionRules().stream())
						.forEach(distinctRetentionRules::add);

				MetadataVO retentionRuleSyntheticMetadata = params.getSyntheticMetadataVOBuilder().build(new SyntheticMetadataVOBuildingArgs(
						LegalRequirement.DEFAULT_SCHEMA + "_" + LEGAL_REQUIREMENT_RETENTION_RULES_SYNTHETIC_LOCAL_CODE,
						Category.SCHEMA_TYPE,
						Category.DEFAULT_SCHEMA, $("LegalRequirementManagement.display.retentionRule"),
						MetadataSortingType.ALPHANUMERICAL_ORDER) {
					@Override
					public boolean isReadOnly() {
						return true;
					}
				});

				MetadataVO categoriesSyntheticMetadata = params.getSyntheticMetadataVOBuilder().build(new SyntheticMetadataVOBuildingArgs(
						LegalRequirement.DEFAULT_SCHEMA + "_" + LEGAL_REQUIREMENT_CATEGORIES_SYNTHETIC_LOCAL_CODE,
						Category.SCHEMA_TYPE,
						Category.DEFAULT_SCHEMA,
						$("LegalRequirementManagement.display.category"),
						MetadataSortingType.ALPHANUMERICAL_ORDER) {
					@Override
					public boolean isReadOnly() {
						return true;
					}
				});

				extraMetadataValues.add(new MetadataValueVO(categoriesSyntheticMetadata, legalRequirementCategories.stream().map(Category::getId).collect(Collectors.toList())));
				extraMetadataValues.add(new MetadataValueVO(retentionRuleSyntheticMetadata, new ArrayList<>(distinctRetentionRules)));
			} else {
				extraMetadataValues = super.addSyntheticMetadataValues(params);
			}

			return extraMetadataValues;
		}
	}
}

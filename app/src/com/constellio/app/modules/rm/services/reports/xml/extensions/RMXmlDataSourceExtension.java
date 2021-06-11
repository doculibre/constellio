package com.constellio.app.modules.rm.services.reports.xml.extensions;

import com.constellio.app.api.extensions.XmlDataSourceExtension;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.LegalRequirement;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.RetentionRuleDocumentType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.entities.schemas.Schemas.LOGICALLY_DELETED_STATUS;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class RMXmlDataSourceExtension extends XmlDataSourceExtension {

	private final RMSchemasRecordsServices rm;
	private final SearchServices searchServices;

	public RMXmlDataSourceExtension(String collection, AppLayerFactory appLayerFactory) {
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
	}

	@Override
	public void addExtraMetadataInformation(XmlDataSourceExtensionExtraMetadataInformationParams params) {
		if (!params.getRecord().isOfSchemaType(RetentionRuleDocumentType.SCHEMA_TYPE)) {
			return;
		}
		if (params.getMetadata() == null || !params.getMetadata().getLocalCode().equals(RetentionRuleDocumentType.RETENTION_RULE_COPY)) {
			return;
		}

		String retentionRuleCopyCode = null;
		String retentionRuleCopyId = params.getRecord().get(params.getMetadata());
		if (retentionRuleCopyId != null) {
			String retentionRuleId = params.getRecord().get(rm.retentionRuleDocumentType.retentionRule());
			if (retentionRuleId != null) {
				RetentionRule retentionRule = rm.getRetentionRule(retentionRuleId);
				retentionRuleCopyCode = retentionRule.getCopyRetentionRuleWithId(retentionRuleCopyId).getCode();
			}
		}
		params.getElement().addElement(params.getMetadata().getLocalCode() + "_code")
				.addText(retentionRuleCopyCode != null ? retentionRuleCopyCode : params.getNullValue());
	}

	@Override
	public Map<String, List<Record>> getExtraReferences(XmlDataSourceExtensionExtraReferencesParams params) {
		if (params.getRecord().isOfSchemaType(LegalRequirement.SCHEMA_TYPE)) {
			String legalRequirementId = params.getRecord().getId();
			List<Record> legalRequirementReferences = searchServices.search(
					new LogicalSearchQuery(from(rm.legalRequirementReference.schemaType())
							.where(rm.legalRequirementReference.ruleRequirement()).isEqualTo(legalRequirementId)
							.andWhere(LOGICALLY_DELETED_STATUS).isFalseOrNull()));

			Map<String, List<Record>> extraReferences = new HashMap<>();
			extraReferences.put("legalRequirements", legalRequirementReferences);
			return extraReferences;
		} else if (params.getRecord().isOfSchemaType(RetentionRule.SCHEMA_TYPE)) {
			String retentionRuleId = params.getRecord().getId();
			List<Record> categories = searchServices.search(new LogicalSearchQuery(from(rm.category.schemaType())
					.where(rm.category.retentionRules()).isEqualTo(retentionRuleId)
					.andWhere(LOGICALLY_DELETED_STATUS).isFalseOrNull()));
			Map<String, List<Record>> extraReferences = new HashMap<>();
			extraReferences.put("categories", categories);
			return extraReferences;
		}

		return Collections.emptyMap();
	}

}

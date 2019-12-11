package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.PagesComponentsExtension;
import com.constellio.app.api.extensions.params.DecorateMainComponentAfterInitExtensionParams;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.MetadataMainCopyRuleFieldFactory;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.pages.management.schemas.metadata.AddEditMetadataViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.constellio.app.modules.rm.wrappers.Folder.MAIN_COPY_RULE_ID_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Folder.RETENTION_RULE_ENTERED;

public class RMMetadataMainCopyRuleFieldsExtension extends PagesComponentsExtension {
	String collection;
	AppLayerFactory appLayerFactory;

	public RMMetadataMainCopyRuleFieldsExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public void decorateMainComponentBeforeViewAssembledOnViewEntered(
			DecorateMainComponentAfterInitExtensionParams params) {
		if (!(params.getMainComponent() instanceof AddEditMetadataViewImpl)) {
			return;
		}

		Map<String, String> viewParams = ParamUtils.getParamsMap(params.getViewChangeEvent().getParameters());
		String metadataCode = viewParams.get("metadataCode");
		if (StringUtils.isBlank(metadataCode)) {
			return;
		}

		String schemaCode = viewParams.get("schemaCode");
		String metadataLocalCode = metadataCode.substring(schemaCode.length() + 1);
		if (!metadataLocalCode.equals(MAIN_COPY_RULE_ID_ENTERED)) {
			return;
		}

		MetadataSchemasManager manager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchema schema = manager.getSchemaTypes(collection).getSchema(schemaCode);
		String defaultRuleId = (String) schema.getMetadata(RETENTION_RULE_ENTERED).getDefaultValue();

		AddEditMetadataViewImpl view = (AddEditMetadataViewImpl) params.getMainComponent();
		view.setFieldFactory(new MetadataMainCopyRuleFieldFactory(defaultRuleId, getCopyRetentionRule(defaultRuleId)));
	}

	private List<CopyRetentionRule> getCopyRetentionRule(String retentionRule) {
		if (StringUtils.isEmpty(retentionRule)) {
			return Collections.EMPTY_LIST;
		}

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		return rm.getRetentionRule(retentionRule).getCopyRetentionRules();
	}
}

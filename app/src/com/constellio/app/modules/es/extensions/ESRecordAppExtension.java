package com.constellio.app.modules.es.extensions;

import com.constellio.app.extensions.records.RecordAppExtension;
import com.constellio.app.extensions.records.params.BuildRecordVOParams;
import com.constellio.app.extensions.records.params.GetIconPathParams;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import org.apache.commons.lang3.StringUtils;

public class ESRecordAppExtension extends RecordAppExtension {

	private final String collection;
	private final MetadataSchemasManager manager;

	public ESRecordAppExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		manager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
	}

	@Override
	public void buildRecordVO(BuildRecordVOParams params) {
		RecordVO recordVO = params.getBuiltRecordVO();
		String schemaTypeCode = SchemaUtils.getSchemaTypeCode(recordVO.getSchema().getCode());

		if (schemaTypeCode.startsWith("connector")) {
			String resourceKey = getIconPath(schemaTypeCode);
			if (resourceKey != null) {
				recordVO.setResourceKey(resourceKey);
			}
		}
	}

	@Override
	public String getIconPathForRecord(GetIconPathParams params) {
		Record record = params.getRecord();
		String schemaCode = record.getSchemaCode();
		String schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaCode);

		String iconPath;
		if (schemaTypeCode.startsWith("connectorType")) {
			ConnectorType connectorType = new ConnectorType(record, types());
			String code = connectorType.getCode();
			iconPath = getIconPath(code);
		} else if (schemaTypeCode.startsWith("connector")) {
			String title = record.getTitle();
			if (StringUtils.isNotBlank(title) && title.indexOf(".") != -1) {
				iconPath = FileIconUtils.getIconPath(title);
			} else {
				iconPath = getIconPath(schemaTypeCode);
			}
		} else {
			iconPath = null;
		}
		return iconPath;
	}

	private String getIconPath(String schemaTypeCode) {
		return "images/icons/connectors/" + schemaTypeCode + ".png";
	}

	private MetadataSchemaTypes types() {
		return manager.getSchemaTypes(collection);
	}

	@Override
	public Resource getIconFromContent(GetIconPathParams params) {
		Resource result;
		RecordVO recordVO = params.getRecordVO();
		Record record = params.getRecord();
		if (record == null) {
			record = recordVO.getRecord();
		}
		String iconPath = getIconPathForRecord(new GetIconPathParams(record, params.isExpanded()));
		if (iconPath != null) {
			result = new ThemeResource(iconPath);
		} else {
			result = null;
		}
		return result;
	}

	@Override
	public Resource getThumbnailResourceForRecordVO(GetIconPathParams params) {
		Resource result;
		RecordVO recordVO = params.getRecordVO();
		String schemaTypeCode = SchemaUtils.getSchemaTypeCode(recordVO.getSchema().getCode());

		if (schemaTypeCode.startsWith("connector") && !schemaTypeCode.startsWith("connectorType")) {
			result = new ThemeResource("images/icons/64/document_network_64.png");
		} else {
			result = null;
		}
		return result;
	}

}

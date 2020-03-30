package com.constellio.app.modules.tasks.extensions;

import com.constellio.app.extensions.records.RecordAppExtension;
import com.constellio.app.extensions.records.params.BuildRecordVOParams;
import com.constellio.app.extensions.records.params.GetIconPathParams;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.SchemaUtils;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;

public class TaskRecordAppExtension extends RecordAppExtension {

	private static final String IMAGES_DIR = "images";

	String collection;
	AppLayerFactory appLayerFactory;

	public TaskRecordAppExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public void buildRecordVO(BuildRecordVOParams params) {
		String resourceKey = null;
		String extension = null;
		RecordVO recordVO = params.getBuiltRecordVO();

		String schemaCode = recordVO.getSchema().getCode();
		String schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaCode);
		if (schemaTypeCode.equals(Task.SCHEMA_TYPE)) {
			resourceKey = getTaskIconPath();
			extension = "task";
			setNiceTitle(recordVO, params.getRecord(), schemaTypeCode, schemaCode, Task.DESCRIPTION);
		}
		if (resourceKey != null) {
			recordVO.setResourceKey(resourceKey);
		}
		if (extension != null) {
			recordVO.setExtension(extension);
		}

	}

	private void setNiceTitle(RecordVO recordVO, Record record, String schemaTypeCode, String schemaCode,
							  String metadataCode) {
		MetadataSchemaTypes metadataSchemaTypes = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(collection);
		Metadata metadata = metadataSchemaTypes.getSchemaType(schemaTypeCode).getSchema(schemaCode).getMetadata(metadataCode);
		String niceTitle = record.get(metadata);
		if (niceTitle != null) {
			recordVO.setNiceTitle(niceTitle);
		}
	}

	@Override
	public String getIconPathForRecord(GetIconPathParams params) {
		String fileName = null;
		String schemaCode = params.getRecord().getSchemaCode();
		String schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaCode);
		if (schemaTypeCode.equals(Task.SCHEMA_TYPE)) {
			fileName = getTaskIconPath();
		}
		return fileName != null ? fileName : null;
	}

	private String getTaskIconPath() {
		return IMAGES_DIR + "/icons/task/task.png";
	}

	@Override
	public Resource getThumbnailResourceForRecordVO(GetIconPathParams params) {
		Resource result;
		RecordVO recordVO = params.getRecordVO();
		String schemaTypeCode = SchemaUtils.getSchemaTypeCode(recordVO.getSchemaCode());
		if (schemaTypeCode.toLowerCase().contains("task")) {
			result = new ThemeResource("images/icons/64/task_64.png");
		} else {
			result = null;
		}
		return result;
	}
	
}

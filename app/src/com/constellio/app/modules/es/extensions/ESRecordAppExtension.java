package com.constellio.app.modules.es.extensions;

import com.constellio.app.extensions.records.RecordAppExtension;
import com.constellio.app.extensions.records.params.BuildRecordVOParams;
import com.constellio.app.extensions.records.params.GetIconPathParams;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.services.schemas.SchemaUtils;

public class ESRecordAppExtension extends RecordAppExtension {

	private static final String IMAGES_DIR = "images";

	@Override
	public void buildRecordVO(BuildRecordVOParams params) {
		RecordVO recordVO = params.getBuiltRecordVO();
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(recordVO.getSchema().getCode());

		if (schemaTypeCode.startsWith("connector")) {
			String resourceKey = getIconPath(schemaTypeCode);
			if (resourceKey != null) {
				recordVO.setResourceKey(resourceKey);

			}
		}
	}

	@Override
	public String getIconPathForRecord(GetIconPathParams params) {
		String schemaCode = params.getRecord().getSchemaCode();
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);

		if (schemaTypeCode.startsWith("connector")) {
			return getIconPath(schemaTypeCode);
		} else {
			return null;
		}
	}

	private String getIconPath(String schemaTypeCode) {
		return IMAGES_DIR + "/icons/connectors/" + schemaTypeCode + ".png";
	}

}

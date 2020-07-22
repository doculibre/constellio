package com.constellio.app.modules.rm.extensions.api;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class RMExternalLinkVOExtension {

	public RecordVO buildExternalLinkVO(RMExternalLinkVOExtensionParams params) {
		return null;
	}

	@AllArgsConstructor
	@Getter
	public static class RMExternalLinkVOExtensionParams {
		Record record;
		VIEW_MODE viewMode;
		MetadataSchemaVO schemaVO;
		SessionContext sessionContext;
	}

}

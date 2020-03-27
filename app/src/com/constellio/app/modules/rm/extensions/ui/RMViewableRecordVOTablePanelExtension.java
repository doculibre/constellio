package com.constellio.app.modules.rm.extensions.ui;

import com.constellio.app.extensions.ui.ViewableRecordVOTablePanelExtension;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.RecordVO;

public class RMViewableRecordVOTablePanelExtension extends ViewableRecordVOTablePanelExtension {

	@Override
	public Boolean isDisplayInWindowOnSelection(ViewableRecordVOTablePanelExtensionParams params) {
		Boolean displayInWindowOnSelection;
		RecordVO recordVO = params.getRecordVO();
		String schemaTypeCode = recordVO.getSchema().getTypeCode();
		if (Folder.SCHEMA_TYPE.equals(schemaTypeCode)) {
			displayInWindowOnSelection = false;
		} else if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
			displayInWindowOnSelection = true;
		} else {
			displayInWindowOnSelection = null;
		}
		return displayInWindowOnSelection;
	}

}

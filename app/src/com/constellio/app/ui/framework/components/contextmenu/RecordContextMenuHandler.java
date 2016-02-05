package com.constellio.app.ui.framework.components.contextmenu;

import java.io.Serializable;

import com.constellio.app.ui.entities.RecordVO;

public interface RecordContextMenuHandler extends Serializable {
	
	boolean isContextMenuForSchemaCode(String schemaCode);
	
	boolean isContextMenuForSchemaTypeCode(String schemaTypeCode);
	
	boolean isContextMenuForRecordId(String recordId);
	
	boolean isContextMenu(RecordVO recordVO);

	RecordContextMenu getForSchemaTypeCode(String schemaTypeCode);

	RecordContextMenu getForSchemaCode(String schemaCode);
	
	RecordContextMenu getForRecordId(String recordId);

	RecordContextMenu get(RecordVO recordVO);

}

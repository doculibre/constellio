package com.constellio.app.ui.framework.navigation;

import java.io.Serializable;

import com.constellio.app.ui.entities.RecordVO;

public interface RecordNavigationHandler extends Serializable {

	boolean isViewForRecordId(String recordId);

	boolean isViewForSchemaCode(String schemaCode);

	boolean isViewForSchemaTypeCode(String schemaTypeCode);

	boolean isView(RecordVO recordVO);

	void navigateToView(String recordId);

	void navigateToView(RecordVO recordVO);
}

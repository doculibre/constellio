package com.constellio.app.ui.framework.components.menuBar;

import com.constellio.app.ui.entities.RecordVO;
import com.vaadin.ui.MenuBar;

import java.io.Serializable;

public interface RecordMenuBarHandler extends Serializable {

	boolean isMenuBarForSchemaCode(String schemaCode);

	boolean isMenuBarForSchemaTypeCode(String schemaTypeCode);

	boolean isMenuBarForRecordId(String recordId);

	boolean isMenuBar(RecordVO recordVO);

	MenuBar get(RecordVO recordVO);

}

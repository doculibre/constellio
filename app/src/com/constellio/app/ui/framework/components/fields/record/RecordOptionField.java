package com.constellio.app.ui.framework.components.fields.record;

import java.io.Serializable;

import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;

public interface RecordOptionField extends Serializable {
	
	void setDataProvider(RecordVODataProvider dataProvider);
	
	SessionContext getSessionContext();

}

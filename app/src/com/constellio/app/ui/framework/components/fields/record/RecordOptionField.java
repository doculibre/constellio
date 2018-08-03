package com.constellio.app.ui.framework.components.fields.record;

import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;

import java.io.Serializable;

public interface RecordOptionField extends Serializable {

	void setDataProvider(RecordVODataProvider dataProvider);

	SessionContext getSessionContext();

}

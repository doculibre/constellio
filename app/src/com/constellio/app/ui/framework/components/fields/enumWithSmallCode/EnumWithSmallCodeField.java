package com.constellio.app.ui.framework.components.fields.enumWithSmallCode;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.EnumWithSmallCode;

public interface EnumWithSmallCodeField extends Serializable {
	
	void setOptions(List<EnumWithSmallCode> enumConstants);

	SessionContext getSessionContext();

}

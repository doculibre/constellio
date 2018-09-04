package com.constellio.app.ui.framework.components.fields.enumWithSmallCode;

import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.EnumWithSmallCode;

import java.io.Serializable;
import java.util.List;

public interface EnumWithSmallCodeField extends Serializable {

	void setOptions(List<EnumWithSmallCode> enumConstants);

	SessionContext getSessionContext();

}

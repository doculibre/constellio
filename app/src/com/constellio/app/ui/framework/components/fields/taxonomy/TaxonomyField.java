package com.constellio.app.ui.framework.components.fields.taxonomy;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.SessionContext;

public interface TaxonomyField extends Serializable {
	
	void setOptions(List<RecordVO> recordVOs);

	SessionContext getSessionContext();

}

package com.constellio.app.ui.framework.components.fields.taxonomy;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.SessionContext;

import java.io.Serializable;
import java.util.List;

public interface TaxonomyField extends Serializable {

	void setOptions(List<RecordVO> recordVOs);

	SessionContext getSessionContext();

}

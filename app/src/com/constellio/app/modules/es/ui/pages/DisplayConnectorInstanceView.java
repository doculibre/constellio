package com.constellio.app.modules.es.ui.pages;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BaseView;

public interface DisplayConnectorInstanceView extends BaseView {

	void setRecord(RecordVO recordVO);
	
	void setDocumentsCount(long count);
	
	void setLastDocuments(String lastDocuments);
	
}

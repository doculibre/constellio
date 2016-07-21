package com.constellio.app.ui.pages.management.sequence;

import java.util.List;

import com.constellio.app.ui.entities.SequenceVO;
import com.constellio.app.ui.pages.base.BaseView;

public interface ListSequencesView extends BaseView {
	
	String getRecordId();
	
	void setSequenceVOs(List<SequenceVO> sequenceVOs);
	
	void showErrorMessage(String message);
	
	void closeWindow();

}

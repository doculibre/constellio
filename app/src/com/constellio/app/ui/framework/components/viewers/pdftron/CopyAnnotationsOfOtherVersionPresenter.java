package com.constellio.app.ui.framework.components.viewers.pdftron;

import com.constellio.app.ui.entities.ContentVersionVO;

import java.util.List;

public interface CopyAnnotationsOfOtherVersionPresenter {
	List<ContentVersionVO> getAvalibleVersion();

	void addAnnotation(ContentVersionVO contentVErsionVO);
}

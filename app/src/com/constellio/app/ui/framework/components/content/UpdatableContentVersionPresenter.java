package com.constellio.app.ui.framework.components.content;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;

public interface UpdatableContentVersionPresenter {
	public ContentVersionVO getUpdatedContentVersionVO(RecordVO recordVO, ContentVersionVO previousConventVersionVO);
}

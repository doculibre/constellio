package com.constellio.app.ui.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class SearchResultVO implements Serializable {
	private final RecordVO recordVO;
	private final Map<String, List<String>> highlights;

	public SearchResultVO(RecordVO recordVO, Map<String, List<String>> highlights) {
		this.recordVO = recordVO;
		this.highlights = highlights;
	}

	public RecordVO getRecordVO() {
		return recordVO;
	}

	public Map<String, List<String>> getHighlights() {
		return highlights;
	}
}

package com.constellio.app.ui.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class SearchResultVO implements Serializable {

	private int index;
	private final RecordVO recordVO;
	private final Map<String, List<String>> highlights;

	public SearchResultVO(int index, RecordVO recordVO, Map<String, List<String>> highlights) {
		this.index = index;
		this.recordVO = recordVO;
		this.highlights = highlights;
	}

	public int getIndex() {
		return index;
	}

	public RecordVO getRecordVO() {
		return recordVO;
	}

	public Map<String, List<String>> getHighlights() {
		return highlights;
	}
}

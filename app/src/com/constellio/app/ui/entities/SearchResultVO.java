package com.constellio.app.ui.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SearchResultVO implements Serializable {

	private int index;
	private final RecordVO recordVO;
	private final Map<String, List<String>> highlights;
	private boolean isDeleted;

	public SearchResultVO(int index, boolean isDeleted) {
		this.isDeleted = isDeleted;
		this.highlights = null;
		this.recordVO = null;
		this.index = index;
	}

	public SearchResultVO(int index, RecordVO recordVO, Map<String, List<String>> highlights) {
		this.index = index;
		this.recordVO = recordVO;
		this.highlights = highlights;
	}

	/***
	 * 	 Cas où entre la recherche dans solr et l'obtention du record dans la cache le
	 * 	 record a été supprimé de la cache.
	 */
	public boolean isDeleted() {
		return isDeleted;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SearchResultVO that = (SearchResultVO) o;
		return index == that.index &&
			   Objects.equals(recordVO, that.recordVO);
	}

	@Override
	public int hashCode() {
		return Objects.hash(index, recordVO);
	}
}

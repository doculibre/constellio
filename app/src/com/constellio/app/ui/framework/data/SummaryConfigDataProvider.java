package com.constellio.app.ui.framework.data;

import com.constellio.app.ui.entities.SummaryConfigElementVO;

import java.util.Collections;
import java.util.List;

public class SummaryConfigDataProvider extends AbstractDataProvider {

	private List<SummaryConfigElementVO> summaryConfigElementVOS;

	public SummaryConfigDataProvider(List<SummaryConfigElementVO> summaryConfigElementVOS) {
		this.summaryConfigElementVOS = summaryConfigElementVOS;
	}

	public SummaryConfigElementVO get(int index) {
		return summaryConfigElementVOS.get(index);
	}

	public SummaryConfigElementVO get(String metadataCode) {
		SummaryConfigElementVO match = null;
		for (SummaryConfigElementVO summaryConfigElementVO : summaryConfigElementVOS) {
			if (summaryConfigElementVO.getMetadataVO().getCode().equals(metadataCode)) {
				match = summaryConfigElementVO;
				break;
			}
		}
		return match;
	}

	public List<SummaryConfigElementVO> getSummaryConfigElementVOS() {
		return Collections.unmodifiableList(summaryConfigElementVOS);
	}

	public void setSummaryConfigElementVOS(List<SummaryConfigElementVO> summaryConfigElementVOS) {
		this.summaryConfigElementVOS = summaryConfigElementVOS;
	}

	public void addSummaryConfigItemVO(int index, SummaryConfigElementVO summaryConfigElementVO) {
		summaryConfigElementVOS.add(index, summaryConfigElementVO);
	}

	public void addSummaryConfigItemVO(SummaryConfigElementVO summaryConfigElementVO) {
		summaryConfigElementVOS.add(summaryConfigElementVO);
	}

	public void removeSummaryConfigItemVO(SummaryConfigElementVO summaryConfigElementVO) {
		summaryConfigElementVOS.remove(summaryConfigElementVO);
	}

	public SummaryConfigElementVO removeSummaryConfigItemVO(int index) {
		return summaryConfigElementVOS.remove(index);
	}

	public void clear() {
		summaryConfigElementVOS.clear();
	}

}

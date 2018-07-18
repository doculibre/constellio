package com.constellio.app.ui.framework.data;

import com.constellio.app.ui.entities.SummaryColumnVO;

import java.util.Collections;
import java.util.List;

public class SummaryColumnDataProvider extends AbstractDataProvider {

    private List<SummaryColumnVO> summaryColumnVOs;

    public SummaryColumnDataProvider(List<SummaryColumnVO> summaryColumnVOs) {
        this.summaryColumnVOs = summaryColumnVOs;
    }

    public SummaryColumnVO get(int index) {
        return summaryColumnVOs.get(index);
    }

    public SummaryColumnVO get(String metadataCode) {
        SummaryColumnVO match = null;
        for (SummaryColumnVO summaryColumnVO : summaryColumnVOs) {
            if (summaryColumnVO.getMetadataVO().getCode().equals(metadataCode)) {
                match = summaryColumnVO;
                break;
            }
        }
        return match;
    }

    public List<SummaryColumnVO> getSummaryColumnVOs() {
        return Collections.unmodifiableList(summaryColumnVOs);
    }

    public void setSummaryColumnVOs(List<SummaryColumnVO> summaryColumnVOs) {
        this.summaryColumnVOs = summaryColumnVOs;
    }

    public void addSummaryColumnVO(int index,SummaryColumnVO summaryColumnVO) {
        summaryColumnVOs.add(index, summaryColumnVO);
    }

    public void addSummaryColumnVO(SummaryColumnVO summaryColumnVO) {
        summaryColumnVOs.add(summaryColumnVO);
    }

    public void removeSummaryColumnVO(SummaryColumnVO summaryColumnVO) {
        summaryColumnVOs.remove(summaryColumnVO);
    }

    public SummaryColumnVO removeSummaryColumnVO(int index) {
        return summaryColumnVOs.remove(index);
    }

    public void clear() {
        summaryColumnVOs.clear();
    }

}

package com.constellio.app.ui.framework.data;

import com.constellio.app.ui.entities.BatchProcessVO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BatchProcessDataProvider extends AbstractDataProvider {

	private List<BatchProcessVO> batchProcessVOs;

	public BatchProcessDataProvider() {
		this(new ArrayList<BatchProcessVO>());
	}

	public BatchProcessDataProvider(List<BatchProcessVO> batchProcessVOs) {
		this.batchProcessVOs = batchProcessVOs;
	}

	public BatchProcessVO get(int index) {
		return batchProcessVOs.get(index);
	}

	public BatchProcessVO get(String id) {
		BatchProcessVO match = null;
		for (BatchProcessVO batchProcessVO : batchProcessVOs) {
			if (batchProcessVO.getId().equals(id)) {
				match = batchProcessVO;
				break;
			}
		}
		return match;
	}

	public List<BatchProcessVO> getBatchProcessVOs() {
		return Collections.unmodifiableList(batchProcessVOs);
	}

	public void setBatchProcessVOs(List<BatchProcessVO> batchProcessVOs) {
		this.batchProcessVOs = batchProcessVOs;
	}

	public void addBatchProcess(BatchProcessVO batchProcessVO) {
		batchProcessVOs.add(batchProcessVO);
	}

	public void removeBatchProcess(BatchProcessVO batchProcessVO) {
		batchProcessVOs.remove(batchProcessVO);
	}

	public void clear() {
		batchProcessVOs.clear();
	}

}

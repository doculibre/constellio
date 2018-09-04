package com.constellio.app.ui.entities;

import com.constellio.model.entities.schemas.Schemas;
import org.joda.time.LocalDateTime;

import java.util.List;

import static com.constellio.model.entities.records.wrappers.TemporaryRecord.CONTENT;
import static com.constellio.model.entities.records.wrappers.TemporaryRecord.DAY_BEFORE_DESTRUCTION;
import static com.constellio.model.entities.records.wrappers.TemporaryRecord.DESTRUCTION_DATE;

public class TemporaryRecordVO extends RecordVO {

	public TemporaryRecordVO(String id, List<MetadataValueVO> metadataValues, VIEW_MODE viewMode) {
		super(id, metadataValues, viewMode);
	}

	public TemporaryRecordVO(RecordVO recordVO) {
		super(recordVO.getId(), recordVO.getMetadataValues(), recordVO.getViewMode());
	}

	public LocalDateTime getCreatedOn() {
		return get(Schemas.CREATED_ON);
	}

	public void setCreatedOn(LocalDateTime creationDate) {
		set(Schemas.CREATED_ON, creationDate);
	}

	public LocalDateTime getDestructionDate() {
		return get(DESTRUCTION_DATE);
	}

	public void setDestructionDate(LocalDateTime destructionDate) {
		set(DESTRUCTION_DATE, destructionDate);
	}

	public ContentVersionVO getContent() {
		return get(CONTENT);
	}

	public void setContent(ContentVersionVO content) {
		set(CONTENT, content);
	}

	public double getNumberOfDaysBeforeDestruction() {
		return Double.parseDouble((String) get(DAY_BEFORE_DESTRUCTION));
	}

	public void setNumberOfDaysBeforeDestruction(int numberOfDaysBeforeDestruction) {
		set(DAY_BEFORE_DESTRUCTION, new Double(numberOfDaysBeforeDestruction));
	}

}

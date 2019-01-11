package com.constellio.app.ui.framework.builders;

import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserDocumentVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;

import java.util.List;

public class UserDocumentToVOBuilder extends RecordToVOBuilder {

	@Override
	protected RecordVO newRecordVO(String id, List<MetadataValueVO> metadataValueVOs, VIEW_MODE viewMode, List<String> excludedMetadata) {
		return new UserDocumentVO(id, metadataValueVOs, viewMode, excludedMetadata);
	}

	@Override
	public UserDocumentVO build(Record record, VIEW_MODE viewMode, SessionContext sessionContext) {
		return (UserDocumentVO) super.build(record, viewMode, sessionContext);
	}

}

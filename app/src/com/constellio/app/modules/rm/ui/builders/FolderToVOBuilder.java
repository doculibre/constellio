package com.constellio.app.modules.rm.ui.builders;

import com.constellio.app.modules.rm.ui.entities.FolderVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;

import java.util.List;

public class FolderToVOBuilder extends RecordToVOBuilder {

	@Override
	public FolderVO build(Record record, VIEW_MODE viewMode, SessionContext sessionContext) {
		return (FolderVO) super.build(record, viewMode, sessionContext);
	}

	@Override
	protected FolderVO newRecordVO(String id, List<MetadataValueVO> metadataValueVOs, VIEW_MODE viewMode, List<String> excludedMetadata) {
		return new FolderVO(id, metadataValueVOs, viewMode, excludedMetadata);
	}
}

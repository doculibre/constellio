package com.constellio.app.modules.rm.ui.builders;

import java.util.List;

import com.constellio.app.modules.rm.ui.entities.FolderVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;

public class FolderToVOBuilder extends RecordToVOBuilder {

	@Override
	public FolderVO build(Record record, VIEW_MODE viewMode) {
		return (FolderVO) super.build(record, viewMode);
	}

	@Override
	public FolderVO build(Record record, VIEW_MODE viewMode, SessionContext sessionContext) {
		return (FolderVO) super.build(record, viewMode, sessionContext);
	}

	@Override
	public FolderVO build(Record record, VIEW_MODE viewMode, MetadataSchemaVO schemaVO) {
		return (FolderVO) super.build(record, viewMode, schemaVO);
	}

	@Override
	protected FolderVO newRecordVO(String id, List<MetadataValueVO> metadataValueVOs, VIEW_MODE viewMode) {
		return new FolderVO(id, metadataValueVOs, viewMode);
	}

}

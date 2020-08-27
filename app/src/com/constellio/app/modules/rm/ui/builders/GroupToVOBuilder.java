package com.constellio.app.modules.rm.ui.builders;

import com.constellio.app.ui.entities.GroupVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;

import java.util.List;

public class GroupToVOBuilder extends RecordToVOBuilder {

	@Override
	public GroupVO build(Record record, VIEW_MODE viewMode, SessionContext sessionContext) {
		return (GroupVO) super.build(record, viewMode, sessionContext);
	}

	@Override
	protected GroupVO newRecordVO(String id, List<MetadataValueVO> metadataValueVOs, VIEW_MODE viewMode,
								  List<String> excludedMetdataCode) {
		return new GroupVO(id, metadataValueVOs, viewMode);
	}
}
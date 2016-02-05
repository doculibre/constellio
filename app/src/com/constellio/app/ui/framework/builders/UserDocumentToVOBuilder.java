package com.constellio.app.ui.framework.builders;

import java.util.List;

import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserDocumentVO;

public class UserDocumentToVOBuilder extends RecordToVOBuilder {

	@Override
	protected RecordVO newRecordVO(String id, List<MetadataValueVO> metadataValueVOs, VIEW_MODE viewMode) {
		return new UserDocumentVO(id, metadataValueVOs, viewMode);
	}


}

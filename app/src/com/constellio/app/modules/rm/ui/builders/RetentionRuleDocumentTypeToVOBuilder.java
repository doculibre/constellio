package com.constellio.app.modules.rm.ui.builders;

import com.constellio.app.modules.rm.ui.entities.RetentionRuleDocumentTypeVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RetentionRuleDocumentTypeToVOBuilder extends RecordToVOBuilder {


	@Override
	public RetentionRuleDocumentTypeVO build(Record record, VIEW_MODE viewMode, SessionContext sessionContext) {
		return (RetentionRuleDocumentTypeVO) super.build(record, viewMode, sessionContext);
	}

	@Override
	protected RetentionRuleDocumentTypeVO newRecordVO(String id, List<MetadataValueVO> metadataValueVOs,
													  VIEW_MODE viewMode,
													  List<String> excludedMetdataCode) {
		return new RetentionRuleDocumentTypeVO(id, metadataValueVOs, viewMode) {
			@Override
			public List<MetadataValueVO> getTableMetadataValues() {
				List<MetadataValueVO> tableMetadataValues = super.getTableMetadataValues();

				List<String> metadatasToRemoveFromDisplay = getMetadatasToRemoveFromDisplay();

				if (metadatasToRemoveFromDisplay != null) {
					tableMetadataValues = tableMetadataValues.stream().filter(metadataValueVO -> !metadatasToRemoveFromDisplay.contains(metadataValueVO.getMetadata().getLocalCode())).collect(Collectors.toList());
				}

				return tableMetadataValues;
			}
		};
	}

	public List<String> getMetadatasToRemoveFromDisplay() {
		return new ArrayList<>();
	}
}

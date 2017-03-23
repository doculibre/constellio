package com.constellio.app.modules.rm.ui.components.retentionRule;

import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class RetentionRuleReferenceDisplay extends ReferenceDisplay {

	public RetentionRuleReferenceDisplay(RecordVO recordVO) {
		super(recordVO);
	}

	public RetentionRuleReferenceDisplay(String recordId) {
		super(recordId);
	}

	@Override
	protected String getNiceTitle(Record record, MetadataSchemaTypes types) {
		RetentionRule retentionRule = new RetentionRule(record, types);
		return new RetentionRuleInfoBuilder(retentionRule).getInfo();
	}

}

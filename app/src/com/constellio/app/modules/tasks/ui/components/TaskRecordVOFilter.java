package com.constellio.app.modules.tasks.ui.components;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.data.RecordVOFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class TaskRecordVOFilter extends RecordVOFilter {

	public TaskRecordVOFilter(MetadataVO propertyId, Object value) {
		super(propertyId, value);
	}

	@Override
	public void addCondition(LogicalSearchQuery query) {
		if (getPropertyId().codeMatches(Task.STARRED_BY_USERS) && getValue() != null) {
			TaskFilterGenerator.SpecialBoolean value = (TaskFilterGenerator.SpecialBoolean) getValue();
			if (value.getBoolean()) {
				query.setCondition(query.getCondition().andWhere(getMetadata()).isNotNull());
			} else {
				query.setCondition(query.getCondition().andWhere(getMetadata()).isNull());
			}
		}else{
			super.addCondition(query);
		}
	}
}

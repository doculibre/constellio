package com.constellio.app.modules.tasks.ui.components;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.data.RecordVOFilter;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.commons.lang3.StringUtils;

public class DemoRecordVOFilter extends RecordVOFilter {
	private static final String LINKED_WORKFLOW_EXECUTION = "linkedWorkflowExecution";
	private static final String LINKED_WORKFLOW_DEFINITION = "linkedWorkflowDefinition";

	public DemoRecordVOFilter(MetadataVO propertyId, Object value) {
		super(propertyId, value);
	}

	@Override
	public void addCondition(LogicalSearchQuery query) {
		if (getPropertyId().codeMatches(Task.STARRED_BY_USERS) && getValue() != null) {
			DemoFilterGenerator.SpecialBoolean value = (DemoFilterGenerator.SpecialBoolean) getValue();
			if (value.getBoolean()) {
				query.setCondition(query.getCondition().andWhere(getMetadata()).isNotNull());
			} else {
				query.setCondition(query.getCondition().andWhere(getMetadata()).isNull());
			}
		} else if(getPropertyId().codeMatches(LINKED_WORKFLOW_EXECUTION) ) {
			SchemaPresenterUtils schemaPresenterUtils = getSchemaPresenterUtils();
			Metadata metadata = schemaPresenterUtils.getMetadata(LINKED_WORKFLOW_DEFINITION);
			String stringValue = (String) getValue();
			if (StringUtils.isNotBlank(stringValue)) {
				query.setCondition(query.getCondition().andWhere(metadata).isEqualTo(getValue()));
			}
		}else{
			super.addCondition(query);
		}
	}
}

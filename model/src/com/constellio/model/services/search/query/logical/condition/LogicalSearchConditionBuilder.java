package com.constellio.model.services.search.query.logical.condition;

import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchCondition;

public interface LogicalSearchConditionBuilder {

	LogicalSearchCondition build(OngoingLogicalSearchCondition ongoing);

}

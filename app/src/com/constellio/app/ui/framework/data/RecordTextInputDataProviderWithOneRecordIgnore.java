package com.constellio.app.ui.framework.data;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class RecordTextInputDataProviderWithOneRecordIgnore extends RecordTextInputDataProvider {

	private String ignoredRecordId;

	public RecordTextInputDataProviderWithOneRecordIgnore(ConstellioFactories constellioFactories,
														  SessionContext sessionContext, String schemaTypeCode,
														  boolean writeAccess, String ignoredRecordId) {
		super(constellioFactories, sessionContext, schemaTypeCode, writeAccess);
		this.ignoredRecordId = ignoredRecordId;
	}

	@Override
	public LogicalSearchQuery getQuery(User user, String text, int startIndex, int count) {
		LogicalSearchQuery logicalSearchQuery = super.getQuery(user, text, startIndex, count);
		logicalSearchQuery.setCondition(logicalSearchQuery.getCondition().andWhere(Schemas.IDENTIFIER).isNotEqual(ignoredRecordId));
		return logicalSearchQuery;
	}
}

package com.constellio.model.entities.security;

import static com.constellio.data.utils.LangUtils.compare;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.data.utils.LangUtils.ListComparisonResults;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;

public class TransactionSecurityModel implements SecurityModel {

	SecurityModel nestedSecurityModel;

	Transaction transaction;

	public TransactionSecurityModel(SecurityModel nestedSecurityModel, Transaction transaction) {
		this.nestedSecurityModel = nestedSecurityModel;
		this.transaction = transaction;
	}

	@Override
	public List<SecurityModelAuthorization> getAuthorizationsOnTarget(String id) {

		List<SecurityModelAuthorization> nestedSecurityModelAuths = nestedSecurityModel.getAuthorizationsOnTarget(id);

		Map<String, Integer> index = new HashMap<>();
		for (int i = 0; i < nestedSecurityModelAuths.size(); i++) {
			index.put(nestedSecurityModelAuths.get(i).details.getId(), i);
		}

		for (SecurityModelAuthorization authorization : nestedSecurityModelAuths) {

			for (Record record : transaction.getRecords()) {

				if (User.SCHEMA_TYPE.equals(record.getTypeCode())) {
					if (record.isModified(Schemas.AUTHORIZATIONS)) {

						ListComparisonResults<String> comparisonResults = compare(record.getCopyOfOriginalRecord()
								.<String>getList(Schemas.AUTHORIZATIONS), record.<String>getList(Schemas.AUTHORIZATIONS));

						for (String newAuthsOnUser : comparisonResults.getNewItems()) {

						}

						for (String removedAuthsOnUser : comparisonResults.getRemovedItems()) {

						}
					}
				}

				if (Group.SCHEMA_TYPE.equals(record.getTypeCode())) {
					if (record.isModified(Schemas.AUTHORIZATIONS)) {

						ListComparisonResults<String> comparisonResults = compare(record.getCopyOfOriginalRecord()
								.<String>getList(Schemas.AUTHORIZATIONS), record.<String>getList(Schemas.AUTHORIZATIONS));

						for (String newAuthsOnUser : comparisonResults.getNewItems()) {

						}

						for (String removedAuthsOnUser : comparisonResults.getRemovedItems()) {

						}
					}
				}
			}
		}

		if (SolrAuthorizationDetails.SCHEMA_TYPE.equals(record.getTypeCode())) {

		}

		return null;
	}
}

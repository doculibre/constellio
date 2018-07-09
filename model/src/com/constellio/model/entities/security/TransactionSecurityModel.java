package com.constellio.model.entities.security;

import java.util.List;

import com.constellio.model.entities.records.Transaction;

public class TransactionSecurityModel implements SecurityModel {

	SecurityModel nestedSecurityModel;

	Transaction transaction;

	public TransactionSecurityModel(SecurityModel nestedSecurityModel, Transaction transaction) {
		this.nestedSecurityModel = nestedSecurityModel;
		this.transaction = transaction;
	}

	@Override
	public List<Authorization> getAuthorizationsOnTarget(String id) {

		List<Authorization> authorizations = nestedSecurityModel.getAuthorizationsOnTarget(id);

		for (Authorization authorization : authorizations) {

		}

		return authorizations;
	}
}

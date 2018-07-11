package com.constellio.model.entities.security;

import static com.constellio.data.utils.LangUtils.compare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.data.utils.LangUtils.ListComparisonResults;
import com.constellio.data.utils.Provider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.security.roles.Roles;

public class TransactionSecurityModel implements SecurityModel {

	SecurityModel nestedSecurityModel;

	Transaction transaction;

	Roles roles;
	MetadataSchemaTypes types;

	public TransactionSecurityModel(MetadataSchemaTypes types, Roles roles, SecurityModel nestedSecurityModel,
			Transaction transaction) {
		this.nestedSecurityModel = nestedSecurityModel;
		this.transaction = transaction;
		this.roles = roles;
		this.types = types;
	}

	@Override
	public List<SecurityModelAuthorization> getAuthorizationsOnTarget(String id) {

		final List<SecurityModelAuthorization> nestedSecurityModelAuths = nestedSecurityModel.getAuthorizationsOnTarget(id);
		final List<SecurityModelAuthorization> returnedAuths = new ArrayList<>(nestedSecurityModelAuths);

		final Map<String, Integer> indexMap = new HashMap<>();

		for (int i = 0; i < nestedSecurityModelAuths.size(); i++) {
			indexMap.put(nestedSecurityModelAuths.get(i).details.getId(), i);
		}

		Provider<String, SecurityModelAuthorization> modifiableAuthProvider = new Provider<String, SecurityModelAuthorization>() {
			@Override
			public SecurityModelAuthorization get(String input) {
				Integer index = indexMap.get(input);
				SecurityModelAuthorization nestedSecurityModelAuth = nestedSecurityModelAuths.get(index);
				SecurityModelAuthorization returnedAuth = returnedAuths.get(index);

				if (returnedAuth == nestedSecurityModelAuth) {
					returnedAuth = new SecurityModelAuthorization(nestedSecurityModelAuth);
					returnedAuths.set(index, returnedAuth);
				}

				return returnedAuth;
			}
		};

		for (Record record : transaction.getRecords()) {
			if (SolrAuthorizationDetails.SCHEMA_TYPE.equals(record.getTypeCode())) {
				SolrAuthorizationDetails solrAuthorizationDetails = SolrAuthorizationDetails.wrapNullable(record, types);
				if (id.equals(solrAuthorizationDetails.getTarget())) {
					Integer index = indexMap.get(solrAuthorizationDetails.getId());
					if (index == null) {
						returnedAuths.add(new SecurityModelAuthorization(solrAuthorizationDetails));
					} else {

						SecurityModelAuthorization newVersion = new SecurityModelAuthorization(solrAuthorizationDetails);
						SecurityModelAuthorization oldVersion = nestedSecurityModelAuths.get(index);

						for (User previousUser : oldVersion.getUsers()) {
							newVersion.addUser(previousUser);
						}

						for (Group previousGroup : oldVersion.getGroups()) {
							newVersion.addGroup(previousGroup);
						}

						returnedAuths.set(index, newVersion);
					}
				}
			}
		}

		for (Record record : transaction.getRecords()) {

			if (User.SCHEMA_TYPE.equals(record.getTypeCode())) {
				if (record.isModified(Schemas.AUTHORIZATIONS)) {
					User user = User.wrapNullable(record, types, roles);
					ListComparisonResults<String> comparisonResults = compare(
							user.getCopyOfOriginalRecord().getUserAuthorizations(), user.getUserAuthorizations());

					for (String newAuthsOnUser : comparisonResults.getNewItems()) {
						modifiableAuthProvider.get(newAuthsOnUser).addUser(user);
					}

					for (String removedAuthsOnUser : comparisonResults.getRemovedItems()) {
						modifiableAuthProvider.get(removedAuthsOnUser).removeUser(user);
					}
				}
			}

			if (Group.SCHEMA_TYPE.equals(record.getTypeCode())) {
				if (record.isModified(Schemas.AUTHORIZATIONS)) {
					Group group = Group.wrapNullable(record, types);
					ListComparisonResults<String> comparisonResults = compare(record.getCopyOfOriginalRecord()
							.<String>getList(Schemas.AUTHORIZATIONS), record.<String>getList(Schemas.AUTHORIZATIONS));

					for (String newAuthsOnGroup : comparisonResults.getNewItems()) {
						modifiableAuthProvider.get(newAuthsOnGroup).addGroup(group);
					}

					for (String removedAuthsOnGroup : comparisonResults.getRemovedItems()) {
						modifiableAuthProvider.get(removedAuthsOnGroup).removeGroup(group);
					}
				}
			}
		}

		return returnedAuths;
	}

}

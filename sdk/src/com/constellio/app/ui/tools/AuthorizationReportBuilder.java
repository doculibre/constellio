package com.constellio.app.ui.tools;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.KeyIntMap;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.returnAll;

public class AuthorizationReportBuilder {

	KeyIntMap<String> recordsTargettedByAuth = new KeyIntMap<>();
	KeyIntMap<String> principalsAuths = new KeyIntMap<>();
	KeyIntMap<String> uniquePrincipalsAuths = new KeyIntMap<>();

	public String build(AppLayerFactory appLayerFactory) {

		StringBuilder stringBuilder = new StringBuilder();
		for (String collection : appLayerFactory.getModelLayerFactory().getCollectionsListManager()
												.getCollectionsExcludingSystem()) {

			String report = build(collection, appLayerFactory);

			stringBuilder.append("=================================================================================");
			stringBuilder.append("= " + collection);
			stringBuilder.append("=================================================================================");
			stringBuilder.append(report);

		}

		return stringBuilder.toString();
	}

	public String build(String collection, AppLayerFactory appLayerFactory) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();

		List<User> allUsers = rm.searchUsers(returnAll());
		List<Group> allGroups = rm.searchGroups(returnAll());

		List<String> allAdministrativeUnits = searchServices.cachedSearchRecordIds(new LogicalSearchQuery(
				from(rm.administrativeUnit.schemaType()).returnAll()));

		for (Record authDetailsRecord : searchServices.cachedSearch(new LogicalSearchQuery(
				from(rm.authorizationDetails.schemaType()).returnAll()))) {

			SolrAuthorizationDetails authDetails = rm.wrapSolrAuthorizationDetails(authDetailsRecord);
			if (!allAdministrativeUnits.contains(authDetails.getTarget())) {

				String principalsString = "";

				for (User user : allUsers) {
					if (user.getUserAuthorizations().contains(authDetails.getId())) {
						principalsString += (principalsString.isEmpty() ? "" : " ") + user.getUsername();
						principalsAuths.increment(user.getUsername());
					}
				}

				for (Group group : allGroups) {
					if (group.getAllAuthorizations().contains(authDetails.getId())) {
						principalsString += (principalsString.isEmpty() ? "" : " ") + group.getCode();
						principalsAuths.increment(group.getCode());
					}
				}

				List<String> roles = new ArrayList<>(authDetails.getRoles());
				Collections.sort(roles);

				String key;
				if (authDetails.getStartDate() == null && authDetails.getEndDate() == null) {
					key = principalsString + ":" + StringUtils.join(roles, ",");

					if (!recordsTargettedByAuth.containsKey(key)) {
						for (User user : allUsers) {
							if (user.getUserAuthorizations().contains(authDetails.getId())) {
								uniquePrincipalsAuths.increment(user.getUsername());
							}
						}

						for (Group group : allGroups) {
							if (group.getAllAuthorizations().contains(authDetails.getId())) {
								uniquePrincipalsAuths.increment(group.getCode());
							}
						}
					}

				} else {
					key = authDetails.getId();
				}
				recordsTargettedByAuth.increment(key);
			}
		}

		StringBuilder report = new StringBuilder();
		report.append("Most used authorizations\n :");

		for (Entry<String, Integer> entry : recordsTargettedByAuth.entriesSortedByDescValue()) {
			report.append("\t" + entry.getKey() + " : " + entry.getValue() + "\n");
		}

		report.append("\n\n\n\nMost used principals\n :");

		for (Entry<String, Integer> entry : principalsAuths.entriesSortedByDescValue()) {
			report.append("\t" + entry.getKey() + " : " + entry.getValue() + "\n");
		}

		report.append("\n\n\n\nMost used principals (regrouping auths)\n :");

		for (Entry<String, Integer> entry : uniquePrincipalsAuths.entriesSortedByDescValue()) {
			report.append("\t" + entry.getKey() + " : " + entry.getValue() + "\n");
		}

		return report.toString();
	}

}

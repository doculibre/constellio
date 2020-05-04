package com.constellio.app.modules.restapi.validation.dao;

import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.core.util.MapUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ValidationDao extends BaseDao {

	public List<String> getUserTokens(String serviceKey) {
		return getUserTokens(serviceKey, false, false);
	}

	public List<String> getUserTokens(String serviceKey, boolean sortByDescDate) {
		return getUserTokens(serviceKey, false, sortByDescDate);
	}

	public List<String> getUserTokens(String serviceKey, boolean refreshCache, boolean sortByDescDate) {
		String username = getUsernameByServiceKey(serviceKey);
		if (username == null) {
			return Collections.emptyList();
		}

		UserCredential userCredential = userServices.getUser(username);
		if (userCredential == null) {
			return Collections.emptyList();
		}

		if (refreshCache) {
			recordServices.refresh(userCredential);
		}

		if (sortByDescDate) {
			Map<String, LocalDateTime> sortedTokens = MapUtils.sortByReverseValue(userCredential.getAccessTokens());
			return new ArrayList<>(sortedTokens.keySet());
		}
		return new ArrayList<>(userCredential.getAccessTokens().keySet());
	}

	public Map<String, LocalDateTime> getUserAccessTokens(String serviceKey) {
		String username = getUsernameByServiceKey(serviceKey);
		if (username == null) {
			return Collections.emptyMap();
		}

		UserCredential userCredential = userServices.getUser(username);
		if (userCredential == null) {
			return Collections.emptyMap();
		}

		return userCredential.getAccessTokens();
	}

	public boolean isUserAuthenticated(String token, String serviceKey) {
		if (getUserTokens(serviceKey).contains(token)) {
			return true;
		}

		return getUserTokens(serviceKey, true, false).contains(token);
	}

	public boolean userHasDeleteAccessOnHierarchy(User user, Record record) {
		List<Record> recordsHierarchy = recordDeleteServices.loadRecordsHierarchyOf(record);
		recordsHierarchy.remove(record);
		return authorizationServices.hasDeletePermissionOnHierarchy(user, record, recordsHierarchy);
	}
}

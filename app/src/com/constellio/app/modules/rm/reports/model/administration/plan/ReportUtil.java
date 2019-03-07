package com.constellio.app.modules.rm.reports.model.administration.plan;

import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.security.roles.RolesManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class ReportUtil {
	public static List<String> getAccess(Authorization authorization, ModelLayerFactory modelLayerFactory) {
		List<String> access = new ArrayList<>();
		for (String roleCode : authorization.getRoles()) {
			RolesManager rolesManager = modelLayerFactory.getRolesManager();
			Role role = rolesManager.getRole(authorization.getCollection(), roleCode);
			if (role.isContentPermissionRole()) {
				access.add(roleCode);
			}
		}

		return access;
	}

	public static String stringListToString(List<String> stringList, String separator) {
		StringBuilder stringBuilder = new StringBuilder();
		for (String item : stringList) {

			if (stringBuilder.length() != 0) {
				stringBuilder.append(separator + " ");
			}

			stringBuilder.append(item);
		}

		return stringBuilder.toString();
	}

	public static void addItemToMapList(Map map, String key, Object item) {
		List list = (List) map.get(key);

		if(list == null) {
			list = new ArrayList<>();
			map.put(key,list);
		}

		list.add(item);
	}

	public static String accessAbreviation(List<String> roles) {
		List<String> shortened = new ArrayList<>(3);


		if (roles.contains(Role.READ)) {
			shortened.add($("AuthorizationsView.short.READ"));
		}
		if (roles.contains(Role.WRITE)) {
			shortened.add($("AuthorizationsView.short.WRITE"));
		}
		if (roles.contains(Role.DELETE)) {
			shortened.add($("AuthorizationsView.short.DELETE"));
		}

		if(shortened == null || roles.isEmpty()) {
			return "";
		}

		return stringListToString(shortened, "/");
	}
}

package com.constellio.app.modules.rm.services;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.UserFunction;
import com.constellio.model.entities.records.wrappers.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.app.modules.rm.services.UserFunctionChecker.ParentAdministrativeUnitFunctionsInclusion.INCLUDE_PARENT_USER_FUNCTIONS;
import static com.constellio.app.modules.rm.services.UserFunctionChecker.ParentAdministrativeUnitFunctionsInclusion.ONLY_INCLUDE_PARENT_USER_FUNCTIONS_IF_NO_RESULTS;

public class UserFunctionChecker {

	public enum ParentAdministrativeUnitFunctionsInclusion {

		EXCLUDE_PARENT_USER_FUNCTIONS,
		INCLUDE_PARENT_USER_FUNCTIONS,
		ONLY_INCLUDE_PARENT_USER_FUNCTIONS_IF_NO_RESULTS,
	}

	RMSchemasRecordsServices rm;

	UserFunction userFunction;

	boolean all;

	public UserFunctionChecker(RMSchemasRecordsServices rm, UserFunction userFunction) {
		this.rm = rm;
		this.userFunction = userFunction;
	}

	public List<User> onSomething() {
		throw new UnsupportedOperationException("Not supported yet");
	}

	public List<User> onAdministrativeUnit(AdministrativeUnit administrativeUnit) {
		return onAdministrativeUnit(administrativeUnit, ONLY_INCLUDE_PARENT_USER_FUNCTIONS_IF_NO_RESULTS);
	}

	public List<User> onAdministrativeUnit(AdministrativeUnit administrativeUnit,
										   ParentAdministrativeUnitFunctionsInclusion inclusion) {

		List<String> foundUsers = new ArrayList<>(findUserIds(administrativeUnit, inclusion));

		Collections.sort(foundUsers);

		List<User> users = new ArrayList<>();

		for (String foundUser : foundUsers) {
			users.add(rm.getUser(foundUser));
		}

		return users;

	}

	private Set<String> findUserIds(AdministrativeUnit administrativeUnit,
									ParentAdministrativeUnitFunctionsInclusion inclusion) {

		Set<String> foundUsers = new HashSet<>();

		foundUsers.addAll(administrativeUnit.getUsersWithFunction(userFunction));

		if ((foundUsers.isEmpty() && inclusion == ONLY_INCLUDE_PARENT_USER_FUNCTIONS_IF_NO_RESULTS)
			|| inclusion == INCLUDE_PARENT_USER_FUNCTIONS) {

			if (administrativeUnit.getParent() != null) {
				AdministrativeUnit parent = rm.getAdministrativeUnit(administrativeUnit.getParent());
				foundUsers.addAll(findUserIds(parent, inclusion));
			}

		}

		return foundUsers;
	}


}

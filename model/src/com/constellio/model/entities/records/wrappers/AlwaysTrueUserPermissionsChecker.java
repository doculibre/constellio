package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;

public class AlwaysTrueUserPermissionsChecker extends UserPermissionsChecker {
	protected AlwaysTrueUserPermissionsChecker(User user) {
		super(user);
	}

	@Override
	public boolean globally() {
		return true;
	}

	@Override
	public boolean on(Record record) {
		return true;
	}

	@Override
	public boolean onSomething() {
		return true;
	}
}

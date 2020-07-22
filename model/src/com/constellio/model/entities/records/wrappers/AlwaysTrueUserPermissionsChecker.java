package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.security.SecurityModelAuthorization;

import java.util.function.Predicate;

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
	public boolean specificallyOn(Record record) {
		return true;
	}

	@Override
	public boolean onSomething() {
		return true;
	}

	@Override
	public boolean onAnyRecord(Predicate<SecurityModelAuthorization> predicate, boolean includingGlobal) {
		return true;
	}


}

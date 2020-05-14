package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.security.SecurityModelAuthorization;

import java.util.function.Predicate;

public abstract class UserPermissionsChecker {

	protected User user;

	protected UserPermissionsChecker(User user) {
		this.user = user;
	}

	public abstract boolean globally();

	public abstract boolean on(Record record);

	public abstract boolean specificallyOn(Record record);

	public abstract boolean onSomething();

	public boolean onAnyTaxonomyConcept(boolean includingGlobal) {

		if (user == null) {
			return false;
		}

		Taxonomy taxonomy = user.getRolesDetails().getSchemasRecordsServices()
				.getModelLayerFactory().getTaxonomiesManager().getPrincipalTaxonomy(user.getCollection());

		if (taxonomy == null) {
			return false;
		}

		return onAnyRecord((a) -> taxonomy.getSchemaTypes().contains(a.getDetails().getTargetSchemaType()), includingGlobal);
	}

	public abstract boolean onAnyRecord(Predicate<SecurityModelAuthorization> predicate, boolean includingGlobal);

	public boolean specificallyOn(RecordWrapper recordWrapper) {
		return specificallyOn(recordWrapper.getWrappedRecord());
	}

	public boolean on(RecordWrapper recordWrapper) {
		return on(recordWrapper.getWrappedRecord());
	}

	public boolean onAll(RecordWrapper... recordWrappers) {
		Record[] records = new Record[recordWrappers.length];
		for (int i = 0; i < recordWrappers.length; i++) {
			records[i] = recordWrappers[i].getWrappedRecord();
		}
		return onAll(records);
	}

	public boolean onAny(RecordWrapper... recordWrappers) {
		Record[] records = new Record[recordWrappers.length];
		for (int i = 0; i < recordWrappers.length; i++) {
			records[i] = recordWrappers[i].getWrappedRecord();
		}
		return onAny(records);
	}

	public boolean onAll(Record... records) {
		if (user.isSystemAdmin()) {
			return true;
		}
		for (Record record : records) {
			if (!on(record)) {
				return false;
			}
		}
		return true;
	}

	public boolean onAny(Record... records) {
		if (user.isSystemAdmin()) {
			return true;
		}
		for (Record record : records) {
			if (on(record)) {
				return true;
			}
		}
		return false;
	}

}

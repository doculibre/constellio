/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;

public abstract class UserPermissionsChecker {

	protected User user;

	protected UserPermissionsChecker(User user) {
		this.user = user;
	}

	public abstract boolean globally();

	public abstract boolean on(Record record);

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

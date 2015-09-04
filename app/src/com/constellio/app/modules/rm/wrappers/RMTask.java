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
package com.constellio.app.modules.rm.wrappers;

import java.util.List;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class RMTask extends Task {

	public static final String ADMINISTRATIVE_UNIT = "administrativeUnit";
	public static final String LINKED_FOLDERS = "linkedFolders";
	public static final String LINKED_DOCUMENTS = "linkedDocuments";

	public RMTask(Record record, MetadataSchemaTypes types) {
		super(record, types);
	}

	public RMTask(Task task) {
		super(task.getWrappedRecord(), task.getMetadataSchemaTypes());
	}

	public String getAdministrativeUnit() {
		return get(ADMINISTRATIVE_UNIT);
	}

	public RMTask getAdministrativeUnit(String administrativeUnitId) {
		set(ADMINISTRATIVE_UNIT, administrativeUnitId);
		return this;
	}

	public RMTask getAdministrativeUnit(Record administrativeUnit) {
		set(ADMINISTRATIVE_UNIT, administrativeUnit);
		return this;
	}

	public RMTask getAdministrativeUnit(AdministrativeUnit administrativeUnit) {
		set(ADMINISTRATIVE_UNIT, administrativeUnit);
		return this;
	}

	public List<String> getLinkedFolders() {
		return getList(LINKED_FOLDERS);
	}

	public RMTask setLinkedFolders(List<?> linkedFolders) {
		set(LINKED_FOLDERS, linkedFolders);
		return this;
	}

	public List<String> getLinkedDocuments() {
		return getList(LINKED_DOCUMENTS);
	}

	public RMTask setLinkedDocuments(List<?> linkedDocuments) {
		set(LINKED_DOCUMENTS, linkedDocuments);
		return this;
	}
}

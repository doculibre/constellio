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
package com.constellio.app.api.cmis.builders.object;

import java.util.EnumSet;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AllowableActionsImpl;

import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.model.entities.records.Record;

public class AllowableActionsBuilder {

	private final Record record;

	private final ConstellioCollectionRepository repository;

	public AllowableActionsBuilder(ConstellioCollectionRepository repository, Record record) {
		this.record = record;
		this.repository = repository;
	}

	public AllowableActions build() {
		boolean userReadOnly = false;
		if (record == null) {
			throw new IllegalArgumentException("File must not be null!");
		}
		boolean isFolder = true;
		//		boolean isRoot = repository.getRoot().equals(file);

		Set<Action> aas = EnumSet.noneOf(Action.class);

		addAction(aas, Action.CAN_GET_OBJECT_PARENTS, true);
		addAction(aas, Action.CAN_GET_PROPERTIES, true);
		addAction(aas, Action.CAN_UPDATE_PROPERTIES, !userReadOnly);
		addAction(aas, Action.CAN_MOVE_OBJECT, !userReadOnly);
		addAction(aas, Action.CAN_DELETE_OBJECT, !userReadOnly);
		addAction(aas, Action.CAN_GET_ACL, true);

		//if (isFolder) {
		addAction(aas, Action.CAN_GET_DESCENDANTS, true);
		addAction(aas, Action.CAN_GET_CHILDREN, true);
		addAction(aas, Action.CAN_GET_FOLDER_PARENT, true);
		addAction(aas, Action.CAN_GET_FOLDER_TREE, true);
		addAction(aas, Action.CAN_CREATE_DOCUMENT, !userReadOnly);
		addAction(aas, Action.CAN_CREATE_FOLDER, !userReadOnly);
		addAction(aas, Action.CAN_DELETE_TREE, !userReadOnly);
		//} else {
		addAction(aas, Action.CAN_GET_CONTENT_STREAM, true);
		addAction(aas, Action.CAN_SET_CONTENT_STREAM, true);
		addAction(aas, Action.CAN_DELETE_CONTENT_STREAM, true);
		addAction(aas, Action.CAN_GET_ALL_VERSIONS, true);
		//}

		AllowableActionsImpl result = new AllowableActionsImpl();
		result.setAllowableActions(aas);

		return result;
	}

	private void addAction(Set<Action> aas, Action action, boolean condition) {
		if (condition) {
			aas.add(action);
		}
	}
}

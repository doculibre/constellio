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
		boolean isRoot = record.getSchemaCode().startsWith("collection_");

		Set<Action> aas = EnumSet.noneOf(Action.class);
		if (isRoot) {
			addAction(aas, Action.CAN_GET_PROPERTIES, true);
			addAction(aas, Action.CAN_GET_CHILDREN, true);
		} else {

			addAction(aas, Action.CAN_GET_OBJECT_PARENTS, true);
			addAction(aas, Action.CAN_GET_PROPERTIES, true);
			addAction(aas, Action.CAN_UPDATE_PROPERTIES, !userReadOnly);
			addAction(aas, Action.CAN_MOVE_OBJECT, !userReadOnly);
			addAction(aas, Action.CAN_DELETE_OBJECT, !userReadOnly);
			addAction(aas, Action.CAN_GET_ACL, true);
			addAction(aas, Action.CAN_APPLY_ACL, true);

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
		}
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

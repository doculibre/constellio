package com.constellio.app.api.cmis.builders.object;

import java.util.HashSet;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AllowableActionsImpl;

import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.SchemaUtils;

public class AllowableActionsBuilder {

	private final User user;

	private final Record record;

	private final ConstellioCollectionRepository repository;

	private final AppLayerFactory appLayerFactory;

	public AllowableActionsBuilder(AppLayerFactory appLayerFactory, ConstellioCollectionRepository repository,
			Record record) {
		this(appLayerFactory, repository, record, null);
	}

	public AllowableActionsBuilder(AppLayerFactory appLayerFactory, ConstellioCollectionRepository repository,
			Record record, User user) {
		this.user = user;
		this.record = record;
		this.repository = repository;
		this.appLayerFactory = appLayerFactory;
	}

	public AllowableActions build() {

		MetadataSchemaType type = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(repository.getCollection())
				.getSchemaType(new SchemaUtils().getSchemaTypeCode(record.getSchemaCode()));

		boolean readAccess = user.hasReadAccess().on(record);
		boolean writeAccess = user.hasWriteAccess().on(record);
		boolean deleteAccess = user.hasDeleteAccess().on(record);

		if (record == null) {
			throw new IllegalArgumentException("File must not be null!");
		}
		boolean isRoot = record.getSchemaCode().startsWith("collection_");
		Set<Action> availableActions = new HashSet<>();

		if (isRoot) {
			availableActions.add(Action.CAN_GET_PROPERTIES);
			availableActions.add(Action.CAN_GET_CHILDREN);
		} else {
			availableActions.add(Action.CAN_GET_OBJECT_PARENTS);

			if (readAccess) {
				availableActions.add(Action.CAN_GET_PROPERTIES);
			}

			//			if (userReadOnly) {
			//				availableActions.add(Action.CAN_UPDATE_PROPERTIES);
			//				availableActions.add(Action.CAN_MOVE_OBJECT);
			//				availableActions.add(Action.CAN_DELETE_OBJECT);
			//			}

			if (type.hasSecurity()) {
				availableActions.add(Action.CAN_GET_ACL);
				availableActions.add(Action.CAN_APPLY_ACL);
			}

			availableActions.add(Action.CAN_GET_CHILDREN);
			availableActions.add(Action.CAN_GET_FOLDER_PARENT);
			availableActions.add(Action.CAN_GET_FOLDER_TREE);

			if (writeAccess) {
				availableActions.add(Action.CAN_CREATE_FOLDER);
			}

			if (deleteAccess) {
				availableActions.add(Action.CAN_DELETE_TREE);
			}

			if (!type.getAllMetadatas().onlyWithType(MetadataValueType.CONTENT).isEmpty()) {
				if (writeAccess) {
					availableActions.add(Action.CAN_CREATE_DOCUMENT);
					availableActions.add(Action.CAN_SET_CONTENT_STREAM);
					availableActions.add(Action.CAN_DELETE_CONTENT_STREAM);
				}
				if (readAccess) {
					availableActions.add(Action.CAN_GET_CONTENT_STREAM);
					availableActions.add(Action.CAN_GET_ALL_VERSIONS);
				}
			}

		}
		AllowableActionsImpl result = new AllowableActionsImpl();
		result.setAllowableActions(availableActions);

		return result;
	}

	private void addAction(Set<Action> aas, Action action, boolean condition) {
		if (condition) {
			aas.add(action);
		}
	}
}

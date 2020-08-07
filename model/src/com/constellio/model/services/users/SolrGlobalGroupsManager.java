package com.constellio.model.services.users;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.GroupAddUpdateRequest;
import com.constellio.model.entities.security.global.SystemWideGroup;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.GlobalGroupsManagerRuntimeException.GlobalGroupsManagerRuntimeException_InvalidParent;
import com.constellio.model.services.users.GlobalGroupsManagerRuntimeException.GlobalGroupsManagerRuntimeException_ParentNotFound;
import com.constellio.model.services.users.GlobalGroupsManagerRuntimeException.GlobalGroupsManagerRuntimeException_RecordException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.constellio.data.dao.dto.records.OptimisticLockingResolution.EXCEPTION;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class SolrGlobalGroupsManager implements StatefulService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SolrGlobalGroupsManager.class);

	private final ModelLayerFactory modelLayerFactory;
	private final SearchServices searchServices;
	private final SchemasRecordsServices schemas;

	public SolrGlobalGroupsManager(final ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		searchServices = modelLayerFactory.newSearchServices();
		schemas = SchemasRecordsServices.usingMainModelLayerFactory(Collection.SYSTEM_COLLECTION, modelLayerFactory);
	}

	GroupAddUpdateRequest create(String code, String name, List<String> collections, String parent,
								 GlobalGroupStatus status,
								 boolean locallyCreated) {

		return addUpdateRequest(code)
				.setName(name)
				.addCollections(collections)
				.setParent(parent)
				.setStatusInAllCollections(status)
				.setLocallyCreated(locallyCreated);
	}

	GroupAddUpdateRequest create(String code, String parent, GlobalGroupStatus status, boolean locallyCreated) {
		return create(code, code, Collections.<String>emptyList(), parent, status, locallyCreated);
	}

	//TODO improve!
	void addUpdate(List<GroupAddUpdateRequest> requests) {
		requests.forEach(this::addUpdate);
	}

	void addUpdate(GroupAddUpdateRequest request) {


		Record record = modelLayerFactory.newRecordServices()
				.getRecordByMetadata(schemas.globalGroupCode(), request.getCode());
		if (record == null) {
			record = modelLayerFactory.newRecordServices().newRecordWithSchema(schemas.globalGroupSchema());
			record.set(Schemas.CODE, request.getCode());
		}

		GlobalGroup group = schemas.wrapOldGlobalGroup(record);

		for (Map.Entry<String, Object> entry : request.getModifiedAttributes().entrySet()) {
			group.set(entry.getKey(), entry.getValue());
		}

		validateHierarchy(group);

		if (request.getRemovedCollections() != null || request.getNewCollections() != null) {
			List<String> collections = new ArrayList<>(group.getUsersAutomaticallyAddedToCollections());

			if (request.getNewCollections() != null) {
				for (String newCollection : request.getNewCollections()) {
					if (!collections.contains(newCollection)) {
						collections.add(newCollection);
					}
				}
			}
			if (request.getRemovedCollections() != null) {
				for (String removedCollection : request.getRemovedCollections()) {
					collections.remove(removedCollection);
				}
			}

			group.setUsersAutomaticallyAddedToCollections(collections);
		}

		try {
			modelLayerFactory.newRecordServices().add(group);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	void logicallyRemoveGroup(SystemWideGroup group) {
		List<GroupAddUpdateRequest> requests = new ArrayList<>();
		for (SystemWideGroup each : getGroupHierarchy(group)) {
			requests.add(request(each.getCode()).setStatusInAllCollections(GlobalGroupStatus.INACTIVE)
					.setLogicallyDeletedStatus(true));
		}

		addUpdate(requests);
	}

	GroupAddUpdateRequest request(String code) {
		return modelLayerFactory.newUserServices().request(code);
	}

	GroupAddUpdateRequest addUpdateRequest(String code) {
		return schemas.newGlobalGroup(code);
	}


	void activateGlobalGroupHierarchy(SystemWideGroup group) {
		List<GroupAddUpdateRequest> requests = new ArrayList<>();
		for (SystemWideGroup each : getGroupHierarchy(group)) {
			GroupAddUpdateRequest request = request(each.getCode());
			requests.add(request.setStatusInAllCollections(GlobalGroupStatus.ACTIVE)
					.setLogicallyDeletedStatus(null));
		}
		addUpdate(requests);
	}

	SystemWideGroup getGlobalGroupWithCode(String code) {
		Record record = modelLayerFactory.newRecordServices()
				.getRecordByMetadata(schemas.globalGroupCode(), code);
		return record != null ? wrapGlobalGroup(record) : null;
	}


	LogicalSearchQuery getAllGroupsQuery() {
		return new LogicalSearchQuery(from(schemas.globalGroupSchemaType()).returnAll()).sortAsc(schemas.globalGroupCode());
	}

	List<SystemWideGroup> getAllGroups() {
		return wrapGlobalGroups(searchServices.search(getAllGroupsQuery()));
	}

	public List<SystemWideGroup> wrapGlobalGroups(List<Record> records) {
		List<SystemWideGroup> result = new ArrayList<>(records.size());
		for (Record record : records) {
			result.add(wrapGlobalGroup(record));
		}
		return result;
	}


	public SystemWideGroup wrapGlobalGroup(Record record) {
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(Collection.SYSTEM_COLLECTION);
		GlobalGroup globalGroup = new GlobalGroup(record, types);
		return SystemWideGroup.builder()
				.code(globalGroup.getCode())
				.name(globalGroup.getCode())
				.collections(globalGroup.getUsersAutomaticallyAddedToCollections())
				.parent(globalGroup.getParent())
				.groupStatus(globalGroup.getStatus())
				.hierarchy(globalGroup.getHierarchy())
				.toString(globalGroup.toString())
				.logicallyDeletedStatus(globalGroup.getLogicallyDeletedStatus())
				.build();
	}

	List<SystemWideGroup> getHierarchy(String code) {
		SystemWideGroup group = (SystemWideGroup) getGlobalGroupWithCode(code);
		return group != null ? getGroupHierarchy(group) : Collections.<SystemWideGroup>emptyList();
	}

	LogicalSearchQuery getActiveGroupsQuery() {
		return new LogicalSearchQuery(
				from(schemas.globalGroupSchemaType()).where(schemas.globalGroupStatus()).isEqualTo(GlobalGroupStatus.ACTIVE))
				.sortAsc(schemas.globalGroupCode());
	}

	List<SystemWideGroup> getActiveGroups() {
		return wrapGlobalGroups(searchServices.search(getActiveGroupsQuery()));
	}

	LogicalSearchQuery getGroupsInCollectionQuery(String collection) {
		return new LogicalSearchQuery(
				from(schemas.globalGroupSchemaType()).where(schemas.globalGroupCollections()).isEqualTo(collection))
				.sortAsc(schemas.globalGroupCode());
	}

	void removeCollection(final String collection) {
		try {
			new ActionExecutorInBatch(searchServices, "Remove collection in global groups", 100) {

				@Override
				public void doActionOnBatch(List<Record> records)
						throws Exception {
					List<GroupAddUpdateRequest> requests = new ArrayList<>();

					Transaction transaction = new Transaction();
					transaction.getRecordUpdateOptions().setOptimisticLockingResolution(EXCEPTION);
					for (Record record : records) {
						GlobalGroup globalGroup = schemas.wrapOldGlobalGroup(record);
						requests.add(request(globalGroup.getCode()).removeCollection(collection));
					}

					modelLayerFactory.newRecordServices().execute(transaction);

				}
			}.execute(getGroupsInCollectionQuery(collection));
		} catch (Exception e) {
			throw new GlobalGroupsManagerRuntimeException_RecordException(e);
		}
	}

	@Override
	public void initialize() {
		// Nothing to be done
	}

	@Override
	public void close() {
		// Nothing to be done
	}

	private List<SystemWideGroup> getGroupHierarchy(SystemWideGroup group) {
		List<SystemWideGroup> result = new ArrayList<>();
		for (Record record : searchServices.search(getGroupHierarchyQuery(group))) {
			result.add(wrapGlobalGroup(record));
		}
		return result;
	}

	private LogicalSearchQuery getGroupHierarchyQuery(SystemWideGroup group) {
		LogicalSearchCondition condition = from(schemas.globalGroupSchemaType())
				.where(schemas.globalGroupCode()).isEqualTo(group.getCode())
				.orWhere(schemas.globalGroupHierarchy()).isStartingWithText(group.getHierarchy() + "/");
		return new LogicalSearchQuery(condition).sortAsc(schemas.globalGroupHierarchy());
	}

	private void validateHierarchy(GlobalGroup group) {

		if (group.getParent() == null) {
			group.setHierarchy(group.getCode());
			return;
		}
		SystemWideGroup parent = (SystemWideGroup) getGlobalGroupWithCode(group.getParent());
		if (parent == null) {
			throw new GlobalGroupsManagerRuntimeException_ParentNotFound();
		}
		if (parent.getCode().equals(group.getCode()) || parent.getHierarchy().startsWith(group.getHierarchy() + "/")) {
			throw new GlobalGroupsManagerRuntimeException_InvalidParent(group.getParent());
		}
		group.setHierarchy(parent.getHierarchy() + "/" + group.getCode());
	}

}

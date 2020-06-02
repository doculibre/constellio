package com.constellio.model.services.users;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
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

import static com.constellio.data.dao.dto.records.OptimisticLockingResolution.EXCEPTION;
import static com.constellio.data.utils.LangUtils.valueOrDefault;
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

	public GlobalGroup create(String code, String name, List<String> collections, String parent,
							  GlobalGroupStatus status,
							  boolean locallyCreated) {
		return ((GlobalGroup) valueOrDefault(getGlobalGroupWithCode(code), schemas.newGlobalGroup()))
				.setCode(code)
				.setName(name)
				.setUsersAutomaticallyAddedToCollections(collections)
				.setParent(parent)
				.setStatus(status)
				.setLocallyCreated(locallyCreated);
	}

	public GlobalGroup create(String code, String parent, GlobalGroupStatus status, boolean locallyCreated) {
		return create(code, code, Collections.<String>emptyList(), parent, status, locallyCreated);
	}

	public void addUpdate(GlobalGroup group) {
		GlobalGroup groupRecord = (GlobalGroup) group;
		validateHierarchy(groupRecord);
		try {
			modelLayerFactory.newRecordServices().add(groupRecord);
		} catch (RecordServicesException e) {
			String groupInfo = group != null ? group.toString() : "Null group";
			LOGGER.warn("Error while trying to save global group: " + groupInfo);
		}
	}

	public void logicallyRemoveGroup(GlobalGroup group) {
		Transaction transaction = new Transaction();
		for (GlobalGroup each : getGroupHierarchy((GlobalGroup) group)) {
			RecordWrapper recordWrapper = ((GlobalGroup) each).setStatus(GlobalGroupStatus.INACTIVE)
					.set(Schemas.LOGICALLY_DELETED_STATUS, true);
			transaction.add(recordWrapper);
		}
		try {
			modelLayerFactory.newRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			String groupInfo = group != null ? group.toString() : "Null group";
			LOGGER.warn("Error while trying to remove global group: " + groupInfo);
		}
	}

	public void activateGlobalGroupHierarchy(GlobalGroup group) {
		Transaction transaction = new Transaction();
		for (GlobalGroup each : getGroupHierarchy((GlobalGroup) group)) {
			RecordWrapper record = ((GlobalGroup) each).setStatus(GlobalGroupStatus.ACTIVE)
					.set(Schemas.LOGICALLY_DELETED_STATUS, null)
					.set(Schemas.LOGICALLY_DELETED_ON, null);
			transaction.add(record);
		}
		try {
			modelLayerFactory.newRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			String groupInfo = group != null ? group.toString() : "Null group";
			LOGGER.warn("Error while trying to activate global group hierarchy: " + groupInfo);
		}
	}

	public GlobalGroup getGlobalGroupWithCode(String code) {
		Record record = modelLayerFactory.newRecordServices()
				.getRecordByMetadata(schemas.globalGroupCode(), code);
		return record != null ? schemas.wrapGlobalGroup(record) : null;
	}

	public GlobalGroup getActiveGlobalGroupWithCode(String code) {
		GlobalGroup group = getGlobalGroupWithCode(code);
		return group != null && group.getStatus() == GlobalGroupStatus.ACTIVE ? group : null;
	}

	public LogicalSearchQuery getAllGroupsQuery() {
		return new LogicalSearchQuery(from(schemas.globalGroupSchemaType()).returnAll()).sortAsc(schemas.globalGroupCode());
	}

	public List<GlobalGroup> getAllGroups() {
		return schemas.wrapGlobalGroups(searchServices.search(getAllGroupsQuery()));
	}

	public LogicalSearchQuery getDeletedGroupsQuery() {
		return getAllGroupsQuery().filteredByStatus(StatusFilter.DELETED);
	}

	public LogicalSearchCondition getDeletedGroupsCondition() {
		return from(schemas.globalGroupSchemaType()).where(Schemas.LOGICALLY_DELETED_STATUS).isTrue();
	}

	public List<GlobalGroup> getHierarchy(String code) {
		GlobalGroup group = (GlobalGroup) getGlobalGroupWithCode(code);
		return group != null ? getGroupHierarchy(group) : Collections.<GlobalGroup>emptyList();
	}

	public LogicalSearchQuery getActiveGroupsQuery() {
		return new LogicalSearchQuery(
				from(schemas.globalGroupSchemaType()).where(schemas.globalGroupStatus()).isEqualTo(GlobalGroupStatus.ACTIVE))
				.sortAsc(schemas.globalGroupCode());
	}

	public List<GlobalGroup> getActiveGroups() {
		return schemas.wrapGlobalGroups(searchServices.search(getActiveGroupsQuery()));
	}

	public LogicalSearchQuery getGroupsInCollectionQuery(String collection) {
		return new LogicalSearchQuery(
				from(schemas.globalGroupSchemaType()).where(schemas.globalGroupCollections()).isEqualTo(collection))
				.sortAsc(schemas.globalGroupCode());
	}

	public void removeCollection(final String collection) {
		try {
			new ActionExecutorInBatch(searchServices, "Remove collection in global groups", 100) {

				@Override
				public void doActionOnBatch(List<Record> records)
						throws Exception {
					Transaction transaction = new Transaction();
					transaction.getRecordUpdateOptions().setOptimisticLockingResolution(EXCEPTION);
					for (Record record : records) {
						transaction.add((GlobalGroup) schemas.wrapGlobalGroup(record)).removeCollection(collection);
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

	private List<GlobalGroup> getGroupHierarchy(GlobalGroup group) {
		List<GlobalGroup> result = new ArrayList<>();
		for (Record record : searchServices.search(getGroupHierarchyQuery(group))) {
			result.add(schemas.wrapGlobalGroup(record));
		}
		return result;
	}

	private LogicalSearchQuery getGroupHierarchyQuery(GlobalGroup group) {
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
		GlobalGroup parent = (GlobalGroup) getGlobalGroupWithCode(group.getParent());
		if (parent == null) {
			throw new GlobalGroupsManagerRuntimeException_ParentNotFound();
		}
		if (parent.getCode().equals(group.getCode()) || parent.getHierarchy().startsWith(group.getHierarchy() + "/")) {
			throw new GlobalGroupsManagerRuntimeException_InvalidParent(group.getParent());
		}
		group.setHierarchy(parent.getHierarchy() + "/" + group.getCode());
	}

}

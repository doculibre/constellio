package com.constellio.model.services.users;

import static com.constellio.data.utils.LangUtils.valueOrDefault;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.SolrGlobalGroup;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.factories.SystemCollectionListener;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.GlobalGroupsManagerRuntimeException.GlobalGroupsManagerRuntimeException_InvalidParent;
import com.constellio.model.services.users.GlobalGroupsManagerRuntimeException.GlobalGroupsManagerRuntimeException_ParentNotFound;

public class SolrGlobalGroupsManager implements GlobalGroupsManager, SystemCollectionListener {
	private final ModelLayerFactory modelLayerFactory;
	private final SearchServices searchServices;
	private final SchemasRecordsServices schemas;

	public SolrGlobalGroupsManager(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		modelLayerFactory.addSystemCollectionListener(this);
		searchServices = modelLayerFactory.newSearchServices();
		schemas = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, modelLayerFactory);
	}

	@Override
	public GlobalGroup create(String code, String name, List<String> collections, String parent, GlobalGroupStatus status) {
		return ((SolrGlobalGroup) valueOrDefault(getGlobalGroupWithCode(code), schemas.newGlobalGroup()))
				.setCode(code)
				.setName(name)
				.setUsersAutomaticallyAddedToCollections(collections)
				.setParent(parent)
				.setStatus(status);
	}

	@Override
	public GlobalGroup create(String code, String parent, GlobalGroupStatus status) {
		return create(code, code, Collections.<String>emptyList(), parent, status);
	}

	@Override
	public void addUpdate(GlobalGroup group) {
		SolrGlobalGroup groupRecord = (SolrGlobalGroup) group;
		validateHierarchy(groupRecord);
		try {
			modelLayerFactory.newRecordServices().add(groupRecord);
		} catch (RecordServicesException e) {
			// TODO: Exception
			e.printStackTrace();
		}
	}

	@Override
	public void logicallyRemoveGroup(GlobalGroup group) {
		Transaction transaction = new Transaction();
		for (GlobalGroup each : getGroupHierarchy((SolrGlobalGroup) group)) {
			transaction.add(((SolrGlobalGroup) each).setStatus(GlobalGroupStatus.INACTIVE));
		}
		try {
			modelLayerFactory.newRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			// TODO: Exception
			e.printStackTrace();
		}
	}

	@Override
	public void activateGlobalGroupHierarchy(GlobalGroup group) {
		Transaction transaction = new Transaction();
		for (GlobalGroup each : getGroupHierarchy((SolrGlobalGroup) group)) {
			transaction.add(((SolrGlobalGroup) each).setStatus(GlobalGroupStatus.ACTIVE));
		}
		try {
			modelLayerFactory.newRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			// TODO: Exception
			e.printStackTrace();
		}
	}

	@Override
	public GlobalGroup getGlobalGroupWithCode(String code) {
		Record group = searchServices.searchSingleResult(
				from(schemas.globalGroupSchemaType()).where(schemas.globalGroupCode()).isEqualTo(code));
		return group != null ? schemas.wrapGlobalGroup(group) : null;
	}

	@Override
	public GlobalGroup getActiveGlobalGroupWithCode(String code) {
		GlobalGroup group = getGlobalGroupWithCode(code);
		return group != null && group.getStatus() == GlobalGroupStatus.ACTIVE ? group : null;
	}

	public LogicalSearchQuery getAllGroupsQuery() {
		return new LogicalSearchQuery(from(schemas.globalGroupSchemaType()).returnAll()).sortAsc(schemas.globalGroupCode());
	}

	@Override
	public List<GlobalGroup> getAllGroups() {
		return schemas.wrapGlobalGroups(searchServices.search(getAllGroupsQuery()));
	}

	@Override
	public List<GlobalGroup> getHierarchy(String code) {
		SolrGlobalGroup group = (SolrGlobalGroup) getGlobalGroupWithCode(code);
		return group != null ? getGroupHierarchy(group) : Collections.<GlobalGroup>emptyList();
	}

	public LogicalSearchQuery getActiveGroupsQuery() {
		return new LogicalSearchQuery(
				from(schemas.globalGroupSchemaType()).where(schemas.globalGroupStatus()).isEqualTo(GlobalGroupStatus.ACTIVE))
				.sortAsc(schemas.globalGroupCode());
	}

	@Override
	public List<GlobalGroup> getActiveGroups() {
		return schemas.wrapGlobalGroups(searchServices.search(getActiveGroupsQuery()));
	}

	public LogicalSearchQuery getGroupsInCollectionQuery(String collection) {
		return new LogicalSearchQuery(
				from(schemas.globalGroupSchemaType()).where(schemas.globalGroupCollections()).isEqualTo(collection))
				.sortAsc(schemas.globalGroupCode());
	}

	@Override
	public void removeCollection(String collection) {
		Transaction transaction = new Transaction();
		for (GlobalGroup group : schemas.wrapGlobalGroups(searchServices.search(getGroupsInCollectionQuery(collection)))) {
			transaction.add((SolrGlobalGroup) group.withRemovedCollection(collection));
		}
		try {
			modelLayerFactory.newRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			// TODO: Exception
			e.printStackTrace();
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

	@Override
	public void systemCollectionCreated() {
		MetadataSchemasManager manager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaTypesBuilder builder = manager.modify(Collection.SYSTEM_COLLECTION);
		createGlobalGroupSchema(builder);
		try {
			manager.saveUpdateSchemaTypes(builder);
		} catch (OptimisticLocking e) {
			systemCollectionCreated();
		}

	}

	private List<GlobalGroup> getGroupHierarchy(SolrGlobalGroup group) {
		List<GlobalGroup> result = new ArrayList<>();
		for (Record record : searchServices.search(getGroupHierarchyQuery(group))) {
			result.add(schemas.wrapGlobalGroup(record));
		}
		return result;
	}

	private LogicalSearchQuery getGroupHierarchyQuery(SolrGlobalGroup group) {
		LogicalSearchCondition condition = from(schemas.globalGroupSchemaType())
				.where(schemas.globalGroupCode()).isEqualTo(group.getCode())
				.orWhere(schemas.globalGroupHierarchy()).isStartingWithText(group.getHierarchy() + "/");
		return new LogicalSearchQuery(condition).sortAsc(schemas.globalGroupHierarchy());
	}

	private void validateHierarchy(SolrGlobalGroup group) {
		if (group.getParent() == null) {
			group.setHierarchy(group.getCode());
			return;
		}
		SolrGlobalGroup parent = (SolrGlobalGroup) getGlobalGroupWithCode(group.getParent());
		if (parent == null) {
			throw new GlobalGroupsManagerRuntimeException_ParentNotFound();
		}
		if (parent.getCode().equals(group.getCode()) || parent.getHierarchy().startsWith(group.getHierarchy() + "/")) {
			throw new GlobalGroupsManagerRuntimeException_InvalidParent(group.getParent());
		}
		group.setHierarchy(parent.getHierarchy() + "/" + group.getCode());
	}

	private void createGlobalGroupSchema(MetadataSchemaTypesBuilder builder) {
		MetadataSchemaTypeBuilder credentialsTypeBuilder = builder.createNewSchemaType(SolrGlobalGroup.SCHEMA_TYPE);
		credentialsTypeBuilder.setSecurity(false);
		MetadataSchemaBuilder groups = credentialsTypeBuilder.getDefaultSchema();

		groups.createUniqueCodeMetadata();
		groups.createUndeletable(SolrGlobalGroup.NAME).setType(MetadataValueType.STRING).setDefaultRequirement(true);
		groups.createUndeletable(SolrGlobalGroup.COLLECTIONS).setType(MetadataValueType.STRING).setMultivalue(true);
		groups.createUndeletable(SolrGlobalGroup.PARENT).setType(MetadataValueType.STRING);
		groups.createUndeletable(SolrGlobalGroup.STATUS).defineAsEnum(GlobalGroupStatus.class).setDefaultRequirement(true);
		groups.createUndeletable(SolrGlobalGroup.HIERARCHY).setType(MetadataValueType.STRING);
	}
}

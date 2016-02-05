package com.constellio.app.modules.robots.ui.data;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.robots.services.RobotSchemaRecordServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.data.LazyTreeDataProvider;
import com.constellio.app.ui.framework.data.ObjectsResponse;
import com.constellio.app.ui.framework.data.RecordDataTreeNode;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class RobotLazyTreeDataProvider implements LazyTreeDataProvider<String> {
	private final Map<String, RecordDataTreeNode> nodesCache = new HashMap<>();
	private final Map<String, String> parentCache = new HashMap<>();

	private final String collection;
	private final String rootId;

	private transient SearchServices searchServices;
	private transient RobotSchemaRecordServices schemas;

	public RobotLazyTreeDataProvider(AppLayerFactory appLayerFactory, String collection, String rootId) {
		this.collection = collection;
		this.rootId = rootId;
		init(appLayerFactory);
	}

	@Override
	public ObjectsResponse<String> getRootObjects(int start, int maxSize) {
		return getChildren(rootId, start, maxSize, false);
	}

	@Override
	public String getParent(String childId) {
		return parentCache.get(childId);
	}

	@Override
	public ObjectsResponse<String> getChildren(String parentId, int start, int maxSize) {
		return getChildren(parentId, start, maxSize, true);
	}

	@Override
	public boolean hasChildren(String parentId) {
		return getNode(parentId).hasChildren();
	}

	@Override
	public boolean isLeaf(String nodeId) {
		return !hasChildren(nodeId);
	}

	@Override
	public String getTaxonomyCode() {
		// TODO: Hope this doesn't explode
		return null;
	}

	@Override
	public String getCaption(String nodeId) {
		return getNode(nodeId).getCaption();
	}

	@Override
	public String getDescription(String nodeId) {
		return getNode(nodeId).getDescription();
	}

	private RecordDataTreeNode getNode(String nodeId) {
		return nodesCache.get(nodeId);
	}

	private ObjectsResponse<String> getChildren(String parentId, int start, int maxSize, boolean updateParents) {
		SPEQueryResponse response = loadChildren(parentId, start, maxSize);
		List<String> recordIds = new ArrayList<>();
		for (Record record : response.getRecords()) {
			nodesCache.put(record.getId(), toTreeNode(record));
			recordIds.add(record.getId());
			if (updateParents) {
				parentCache.put(record.getId(), parentId);
			}
		}
		return new ObjectsResponse<>(recordIds, response.getNumFound());
	}

	private SPEQueryResponse loadChildren(String parentId, int start, int maxSize) {
		LogicalSearchQuery query = new LogicalSearchQuery(
				from(schemas.robot.schema()).where(schemas.robot.parent()).isEqualTo(parentId))
				.setStartRow(start).setNumberOfRows(maxSize);
		return searchServices.query(query);
	}

	private RecordDataTreeNode toTreeNode(Record record) {
		String schemaType = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		String caption = SchemaCaptionUtils.getCaptionForRecord(record);
		String description = record.get(Schemas.DESCRIPTION_STRING);
		if (description == null) {
			description = record.get(Schemas.DESCRIPTION_TEXT);
		}
		// TODO: We are faking the children field here
		return new RecordDataTreeNode(record.getId(), caption, description, schemaType, true);
	}

	private void init(AppLayerFactory appLayerFactory) {
		searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		schemas = new RobotSchemaRecordServices(collection, appLayerFactory);
	}

	private void readObject(ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init(ConstellioFactories.getInstance().getAppLayerFactory());
	}
}

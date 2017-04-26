package com.constellio.app.modules.robots.ui.data;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.robots.services.RobotSchemaRecordServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.data.trees.RecordTreeNodesDataProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.taxonomies.FastContinueInfos;
import com.constellio.model.services.taxonomies.LinkableTaxonomySearchResponse;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;

public class RobotTreeNodesDataProvider implements RecordTreeNodesDataProvider {

	String collection;
	String rootId;

	private transient SearchServices searchServices;
	private transient RobotSchemaRecordServices schemas;

	public RobotTreeNodesDataProvider(AppLayerFactory appLayerFactory, String collection, String rootId) {
		this.collection = collection;
		this.rootId = rootId;
		init(appLayerFactory);
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

	@Override
	public LinkableTaxonomySearchResponse getChildrenNodes(String parentId, int start, int maxSize, FastContinueInfos infos) {
		LogicalSearchQuery query = new LogicalSearchQuery(
				from(schemas.robot.schema()).where(schemas.robot.parent()).isEqualTo(parentId))
				.setStartRow(start).setNumberOfRows(maxSize);

		SPEQueryResponse response = searchServices.query(query);
		return LinkableTaxonomySearchResponse.asUnlinkableWithChildrenRecords(response);
	}

	@Override
	public LinkableTaxonomySearchResponse getRootNodes(int start, int maxSize, FastContinueInfos infos) {
		return getChildrenNodes(rootId, start, maxSize, null);
	}

	@Override
	public String getTaxonomyCode() {
		return null;
	}
}

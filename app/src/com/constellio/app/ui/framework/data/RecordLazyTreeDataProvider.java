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
package com.constellio.app.ui.framework.data;

import static com.constellio.app.services.factories.ConstellioFactories.getInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.taxonomies.LinkableTaxonomySearchResponse;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import com.constellio.model.services.users.UserServices;

public class RecordLazyTreeDataProvider implements LazyTreeDataProvider<String> {

	private String collection;

	private String taxonomyCode;

	private Map<String, String> parentCache = new HashMap<>();

	private Map<String, RecordDataTreeNode> nodesCache = new HashMap<>();

	public RecordLazyTreeDataProvider(String taxonomyCode) {
		super();
		this.taxonomyCode = taxonomyCode;

	}

	public final String getTaxonomyCode() {
		return taxonomyCode;
	}

	@Override
	public String getCaption(String id) {
		return getNode(id).getCaption();
	}

	@Override
	public ObjectsResponse<String> getRootObjects(int start, int maxSize) {

		ModelLayerFactory modelLayerFactory = getInstance().getModelLayerFactory();
		User currentUser = getCurrentUser(modelLayerFactory);

		TaxonomiesSearchOptions taxonomiesSearchOptions = new TaxonomiesSearchOptions(maxSize, start, StatusFilter.ACTIVES);
		LinkableTaxonomySearchResponse response = modelLayerFactory.newTaxonomiesSearchService().getVisibleRootConceptResponse(
				currentUser, currentUser.getCollection(), taxonomyCode, taxonomiesSearchOptions);

		List<String> recordIds = new ArrayList<>();
		for (TaxonomySearchRecord searchRecord : response.getRecords()) {
			RecordDataTreeNode node = toTreeNode(searchRecord);
			nodesCache.put(searchRecord.getId(), node);
			recordIds.add(searchRecord.getId());
		}
		return new ObjectsResponse<>(recordIds, response.getNumFound());
	}

	@Override
	public String getParent(String child) {
		return parentCache.get(child);
	}

	@Override
	public ObjectsResponse<String> getChildren(String parent, int start, int maxSize) {

		ModelLayerFactory modelLayerFactory = getInstance().getModelLayerFactory();
		User currentUser = getCurrentUser(modelLayerFactory);
		Record record = getRecord(modelLayerFactory, parent);

		TaxonomiesSearchOptions taxonomiesSearchOptions = new TaxonomiesSearchOptions(maxSize, start, StatusFilter.ACTIVES);
		LinkableTaxonomySearchResponse response = modelLayerFactory.newTaxonomiesSearchService()
				.getVisibleChildConceptResponse(currentUser, taxonomyCode, record, taxonomiesSearchOptions);

		List<String> recordIds = new ArrayList<>();
		for (TaxonomySearchRecord searchRecord : response.getRecords()) {
			RecordDataTreeNode node = toTreeNode(searchRecord);
			nodesCache.put(searchRecord.getId(), node);
			recordIds.add(searchRecord.getId());
			parentCache.put(searchRecord.getId(), parent);
		}
		return new ObjectsResponse<>(recordIds, response.getNumFound());
	}

	private User getCurrentUser(ModelLayerFactory modelLayerFactory) {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		String currentCollection = sessionContext.getCurrentCollection();
		UserVO currentUserVO = sessionContext.getCurrentUser();
		UserServices userServices = modelLayerFactory.newUserServices();

		return userServices.getUserInCollection(currentUserVO.getUsername(), currentCollection);
	}

	private Record getRecord(ModelLayerFactory modelLayerFactory, String id) {
		RecordServices recordServices = modelLayerFactory.newRecordServices();

		return recordServices.getDocumentById(id);
	}

	private RecordDataTreeNode toTreeNode(TaxonomySearchRecord searchRecord) {
		Record record = searchRecord.getRecord();
		String schemaType = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		String caption = SchemaCaptionUtils.getCaptionForRecord(searchRecord.getRecord());

		return new RecordDataTreeNode(searchRecord.getId(), caption, schemaType, searchRecord.hasChildren());
	}

	@Override
	public boolean hasChildren(String parentId) {
		RecordDataTreeNode parent = nodesCache.get(parentId);
		return parent.hasChildren();
	}

	@Override
	public boolean isLeaf(String parentId) {
		RecordDataTreeNode parent = nodesCache.get(parentId);
		return !parent.hasChildren();
	}

	public RecordDataTreeNode getNode(String id) {
		return nodesCache.get(id);
	}
}

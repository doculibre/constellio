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
package com.constellio.app.ui.pages.management.facet;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotPhysicallyDeleteRecord;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class ListFacetConfigurationPresenter extends BasePresenter<ListFacetConfigurationView> {

	public ListFacetConfigurationPresenter(ListFacetConfigurationView view) {
		super(view);
		init();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_FACETS).globally();
	}

	public void addButtonClicked() {
		view.navigateTo().addFacetConfiguration();
	}

	public void orderButtonClicked() {
		view.navigateTo().orderFacetConfiguration();
	}

	public void displayButtonClicked(RecordVO recordVO) {
		view.navigateTo().displayFacetConfiguration(recordVO.getId());
	}

	public void deleteButtonClicked(RecordVO recordVO) {
		Record record = recordServices().getDocumentById(recordVO.getId());
		recordServices().logicallyDelete(record, User.GOD);
		try {
			recordServices().physicallyDelete(record, User.GOD);
		} catch (RecordServicesRuntimeException_CannotPhysicallyDeleteRecord e) {
			recordServices().restore(record, User.GOD);
			throw new RuntimeException(e);
		}

		view.navigateTo().listFacetConfiguration();
	}

	public void editButtonClicked(RecordVO recordVO) {
		view.navigateTo().editFacetConfiguration(recordVO.getId());
	}

	public RecordVODataProvider getDataProvider() {
		List<String> metadatas = new ArrayList<>();
		metadatas.add(Facet.TITLE);
		metadatas.add(Facet.FACET_TYPE);
		metadatas.add(Facet.ORDER_RESULT);
		final MetadataSchemaVO facetDefaultVO = new MetadataSchemaToVOBuilder().build(schema(Facet.DEFAULT_SCHEMA),
				VIEW_MODE.TABLE, metadatas, view.getSessionContext());
		return new RecordVODataProvider(facetDefaultVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				SchemasRecordsServices schemasRecords = new SchemasRecordsServices(collection, modelLayerFactory);
				LogicalSearchQuery query = new LogicalSearchQuery();
				query.setCondition(from(schemasRecords.facetSchemaType()).returnAll())
						.sortAsc(schemasRecords.defaultFacet().get(Facet.ORDER));
				return query;
			}
		};
	}

	public Record toRecord(RecordVO recordVO) {
		SchemaPresenterUtils schemaPresenterUtils = new SchemaPresenterUtils(recordVO.getSchema().getCode(),
				view.getConstellioFactories(), view.getSessionContext());
		return schemaPresenterUtils.toRecord(recordVO);
	}

}

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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class OrderFacetConfigurationPresenter extends BasePresenter<OrderFacetConfigurationView> {
	private Map<String, Facet> facets;
	private List<String> codeTitles;

	public OrderFacetConfigurationPresenter(OrderFacetConfigurationView view) {
		super(view);
		init();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
		facets = new HashMap<>();
		SchemasRecordsServices schemasRecords = new SchemasRecordsServices(collection, modelLayerFactory);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(schemasRecords.facetSchemaType()).returnAll())
				.sortAsc(schemasRecords.defaultFacet().get(Facet.ORDER));
		List<Record> records = searchServices().search(query);

		for (Record record : records) {
			Facet facet = new Facet(record, types());
			facets.put(facet.getId(), facet);
		}
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_FACETS).globally();
	}

	public void displayButtonClicked(RecordVO recordVO) {
		view.navigateTo().displayFacetConfiguration(recordVO.getId());
	}

	public void deleteButtonClicked(RecordVO recordVO) {
		Record record = recordServices().getDocumentById(recordVO.getId());
		recordServices().logicallyDelete(record, User.GOD);
		recordServices().physicallyDelete(record, User.GOD);
		view.navigateTo().listFacetConfiguration();
	}

	public void cancelButtonClicked() {
		view.navigateTo().listFacetConfiguration();
	}

	public void swap(String value, int offset) {
		int current = codeTitles.indexOf(value);
		try {
			Collections.swap(codeTitles, current, current + offset);
		} catch (Exception e) {
			//
		}
	}

	public void saveButtonClicked() {
		Transaction transaction = new Transaction();
		for (int i = 0; i < codeTitles.size(); ++i) {
			Facet facet = facets.get(codeTitles.get(i));
			facet.setOrder(i);
			transaction.update(facet.getWrappedRecord());
		}

		try {
			recordServices().execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException("");
		}

		view.navigateTo().listFacetConfiguration();
	}

	public List<String> getFacetTitle() {
		if (codeTitles == null) {
			codeTitles = new ArrayList<>();
			for (String facetId : facets.keySet()) {
				codeTitles.add(facetId);
			}
			List<String> initialCodeTitles = new ArrayList<>();
			initialCodeTitles.addAll(codeTitles);
			Set<String> finalCodeTitles = new HashSet<>();
			for (Entry<String, Facet> stringFacetEntry : facets.entrySet()) {
				int order = ((Double) stringFacetEntry.getValue().get(Facet.ORDER)).intValue();
				String facetId = stringFacetEntry.getKey();
				codeTitles.set(order, facetId);
			}
			finalCodeTitles.addAll(codeTitles);
			if (finalCodeTitles.size() != facets.size()) {
				codeTitles = initialCodeTitles;
			}
		}
		return codeTitles;
	}

	public String getLabelForCode(String code) {
		return facets.get(code).getTitle();
	}

	public Record toRecord(RecordVO recordVO) {
		SchemaPresenterUtils schemaPresenterUtils = new SchemaPresenterUtils(recordVO.getSchema().getCode(),
				view.getConstellioFactories(), view.getSessionContext());
		return schemaPresenterUtils.toRecord(recordVO);
	}

}

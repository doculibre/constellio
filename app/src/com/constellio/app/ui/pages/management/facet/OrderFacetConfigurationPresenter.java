package com.constellio.app.ui.pages.management.facet;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.OptimisticLockException;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

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
		view.navigate().to().displayFacetConfiguration(recordVO.getId());
	}

	public void deleteButtonClicked(RecordVO recordVO) {
		Record record = recordServices().getDocumentById(recordVO.getId());
		recordServices().logicallyDelete(record, User.GOD);
		recordServices().physicallyDelete(record, User.GOD);
		view.navigate().to().listFacetConfiguration();
	}

	public void cancelButtonClicked() {
		view.navigate().to().listFacetConfiguration();
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

		view.navigate().to().listFacetConfiguration();
	}

	public List<String> getFacetTitle() {
		if (codeTitles == null) {
			List<Entry<String, Facet>> entries = new ArrayList<>(facets.entrySet());
			Collections.sort(entries, new Comparator<Entry<String, Facet>>() {
				@Override
				public int compare(Entry<String, Facet> e1, Entry<String, Facet> e2) {
					return new Integer(e1.getValue().getOrder()).compareTo(e2.getValue().getOrder());
				}
			});

			codeTitles = new ArrayList<>();
			for (Map.Entry<String, Facet> entry : entries) {
				codeTitles.add(entry.getKey());
			}
		}
		return codeTitles;
	}

	public String getLabelForCode(String code) {
		return facets.get(code).getTitle();
	}

	public Record toRecord(RecordVO recordVO) throws OptimisticLockException {
		SchemaPresenterUtils schemaPresenterUtils = new SchemaPresenterUtils(recordVO.getSchema().getCode(),
				view.getConstellioFactories(), view.getSessionContext());
		return schemaPresenterUtils.toRecord(recordVO);
	}

}

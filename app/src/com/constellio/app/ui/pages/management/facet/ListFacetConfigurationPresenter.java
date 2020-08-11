package com.constellio.app.ui.pages.management.facet;

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
import com.constellio.model.frameworks.validation.OptimisticLockException;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotPhysicallyDeleteRecord;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ListFacetConfigurationPresenter extends BasePresenter<ListFacetConfigurationView> {

	private FacetConfigurationPresenterService service;

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
		service = new FacetConfigurationPresenterService(view.getConstellioFactories(), view.getSessionContext());
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_FACETS).globally();
	}

	public void addButtonClicked() {
		view.navigate().to().addFacetConfiguration();
	}

	public void orderButtonClicked() {
		view.navigate().to().orderFacetConfiguration();
	}

	public void displayButtonClicked(RecordVO recordVO) {
		view.navigate().to().displayFacetConfiguration(recordVO.getId());
	}

	public void deleteButtonClicked(RecordVO recordVO) {
		Record record = recordServices().getDocumentById(recordVO.getId());
		recordServices().logicallyDelete(record, User.GOD);
		try {
			recordServices().physicallyDeleteNoMatterTheStatus(record, User.GOD, new RecordPhysicalDeleteOptions());
		} catch (RecordServicesRuntimeException_CannotPhysicallyDeleteRecord e) {
			recordServices().restore(record, User.GOD);
			throw new RuntimeException(e);
		}

		view.navigate().to().listFacetConfiguration();
	}

	public void editButtonClicked(RecordVO recordVO) {
		view.navigate().to().editFacetConfiguration(recordVO.getId());
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
			public LogicalSearchQuery getQuery() {
				SchemasRecordsServices schemasRecords = new SchemasRecordsServices(collection, modelLayerFactory);
				LogicalSearchQuery query = new LogicalSearchQuery();
				query.setCondition(from(schemasRecords.facetSchemaType()).returnAll())
						.sortAsc(schemasRecords.defaultFacet().get(Facet.ORDER));
				return query;
			}
		};
	}

	public Record toRecord(RecordVO recordVO) throws OptimisticLockException {
		SchemaPresenterUtils schemaPresenterUtils = new SchemaPresenterUtils(recordVO.getSchema().getCode(),
				view.getConstellioFactories(), view.getSessionContext());
		return schemaPresenterUtils.toRecord(recordVO);
	}

	public void activate(RecordVO recordVO) {
		try {
			service.activate(recordVO.getId());
			view.refreshTable();
		} catch (Exception e) {
			view.showErrorMessage($("ListFacetConfigurationView.cannotActivateFacet", recordVO.getTitle()));
		}
	}

	public void deactivate(RecordVO recordVO) {
		try {
			service.deactivate(recordVO.getId());
			view.refreshTable();
		} catch (Exception e) {
			view.showErrorMessage($("ListFacetConfigurationView.cannotDeactivateFacet", recordVO.getTitle()));
		}
	}

	public boolean isActive(RecordVO recordVO) {
		return service.isActive(recordVO);
	}

	public void backButtonClicked() {
		view.navigate().to().searchConfiguration();
	}

}

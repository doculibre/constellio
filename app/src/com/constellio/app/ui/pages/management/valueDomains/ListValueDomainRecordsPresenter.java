package com.constellio.app.ui.pages.management.valueDomains;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.metadata.AppSchemasServices;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.pages.management.schemaRecords.SchemaRecordsPresentersServices;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.HierarchicalValueListItem;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotPhysicallyDeleteRecord;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotRestoreRecord;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.schemas.Schemas.ALL_REFERENCES;
import static com.constellio.model.services.search.StatusFilter.ACTIVES;
import static com.constellio.model.services.search.StatusFilter.DELETED;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

public class ListValueDomainRecordsPresenter extends SingleSchemaBasePresenter<ListValueDomainRecordsView> {

	private String freeText;
	private String schemaCode;

	public ListValueDomainRecordsPresenter(ListValueDomainRecordsView view) {
		super(view);
	}

	public void forSchema(String parameters) {
		schemaCode = parameters;
		setSchemaCode(schemaCode);
	}

	public RecordVODataProvider getDataProvider(final boolean actives) {
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder()
				.build(defaultSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		RecordToVOBuilder voBuilder = new RecordToVOBuilder();
		RecordVODataProvider dataProvider = new RecordVODataProvider(
				schemaVO, voBuilder, modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				MetadataSchema schema = defaultSchema();
				LogicalSearchCondition condition = from(schema).returnAll();
				if (isHierarchical()) {
					condition = condition.andWhere(schema.get(HierarchicalValueListItem.PARENT)).isNull();
				}
				LogicalSearchQuery query = new LogicalSearchQuery(condition)
						.filteredByStatus(actives ? ACTIVES : DELETED).sortAsc(Schemas.TITLE);

				if (StringUtils.isNotBlank(freeText)) {
					query.setFreeTextQuery(freeText);
				}

				return query;
			}
		};
		return dataProvider;
	}

	private boolean isHierarchical() {
		MetadataSchema schema = schema(schemaCode);
		return schema.hasMetadataWithCode(HierarchicalValueListItem.PARENT);
	}

	public void displayButtonClicked(RecordVO recordVO) {
		view.navigate().to().displaySchemaRecord(recordVO.getId());
	}

	public void editButtonClicked(RecordVO recordVO) {
		String schemaCode = getSchemaCode();
		view.navigate().to().editSchemaRecord(schemaCode, recordVO.getId());
	}

	public void addLinkClicked() {
		String schemaCode = getSchemaCode();
		view.navigate().to().addSchemaRecord(schemaCode);
	}

	public void deleteButtonClicked(RecordVO recordVO) {
		Record record = getRecord(recordVO.getId());
		RecordServices recordServices = modelLayerFactory.newRecordServices();

		if (record.isActive()) {
			try {
				recordServices.logicallyDelete(record, User.GOD);
			} catch (RecordServicesRuntimeException_CannotLogicallyDeleteRecord e) {
				view.showErrorMessage(MessageUtils.toMessage(e));
			}
		}

		if (!record.isActive()) {
			try {
				recordServices.physicallyDelete(record, User.GOD);
			} catch (RecordServicesRuntimeException_CannotPhysicallyDeleteRecord e) {
				view.showErrorMessage(MessageUtils.toMessage(e));
			}
		}

		view.refreshTables();

	}

	@Override
	protected boolean hasPageAccess(String params, final User user) {
		String schemaTypeCode = SchemaUtils.getSchemaTypeCode(params);
		return new SchemaRecordsPresentersServices(appLayerFactory).canManageSchemaType(schemaTypeCode, user);
	}

	public void search(String freeText) {
		this.freeText = freeText;
		view.refreshTables();
	}

	public void disableButtonClick(RecordVO recordVO) {
		Record record = getRecord(recordVO.getId());
		RecordServices recordServices = modelLayerFactory.newRecordServices();

		try {
			recordServices.logicallyDelete(record, User.GOD);
		} catch (RecordServicesRuntimeException_CannotLogicallyDeleteRecord e) {
			view.showErrorMessage(MessageUtils.toMessage(e));
		}

		view.refreshTables();
	}

	public void enableButtonClick(RecordVO recordVO) {
		Record record = getRecord(recordVO.getId());

		if (hasOtherActiveRecordWithSameCode(record)) {
			view.showErrorMessage($("ListValueDomainRecordsPresenter.otherActiveRecordHasSameCode"));
		} else {
			RecordServices recordServices = modelLayerFactory.newRecordServices();

			try {
				recordServices.restore(record, User.GOD);
			} catch (RecordServicesRuntimeException_CannotRestoreRecord e) {
				view.showErrorMessage(MessageUtils.toMessage(e));
			}

			enableLinkedSchema(record);
			view.refreshTables();
		}

	}

	private boolean hasOtherActiveRecordWithSameCode(Record record) {
		String code = record.get(Schemas.CODE);

		LogicalSearchQuery query = new LogicalSearchQuery();
		MetadataSchemaType type = types().getSchemaType(record.getTypeCode());
		query.filteredByStatus(StatusFilter.ACTIVES);
		query.setCondition(
				from(type).where(Schemas.CODE).isEqualTo(code).andWhere(Schemas.IDENTIFIER).isNotEqual(record.getId()));

		return searchServices().hasResults(query);
	}

	public void deleteAllUnused() {
		LogicalSearchQuery query = new LogicalSearchQuery(from(defaultSchema()).returnAll()).filteredByStatus(DELETED);
		Iterator<Record> recordIterator = searchServices().recordsIterator(query, 500);

		while (recordIterator.hasNext()) {
			Record record = recordIterator.next();

			if (!searchServices().hasResults(fromAllSchemasIn(collection).where(ALL_REFERENCES).isEqualTo(record.getId()))) {
				try {
					recordServices().physicallyDelete(record, User.GOD);
				} catch (RecordServicesRuntimeException_CannotPhysicallyDeleteRecord e) {
					view.showErrorMessage(MessageUtils.toMessage(e));
				}
			}
		}

		view.refreshTables();
	}

	private RMSchemasRecordsServices rmSchemas() {
		return new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	private void enableLinkedSchema(Record record) {
		String linkedSchema = rmSchemas().getLinkedSchemaOf(record);
		AppSchemasServices appSchemasServices = new AppSchemasServices(appLayerFactory);
		appSchemasServices.enableSchema(collection, linkedSchema);
	}
}

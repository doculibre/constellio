package com.constellio.app.ui.pages.management.Report;

import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class AddEditPrintableReportPresenter extends SingleSchemaBasePresenter<AddEditPrintableReportView> {
	private transient MetadataSchemasManager metadataSchemasManager;
	protected RecordVO container;
	private String currentType;

	public AddEditPrintableReportPresenter(AddEditPrintableReportView view) {
		super(view);
		initTransientObjects();
	}

	private void initTransientObjects() {
		metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_PRINTABLE_REPORT).globally();
	}

	public void saveButtonClicked(RecordVO rvo) throws Exception {
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		SchemaPresenterUtils utils = new SchemaPresenterUtils(PrintableReport.SCHEMA_NAME, view.getConstellioFactories(), view.getSessionContext());
		Record record = utils.toRecord(rvo);
		record.set(metadataSchemasManager.getSchemaTypes(collection).getMetadata(PrintableReport.SCHEMA_NAME + "_" + PrintableReport.ISDELETABLE), true);
		Transaction trans = new Transaction();
		trans.update(record);
		recordServices.execute(trans);
		view.navigate().to().previousView();
	}

	public void cancelButtonClicked() {
		view.navigate().to().previousView();
	}

	@Override
	protected Record newRecord() {
		super.setSchemaCode(PrintableReport.SCHEMA_NAME);
		return super.newRecord();
	}

	public RecordVO getRecordVO(String id) {
		LogicalSearchCondition condition = from(modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchema(PrintableReport.SCHEMA_NAME)).where(Schemas.IDENTIFIER).isEqualTo(id);
		Record printableReportRecord = searchServices().searchSingleResult(condition);
		return new RecordToVOBuilder().build(printableReportRecord, RecordVO.VIEW_MODE.FORM, view.getSessionContext());
	}

	public void setCurrentType(String schemaType) {
		this.currentType = schemaType;
	}

	public List<MetadataSchema> getSchemasForCurrentType() {
		return currentType != null ? modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(currentType).getAllSchemas() : new ArrayList<MetadataSchema>();
	}

	public String getLabelForSchemaType(String schemaType) {
		return metadataSchemasManager.getSchemaTypes(view.getCollection()).getSchemaType(schemaType).getLabel(Language.withLocale(view.getSessionContext().getCurrentLocale()));
	}
}

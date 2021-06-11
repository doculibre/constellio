package com.constellio.app.ui.pages.management.Report;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;

public class DisplayPrintableReportPresenter extends SingleSchemaBasePresenter<DisplayPrintableReportView> {

	public DisplayPrintableReportPresenter(DisplayPrintableReportView view) {
		super(view);
	}

	public RecordVO getRecordVO(String id) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(view.getCollection(), view);
		RecordToVOBuilder voBuilder = new RecordToVOBuilder();
		return voBuilder.build(rm.getPrintableReport(id).getWrappedRecord(), RecordVO.VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_PRINTABLE_REPORT).globally();
	}

	protected void backButtonClicked() {
		view.navigate().to().previousView();
	}

	protected String getRecordTypeValue(RecordVO recordVO) {
		MetadataSchemaType metadataSchemaType = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(recordVO.<String>get(PrintableReport.RECORD_TYPE));
		return metadataSchemaType.getLabel(Language.withLocale(view.getSessionContext().getCurrentLocale()));
	}

	protected String getRecordSchemaValue(RecordVO recordVO) {
		MetadataSchemaType metadataSchemaType = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(recordVO.<String>get(PrintableReport.RECORD_TYPE));
		String recordSchema = recordVO.get(PrintableReport.RECORD_SCHEMA);
		return recordSchema != null ?
			   metadataSchemaType.getSchema(recordSchema).getLabel(Language.withLocale(view.getSessionContext().getCurrentLocale())) :
			   null;
	}
}

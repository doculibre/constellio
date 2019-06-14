package com.constellio.app.ui.pages.imports;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.ExportAudit;
import com.constellio.model.entities.records.wrappers.ImportAudit;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ListImportExportPresenter extends BasePresenter<ListImportExportView> {

	public ListImportExportPresenter(ListImportExportView view) {
		super(view);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public RecordVODataProvider getImportDataProvider() {
		final MetadataSchemaType temporaryRecordSchemaType = types().getSchemaType(TemporaryRecord.SCHEMA_TYPE);
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder().build(temporaryRecordSchemaType.getSchema(ImportAudit.SCHEMA),
				RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
		RecordVODataProvider dataProvider = new RecordVODataProvider(
				schemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {

				return new LogicalSearchQuery().setCondition(from(temporaryRecordSchemaType.getSchema(ImportAudit.SCHEMA))
						.returnAll())
						.sortDesc(Schemas.CREATED_ON);
			}
		};
		return dataProvider;
	}

	public RecordVODataProvider getExportDataProvider() {
		final MetadataSchemaType temporaryRecordSchemaType = types().getSchemaType(TemporaryRecord.SCHEMA_TYPE);
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder().build(temporaryRecordSchemaType.getSchema(ExportAudit.SCHEMA),
				RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
		RecordVODataProvider dataProvider = new RecordVODataProvider(
				schemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {

				return new LogicalSearchQuery().setCondition(from(temporaryRecordSchemaType.getSchema(ExportAudit.SCHEMA))
						.returnAll())
						.sortDesc(Schemas.CREATED_ON);
			}
		};
		return dataProvider;
	}
}

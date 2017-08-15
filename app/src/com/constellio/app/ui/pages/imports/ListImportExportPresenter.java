package com.constellio.app.ui.pages.imports;

import com.constellio.app.ui.entities.BatchProcessVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.BatchProcessToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.BatchProcessDataProvider;
import com.constellio.app.ui.framework.data.DataProvider;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.batchprocess.ListBatchProcessesView;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.wrappers.ImportExportAudit;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.vaadin.data.Container;
import org.joda.time.Hours;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.records.wrappers.ImportExportAudit.ExportImport.*;

public class ListImportExportPresenter extends BasePresenter<ListImportExportView> {

	public ListImportExportPresenter(ListImportExportView view) {
		super(view);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public RecordVODataProvider getImportDataProvider() {
		final MetadataSchemaType importExportSchemaType = types().getSchemaType(ImportExportAudit.SCHEMA_TYPE);
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder().build(importExportSchemaType.getDefaultSchema(),
				RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
		RecordVODataProvider dataProvider = new RecordVODataProvider(
				schemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {

				return new LogicalSearchQuery().setCondition(LogicalSearchQueryOperators.from(importExportSchemaType
				).where(importExportSchemaType.getDefaultSchema().getMetadata(ImportExportAudit.TYPE)).isEqualTo(IMPORT))
						.sortDesc(importExportSchemaType.getDefaultSchema().getMetadata(ImportExportAudit.START_DATE));
			}
		};
		return  dataProvider;
	}

	public RecordVODataProvider getExportDataProvider() {
		final MetadataSchemaType importExportSchemaType = types().getSchemaType(ImportExportAudit.SCHEMA_TYPE);
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder().build(importExportSchemaType.getDefaultSchema(),
				RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
		RecordVODataProvider dataProvider = new RecordVODataProvider(
				schemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {

				return new LogicalSearchQuery().setCondition(LogicalSearchQueryOperators.from(importExportSchemaType
				).where(importExportSchemaType.getDefaultSchema().getMetadata(ImportExportAudit.TYPE)).isEqualTo(EXPORT))
						.sortDesc(importExportSchemaType.getDefaultSchema().getMetadata(ImportExportAudit.START_DATE));
			}
		};
		return  dataProvider;
	}
}

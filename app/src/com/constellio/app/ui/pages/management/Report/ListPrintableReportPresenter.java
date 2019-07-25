package com.constellio.app.ui.pages.management.Report;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.server.StreamResource;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ListPrintableReportPresenter extends SingleSchemaBasePresenter<ListPrintableReportView> {
	private Map<PrintableReportListPossibleType, String> POSSIBLE_SCHEMA_TYPE = new HashMap<PrintableReportListPossibleType, String>() {{
		put(PrintableReportListPossibleType.FOLDER, Folder.SCHEMA_TYPE);
		put(PrintableReportListPossibleType.DOCUMENT, Document.SCHEMA_TYPE);
		put(PrintableReportListPossibleType.TASK, Task.SCHEMA_TYPE);
	}};

	private MetadataSchemaToVOBuilder schemaVOBuilder;
	private ListPrintableReportView view;
	private RecordVODataProvider folderDataAdapter, documentDataAdapter, taskDataAdapter;

	public ListPrintableReportPresenter(ListPrintableReportView view) {
		super(view);
		this.view = view;
		initTransientObjects();
	}

	public ListPrintableReportView getView() {
		return this.view;
	}

	private void initTransientObjects() {
		schemaVOBuilder = new MetadataSchemaToVOBuilder();
	}

	private RecordVODataProvider getDataProviderForSchemaType(final String schemaType) {
		final MetadataSchemaVO printableReportVO = schemaVOBuilder.build(
				modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchema(PrintableReport.SCHEMA_NAME),
				RecordVO.VIEW_MODE.TABLE,
				view.getSessionContext());
		return new RecordVODataProvider(printableReportVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				MetadataSchemaType printableSchemaType = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(Printable.SCHEMA_TYPE);
				return schemaType == null ? null : new LogicalSearchQuery(from(printableSchemaType).where(printableSchemaType.getCustomSchema(PrintableReport.SCHEMA_TYPE).get(PrintableReport.RECORD_TYPE)).isEqualTo(schemaType));
			}
		};
	}

	public RecordVODataProvider getPrintableReportFolderDataProvider() {
		if (this.folderDataAdapter == null) {
			folderDataAdapter = getDataProviderForSchemaType(PrintableReportListPossibleType.FOLDER.getSchemaType());
		}
		return folderDataAdapter;
	}

	public RecordVODataProvider getPrintableReportDocumentDataProvider() {
		if (this.documentDataAdapter == null) {
			this.documentDataAdapter = getDataProviderForSchemaType(PrintableReportListPossibleType.DOCUMENT.getSchemaType());
		}
		return documentDataAdapter;
	}

	public RecordVODataProvider getPrintableReportTaskDataProvider() {
		if (this.taskDataAdapter == null) {
			taskDataAdapter = getDataProviderForSchemaType(PrintableReportListPossibleType.TASK.getSchemaType());
		}
		return taskDataAdapter;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_PRINTABLE_REPORT).globally();
	}

	public void addLabelButtonClicked() {
		view.navigate().to().addPrintableReport();
	}

	protected void editButtonClicked(RecordVO report) {
		view.navigate().to().editPrintableReport(report.getId());
	}

	protected void displayButtonClicked(RecordVO report) {
		view.navigate().to().displayPrintableReport(report.getId());
	}

	public void removeRecord(RecordVO report) {
		customDelete(recordServices().getDocumentById(report.getId()));
		view.navigate().to().managePrintableReport();
	}

	public void customDelete(Record record) {
		if (recordServices().validateLogicallyThenPhysicallyDeletable(record, User.GOD).isEmpty()) {
			recordServices().logicallyDelete(record, User.GOD);
			recordServices().physicallyDelete(record, User.GOD);
		}
	}

	protected StreamResource createResource() {
		return new StreamResource(new StreamResource.StreamSource() {
			@Override
			public InputStream getStream() {
				InputStream stream = null;
				try {
					File file = new File(new FoldersLocator().getModuleResourcesFolder("rm"), "Template_PrintableReport.zip");
					stream = new ByteArrayInputStream(FileUtils.readFileToByteArray(file));
				} catch (IOException e) {
					e.printStackTrace();
				}
				return stream;
			}
		}, "templates.zip");
	}
}

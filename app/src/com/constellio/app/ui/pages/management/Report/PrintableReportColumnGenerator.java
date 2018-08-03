package com.constellio.app.ui.pages.management.Report;

import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.model.entities.Language;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class PrintableReportColumnGenerator implements Table.ColumnGenerator {
	public static final String RECORD_SCHEMA = "recordSchema";
	public static final String RECORD_TYPE = "recordType";
	public static final String TITLE = "title";
	public static final String BUTTON_CONTAINER = "buttonContainer";

	private boolean withRecordSchema;
	private boolean withRecordType;
	private ListPrintableReportPresenter presenter;
	private PrintableReportListPossibleType schema;
	private ButtonsContainer buttonContainer;

	public PrintableReportColumnGenerator(ListPrintableReportPresenter presenter,
										  PrintableReportListPossibleType schema) {
		this.presenter = presenter;
		this.schema = schema;
	}

	public PrintableReportColumnGenerator withRecordSchema() {
		this.withRecordSchema = true;
		return this;
	}

	public PrintableReportColumnGenerator withRecordType() {
		this.withRecordType = true;
		return this;
	}

	public PrintableReportColumnGenerator withButtonContainer(ButtonsContainer buttonContainer) {
		this.buttonContainer = buttonContainer;
		return this;
	}

	public BaseTable attachTo(BaseTable table) {
		List<String> visibleColumns = new ArrayList<>();

		table.addGeneratedColumn(TITLE, this);
		table.setColumnHeader(TITLE, $("title"));
		table.setColumnAlignment(TITLE, Table.Align.CENTER);
		visibleColumns.add(TITLE);

		if (withRecordType) {
			table.addGeneratedColumn(RECORD_TYPE, this);
			table.setColumnHeader(RECORD_TYPE, $("PrintableReport.list.reportType"));
			table.setColumnAlignment(RECORD_TYPE, Table.Align.CENTER);
			visibleColumns.add(RECORD_TYPE);
		}

		if (withRecordSchema) {
			table.addGeneratedColumn(RECORD_SCHEMA, this);
			table.setColumnHeader(RECORD_SCHEMA, $("PrintableReport.list.reportSchema"));
			table.setColumnAlignment(RECORD_SCHEMA, Table.Align.CENTER);
			visibleColumns.add(RECORD_SCHEMA);
		}

		if (buttonContainer != null) {
			table.addGeneratedColumn(BUTTON_CONTAINER, this);
			table.setColumnHeader(BUTTON_CONTAINER, "");
			table.setColumnAlignment(BUTTON_CONTAINER, Table.Align.CENTER);
			visibleColumns.add(BUTTON_CONTAINER);
		}

		table.setVisibleColumns(visibleColumns.toArray(new String[0]));
		return table;
	}

	@Override
	public Object generateCell(Table source, Object itemId, Object columnId) {
		RecordVO detail = presenter.getRecordsWithIndex(schema, itemId + "");
		switch ((String) columnId) {
			case RECORD_SCHEMA:
				return buildRecordSchemaColumn(detail);
			case RECORD_TYPE:
				return buildRecordTypeColumn(detail);
			case BUTTON_CONTAINER:
				return buttonContainer;
			default:
				return new Label(detail.getTitle());
		}
	}

	private Label buildRecordSchemaColumn(RecordVO detail) {
		MetadataSchemasManager metadataSchemasManager = presenter.getView().getConstellioFactories().getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager();
		String label = metadataSchemasManager.getSchemaTypes(presenter.getView().getCollection()).getSchemaType(PrintableReportListPossibleType.valueOf(detail.<String>get(PrintableReport.RECORD_TYPE)).getSchemaType()).getSchema(detail.<String>get(PrintableReport.RECORD_SCHEMA)).getLabel(Language.withCode(presenter.getView().getSessionContext().getCurrentLocale().getLanguage()));
		return new Label(label);
	}

	private Label buildRecordTypeColumn(RecordVO detail) {
		MetadataSchemasManager metadataSchemasManager = presenter.getView().getConstellioFactories().getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager();
		String label = metadataSchemasManager.getSchemaTypes(presenter.getView().getCollection()).getSchemaType(PrintableReportListPossibleType.valueOf(detail.<String>get(PrintableReport.RECORD_TYPE)).getSchemaType()).getLabel(Language.withCode(presenter.getView().getSessionContext().getCurrentLocale().getLanguage()));
		return new Label(label);
	}
}

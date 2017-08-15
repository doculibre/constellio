package com.constellio.app.ui.pages.imports;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.records.wrappers.ImportExportAudit;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListImportExportViewImpl extends BaseViewImpl implements ListImportExportView {

	private ListImportExportPresenter presenter;

	public ListImportExportViewImpl() {
		presenter = new ListImportExportPresenter(this);
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.addStyleName("batch-processes");
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);
		TabSheet tabSheet = new TabSheet();
		tabSheet.addTab(newImportTable(), ("ListImportExportView.importTable"));
		tabSheet.addTab(newExportTable(), ("ListImportExportView.exportTable"));
		mainLayout.addComponent(tabSheet);
		
		return mainLayout;
	}

	private BaseTable newImportTable() {
		RecordVOTable importTable = new RecordVOTable(presenter.getImportDataProvider()) {
			@Override
			protected Component buildMetadataComponent(MetadataValueVO metadataValue, RecordVO recordVO) {
				return super.buildMetadataComponent(metadataValue, recordVO);
			}
		};
		importTable.setWidth("98%");
		importTable.setCellStyleGenerator(newStyleGenerator());

		return importTable;
	}

	private BaseTable newExportTable() {
		RecordVOTable exportTable = new RecordVOTable(presenter.getExportDataProvider()) {
			@Override
			protected Component buildMetadataComponent(MetadataValueVO metadataValue, RecordVO recordVO) {
				return super.buildMetadataComponent(metadataValue, recordVO);
			}
		};
		exportTable.setWidth("98%");
		exportTable.setCellStyleGenerator(newStyleGenerator());

		return exportTable;
	}

	private Table.CellStyleGenerator newStyleGenerator() {
		return new Table.CellStyleGenerator() {

			@Override
			public String getStyle(Table source, Object itemId, Object propertyId) {
				try {
					RecordVOItem item = (RecordVOItem) source.getItem(itemId);
					RecordVO record = item.getRecord();
					MetadataVO errors = record.getMetadata(ImportExportAudit.ERRORS);
					if(errors != null && !((List)record.getMetadataValue(errors).getValue()).isEmpty()) {
						return "error";
					}
				} catch (Exception e) {

				}
				return null;
			}
		};
	}

	@Override
	protected boolean isBackgroundViewMonitor() {
		return true;
	}

	@Override
	protected void onBackgroundViewMonitor() {
//	presenter.backgroundViewMonitor();
	}

	@Override
	protected String getTitle() {
		return $("ListBatchProcessesView.viewTitle");
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

}

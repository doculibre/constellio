package com.constellio.app.modules.es.ui.pages;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DownloadLink;
import com.constellio.app.ui.framework.components.BaseDisplay;
import com.constellio.app.ui.framework.components.BaseDisplay.CaptionAndComponent;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.table.BasePagedTable;
import com.constellio.app.ui.framework.containers.RecordVOWithDistinctSchemaTypesLazyContainer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Property;
import com.vaadin.data.util.converter.StringToLongConverter;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.constellio.app.ui.i18n.i18n.$;

public class ConnectorReportViewImpl extends BaseViewImpl implements ConnectorReportView {
	private static Logger LOGGER = LoggerFactory.getLogger(ConnectorReportViewImpl.class);

	private ConnectorReportPresenter presenter;
	private BasePagedTable<RecordVOWithDistinctSchemaTypesLazyContainer> table;
	private HorizontalLayout tableControls;
	private BaseTextField linesField;
	private BaseTextField filterField;
	private VerticalLayout mainLayout;
	private Layout csvLayout;
	private String title;

	public ConnectorReportViewImpl() {
		presenter = new ConnectorReportPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		super.initBeforeCreateComponents(event);
		presenter.forParams(event.getParameters());
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);

		BaseDisplay statsDisplay = buildStatsDisplay();
		Layout filterComponent = buildFilterComponent();

		RecordVOWithDistinctSchemaTypesLazyContainer container = new RecordVOWithDistinctSchemaTypesLazyContainer(
				presenter.getDataProvider(), presenter.getReportMetadataList());
		table = buildTable(container);
		table.setColumnHeader("url", $("ConnectorReportView.url"));
		table.setColumnHeader("fetchedDateTime", $("ConnectorReportView.fetchedDateTime"));
		table.setColumnHeader("errorCode", $("ConnectorReportView.errorCode"));
		table.setColumnHeader("errorMessage", $("ConnectorReportView.errorMessage"));
		table.setColumnHeader("subject", $("ConnectorReportView.subject"));
		table.setColumnHeader("downloadTime", $("ConnectorReportView.downloadTime"));
		table.setColumnHeader("copyOf", $("ConnectorReportView.copyOf"));
		table.setColumnHeader("title", $("ConnectorReportView.title"));
		table.setColumnHeader("type", $("ConnectorReportView.type"));
		tableControls = table.createControls();
		csvLayout = createCSVLinkLayout(table);
		mainLayout.addComponents(statsDisplay, filterComponent, csvLayout, table, tableControls);

		return mainLayout;
	}

	private HorizontalLayout buildFilterComponent() {
		HorizontalLayout filterComponent = new HorizontalLayout();
		filterComponent.setSpacing(true);
		filterField = new BaseTextField();
		BaseButton filterButton = new BaseButton($("ConnectorReportView.filterButton")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.filterButtonClicked();
			}
		};
		filterComponent.addComponents(filterField, filterButton);
		return filterComponent;
	}

	private BaseDisplay buildStatsDisplay() {
		List<CaptionAndComponent> components = new ArrayList<>();
		components.add(new CaptionAndComponent(new Label($("ConnectorReportView.totalDocsFound")),
				new Label(presenter.getTotalDocumentsCount().toString())));
		components.add(new CaptionAndComponent(new Label($("ConnectorReportView.fetchedDocsFound")),
				new Label(presenter.getFetchedDocumentsCount().toString())));
		components.add(new CaptionAndComponent(new Label($("ConnectorReportView.unfetchedDocsFound")),
				new Label(presenter.getUnfetchedDocumentsCount().toString())));
		return new BaseDisplay(components);
	}

	private BasePagedTable buildTable(RecordVOWithDistinctSchemaTypesLazyContainer container) {
		BasePagedTable table = new BasePagedTable<>("connector-report-table", container);
		table.addStyleName("connector-report-table");
		table.setContainerDataSource(container);
		table.setWidth("100%");
		return table;
	}

	@Override
	public void filterTable() {
		RecordVOWithDistinctSchemaTypesLazyContainer container = new RecordVOWithDistinctSchemaTypesLazyContainer(
				presenter.getFilteredDataProvider(filterField.getValue()), presenter.getReportMetadataList());
		BasePagedTable<RecordVOWithDistinctSchemaTypesLazyContainer> newTable = buildTable(container);
		newTable.setColumnHeader("url", $("ConnectorReportView.url"));
		newTable.setColumnHeader("fetchedDateTime", $("ConnectorReportView.fetchedDateTime"));
		newTable.setColumnHeader("errorCode", $("ConnectorReportView.errorCode"));
		newTable.setColumnHeader("errorMessage", $("ConnectorReportView.errorMessage"));
		HorizontalLayout newTableControls = newTable.createControls();
		Layout newCsvLayout = createCSVLinkLayout(newTable);
		mainLayout.replaceComponent(csvLayout, newCsvLayout);
		mainLayout.replaceComponent(table, newTable);
		mainLayout.replaceComponent(tableControls, newTableControls);
		csvLayout = newCsvLayout;
		table = newTable;
		tableControls = newTableControls;
	}

	@Override
	protected String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
	}

	protected Layout createCSVLinkLayout(BasePagedTable table) {
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);

		final DownloadLink downloadLink = new DownloadLink(getCsvDocumentResource(table), $("ConnectorReportView.downloadCsv"));

		Label labelLinesField = new Label($("ConnectorReportView.linesFieldLabel"));

		linesField = new BaseTextField();
		linesField.setConversionError($("ConnectorReportView.linesFieldConversionError"));
		linesField.setConverter(new StringToLongConverter());
		linesField.addValueChangeListener(new Property.ValueChangeListener() {
			private Object oldValue;
			private DownloadLink oldLink = downloadLink;

			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				Object current = linesField.getConvertedValue();

				if (!Objects.equals(oldValue, current)) {
					DownloadLink newComponent = new DownloadLink(getCsvDocumentResource(ConnectorReportViewImpl.this.table), $("ConnectorReportView.downloadCsv"));
					layout.replaceComponent(oldLink, newComponent);
					oldLink = newComponent;
				}

				oldValue = current;
			}
		});

		layout.addComponents(labelLinesField, linesField, downloadLink);
		layout.setComponentAlignment(labelLinesField, Alignment.MIDDLE_LEFT);
		layout.setComponentAlignment(linesField, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(downloadLink, Alignment.MIDDLE_RIGHT);

		HorizontalLayout layout2 = new HorizontalLayout();
		layout2.setSizeFull();

		layout2.addComponent(layout);
		layout2.setComponentAlignment(layout, Alignment.MIDDLE_RIGHT);

		layout2.setVisible(table.getContainer().size() > 0);

		return layout2;
	}

	private Resource getCsvDocumentResource(final BasePagedTable table) {
		return new StreamResource(new StreamResource.StreamSource() {
			@Override
			public InputStream getStream() {
				try {
					return new FileInputStream(new ConnectorReportCSVProducer(table, (Long) linesField.getConvertedValue()).produceCSVFile());
				} catch (IOException e) {
					LOGGER.error("Error during CSV generation", e);
					return null;
				}
			}
		}, getTitle() + ".csv");
	}
}

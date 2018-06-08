package com.constellio.app.ui.pages.management.extractors;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.*;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.TabWithTable;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveTextField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.components.fields.upload.BaseUploadField;
import com.constellio.app.ui.framework.components.fields.upload.TempFileUpload;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.data.GlobalGroupVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.extractors.fields.ListAddRemoveRegexConfigField;
import com.constellio.app.ui.pages.management.extractors.fields.PropertiesAnalyserView;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.parser.FileParser;
import com.constellio.model.services.parser.ForkParsers;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.apache.tika.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class PropertiesAnalyserViewImpl extends BaseViewImpl implements PropertiesAnalyserView {

	private TabSheet propertiesAndStylesTabs;
	private TabWithTable propertiesTab;
	private TabWithTable stylesTab;
	private LookupRecordField lookupRecordField;
	private BaseUploadField uploadField;

	private PropertiesAnalyserPresenter presenter;

	public PropertiesAnalyserViewImpl() {
		this.presenter = new PropertiesAnalyserPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {

	}

	@Override
	protected void afterViewAssembled(ViewChangeEvent event) {

	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);

		uploadField = new BaseUploadField();
		uploadField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				updateTempFile();
				propertiesTab.refreshTable();
				stylesTab.refreshTable();
			}
		});

		propertiesAndStylesTabs = new TabSheet();

		propertiesTab = new TabWithTable("propertiesTab") {
			@Override
			public Table buildTable() {
				return buildPropertiesTable();
			}
		};

		stylesTab = new TabWithTable("stylesTab") {
			@Override
			public Table buildTable() {
				return buildStylesTable();
			}
		};

		propertiesAndStylesTabs.addTab(propertiesTab.getTabLayout(), $("PropertiesAnalyserView.propertiesTab"));
		propertiesAndStylesTabs.addTab(stylesTab.getTabLayout(), $("PropertiesAnalyserView.stylesTab"));

		mainLayout.addComponents(uploadField, propertiesAndStylesTabs);
		return mainLayout;
	}

	private void updateTempFile() {
		TempFileUpload value = (TempFileUpload) uploadField.getValue();
		File tempFile = value == null? null:value.getTempFile();
		presenter.calculatePropertiesAndStyles(tempFile);
	}

	private Table buildPropertiesTable() {
		Table table = new Table();
		table.setWidth("100%");
		table.setColumnHeader("Key", $("PropertiesAnalyserView.key"));
		table.setColumnHeader("Value", $("PropertiesAnalyserView.value"));

		table.setContainerDataSource(buildContainer(presenter.getPropertiesContainer(), String.class));
		return table;
	}

	private Table buildStylesTable() {
		Table table = new Table();
		table.setWidth("100%");
		table.setColumnHeader("Key", $("PropertiesAnalyserView.key"));
		table.setColumnHeader("Value", $("PropertiesAnalyserView.value"));

		table.setContainerDataSource(buildContainer(presenter.getStylesContainer(), List.class));
		return table;
	}

	private Container buildContainer(Map<String, Object> values, Class<?> valueType) {
		IndexedContainer container = new IndexedContainer();
		container.addContainerProperty("Key", String.class, "");
		container.addContainerProperty("Value", valueType, "");

		for(Map.Entry<String, Object> property: values.entrySet()) {
			Item item = container.addItem(property.getKey());
			item.getItemProperty("Key").setValue(property.getKey());
			item.getItemProperty("Value").setValue(property.getValue());
		}
		return container;
	}

	@Override
	protected String getTitle() {
		return $("PropertiesAnalyserView.viewTitle");
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

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return new TitleBreadcrumbTrail(this, getTitle()) {
			@Override
			public List<? extends IntermediateBreadCrumbTailItem> getIntermeiateItems() {
				return asList(new IntermediateBreadCrumbTailItem() {
					@Override
					public boolean isEnabled() {
						return true;
					}

					@Override
					public String getTitle() {
						return $("ViewGroup.AdminViewGroup");
					}

					@Override
					public void activate(Navigation navigate) {
						navigate.to().adminModule();
					}
				},
				new IntermediateBreadCrumbTailItem() {
					@Override
					public boolean isEnabled() {
						return true;
					}

					@Override
					public String getTitle() {
						return $("ListMetadataExtractorsView.viewTitle");
					}

					@Override
					public void activate(Navigation navigate) {
						navigate.to().listMetadataExtractors();
					}
				});
			}
		};
	}
}

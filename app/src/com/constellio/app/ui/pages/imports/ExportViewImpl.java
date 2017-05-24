package com.constellio.app.ui.pages.imports;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Property;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

import java.io.InputStream;

import static com.constellio.app.ui.i18n.i18n.$;

public class ExportViewImpl extends BaseViewImpl implements ExportView {

	public static final String TOOL_OPTION = "toolOption";
	public static final String FOLDER_AND_DOCUMENT_OPTION = "folderAndDocumentOption";
	public static final String ADMINISTRATIVE_UNIT_OPTION = "administrativeUnitOption";
	public static final String OTHERS_OPTION = "othersOption";

	private ListOptionGroup exportationOptions;

	private TextArea idsField;

	private Button exportWithoutContentsButton;

	private Button exportWithContentsButton;

	private Button exportLogs;

	private Button exportTools;

	private VerticalLayout toolLayout = new VerticalLayout();
	private VerticalLayout folderAndDocumentsLayout = new VerticalLayout();
	private VerticalLayout administrativeUnitLayout = new VerticalLayout();
	private VerticalLayout othersLayout = new VerticalLayout();

	private final ExportPresenter presenter;

	public ExportViewImpl() {
		presenter = new ExportPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ExportView.viewTitle");
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonPressed();
			}
		};
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout mainLayout = new VerticalLayout();
		exportationOptions = new ListOptionGroup($("ExportView.exportationOptions"));
		exportationOptions.setEnabled(presenter.hasCurrentCollectionRMModule());
		exportationOptions.setVisible(presenter.hasCurrentCollectionRMModule());
		exportationOptions.setMultiSelect(false);
		exportationOptions.setNullSelectionAllowed(false);

		exportationOptions.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				adjustFields();
			}
		});

		if(presenter.hasCurrentCollectionRMModule()) {
			exportationOptions.addItem(TOOL_OPTION);
			exportationOptions.setItemCaption(TOOL_OPTION, $($("ExportView.toolOption")));
			exportationOptions.addItem(FOLDER_AND_DOCUMENT_OPTION);
			exportationOptions.setItemCaption(FOLDER_AND_DOCUMENT_OPTION, $($("ExportView.folderAndDocumentOption")));
			exportationOptions.addItem(ADMINISTRATIVE_UNIT_OPTION);
			exportationOptions.setItemCaption(ADMINISTRATIVE_UNIT_OPTION, $($("ExportView.administrativeUnitOption")));
			initRMLayouts();
		}

		exportationOptions.addItem(OTHERS_OPTION);
		exportationOptions.setItemCaption(OTHERS_OPTION, $($("ExportView.othersOption")));

		idsField = new BaseTextArea($("ExportView.exportedIds"));
		idsField.setWidth("100%");

		exportWithoutContentsButton = new BaseButton($("ExportView.exportNoContents")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.exportWithoutContentsButtonClicked();
			}
		};
		exportTools = new BaseButton($("ExportView.exportTools")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.exportToolsButtonClicked();
			}
		};

		exportWithContentsButton = new BaseButton($("ExportView.exportAllContents")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.exportWithContentsButtonClicked();
			}
		};
		exportLogs = new BaseButton($("ExportView.exportLogs")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.exportLogs();
			}
		};
		exportWithContentsButton.setVisible(false);

		exportationOptions.setValue(OTHERS_OPTION);

		othersLayout = new VerticalLayout(idsField, exportWithoutContentsButton, exportWithContentsButton, exportTools,
				exportLogs);
		othersLayout.setSizeFull();
		othersLayout.setSpacing(true);

		mainLayout.addComponent(exportationOptions);
		if(presenter.hasCurrentCollectionRMModule()) {
			mainLayout.addComponents(toolLayout, folderAndDocumentsLayout, administrativeUnitLayout);
		}
		mainLayout.addComponent(othersLayout);

		return mainLayout;
	}

	private void initRMLayouts() {
		buildToolLayout();
		buildFolderAndDocumentLayout();
		buildAdministrativeUnitLayout();
	}

	private Component buildToolLayout() {
		toolLayout = new VerticalLayout();
		toolLayout.setSizeFull();
		toolLayout.setSpacing(true);
		BaseButton exportTools = new BaseButton($("ExportView.exportTools")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.exportToolsToXMLButtonClicked();
			}
		};
		toolLayout.addComponent(exportTools);
		return toolLayout;
	}

	private Component buildFolderAndDocumentLayout() {
		folderAndDocumentsLayout = new VerticalLayout();
		folderAndDocumentsLayout.setSizeFull();
		folderAndDocumentsLayout.setSpacing(true);
		final ListAddRemoveRecordLookupField folderField = new ListAddRemoveRecordLookupField(Folder.SCHEMA_TYPE);
		folderField.setCaption($("ExportView.folders"));
		final ListAddRemoveRecordLookupField documentField = new ListAddRemoveRecordLookupField(Document.SCHEMA_TYPE);
		documentField.setCaption($("ExportView.documents"));
		BaseButton exportButton = new BaseButton($("ExportView.exportNoContents")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.exportWithoutContentsXMLButtonClicked(folderField.getValue(), documentField.getValue());
			}
		};
		folderAndDocumentsLayout.addComponents(folderField, documentField, exportButton);
		return folderAndDocumentsLayout;
	}

	private Component buildAdministrativeUnitLayout() {
		administrativeUnitLayout = new VerticalLayout();
		administrativeUnitLayout.setSizeFull();
		administrativeUnitLayout.setSpacing(true);
		final LookupRecordField administrativeUnitField = new LookupRecordField(AdministrativeUnit.SCHEMA_TYPE);
		administrativeUnitField.setCaption($("ExportView.administrativeUnit"));
		BaseButton exportButton = new BaseButton($("ExportView.exportNoContents")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.exportAdministrativeUnitXMLButtonClicked(administrativeUnitField.getValue());
			}
		};
		administrativeUnitLayout.addComponents(administrativeUnitField, exportButton);
		return administrativeUnitLayout;
	}

	private void adjustFields() {
		toolLayout.setVisible(false);
		folderAndDocumentsLayout.setVisible(false);
		administrativeUnitLayout.setVisible(false);
		othersLayout.setVisible(false);
		switch ((String) exportationOptions.getValue()) {
			case TOOL_OPTION:
				toolLayout.setVisible(true);
				break;
			case FOLDER_AND_DOCUMENT_OPTION:
				folderAndDocumentsLayout.setVisible(true);
				break;
			case ADMINISTRATIVE_UNIT_OPTION:
				administrativeUnitLayout.setVisible(true);
				break;
			case OTHERS_OPTION:
				othersLayout.setVisible(true);
				break;
		}
	}

	@Override
	public String getExportedIds() {
		return idsField.getValue();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void startDownload(final String filename, final InputStream inputStream, final String mimeType) {
		StreamSource streamSource = new StreamSource() {
			@Override
			public InputStream getStream() {
				return inputStream;
			}
		};
		StreamResource resource = new StreamResource(streamSource, filename);
		resource.setMIMEType(mimeType);
		Page.getCurrent().open(resource, "_blank", false);
	}
}

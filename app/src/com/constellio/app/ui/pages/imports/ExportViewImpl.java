package com.constellio.app.ui.pages.imports;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Property;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ExportViewImpl extends BaseViewImpl implements ExportView {

	public static final String TOOL_OPTION = "toolOption";
	public static final String FOLDER_AND_DOCUMENT_OPTION = "folderAndDocumentOption";
	public static final String ADMINISTRATIVE_UNIT_OPTION = "administrativeUnitOption";
	public static final String SCHEMA_OPTION = "schemaOption";
	public static final String OTHERS_OPTION = "othersOption";
	public static final String COMPLETE_OPTION = "completeOption";
	public static final String IDS_OPTION = "idsOption";

	public static final String SAME_COLLECTION = "sameCollection";
	public static final String OTHER_COLLECTION = "otherCollection";

	private ListOptionGroup exportationOptions;
	private ListOptionGroup collectionOptions;

	private TextArea idsField;

	private Button exportWithoutContentsButton;

	private Button exportWithContentsButton;

	private Button exportLogs;

	private Button exportTools;

	private CheckBox fastSaveStateCheckBox;

	private CheckBox personalizedMetadataOnlyCheckBox;

	private VerticalLayout toolLayout = new VerticalLayout();
	private VerticalLayout idsLayout = new VerticalLayout();
	private VerticalLayout folderAndDocumentsLayout = new VerticalLayout();
	private VerticalLayout administrativeUnitLayout = new VerticalLayout();
	private VerticalLayout schemaLayout = new VerticalLayout();
	private VerticalLayout completeLayout = new VerticalLayout();
	private VerticalLayout othersLayout = new VerticalLayout();

	private boolean showHiddenOptions = false;

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
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		showHiddenOptions = event != null && event.getParameters().contains("dev");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		personalizedMetadataOnlyCheckBox = new CheckBox($("ExportView.personalizedMetadataOnly"));
		personalizedMetadataOnlyCheckBox.setValue(false);
		BaseButton exportSchemas = new BaseButton($("ExportView.schema")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.exportSchemasClicked(personalizedMetadataOnlyCheckBox.getValue());
			}
		};

		schemaLayout = new VerticalLayout(personalizedMetadataOnlyCheckBox, exportSchemas);
		schemaLayout.setSizeFull();
		schemaLayout.setSpacing(true);

		collectionOptions = new ListOptionGroup($("ExportView.collectionOption"));
		collectionOptions.addItem(OTHER_COLLECTION);
		collectionOptions.setItemCaption(OTHER_COLLECTION, $("ExportView.otherCollection"));
		collectionOptions.addItem(SAME_COLLECTION);
		collectionOptions.setItemCaption(SAME_COLLECTION, $("ExportView.sameCollection"));
		collectionOptions.setVisible(false);
		collectionOptions.setMultiSelect(false);
		collectionOptions.setNullSelectionAllowed(false);
		collectionOptions.setValue(OTHER_COLLECTION);

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

		if (presenter.hasCurrentCollectionRMModule()) {
			exportationOptions.addItem(TOOL_OPTION);
			exportationOptions.setItemCaption(TOOL_OPTION, $("ExportView.toolOption"));
			exportationOptions.addItem(FOLDER_AND_DOCUMENT_OPTION);
			exportationOptions.setItemCaption(FOLDER_AND_DOCUMENT_OPTION, $("ExportView.folderAndDocumentOption"));
			exportationOptions.addItem(ADMINISTRATIVE_UNIT_OPTION);
			exportationOptions.setItemCaption(ADMINISTRATIVE_UNIT_OPTION, $("ExportView.administrativeUnitOption"));
			initRMLayouts();
		}

		exportationOptions.addItem(IDS_OPTION);
		exportationOptions.setItemCaption(IDS_OPTION, $("ExportView.legacyIdsOption"));

		exportationOptions.addItem(SCHEMA_OPTION);
		exportationOptions.setItemCaption(SCHEMA_OPTION, $("ExportView.schemaOption"));

		if (showHiddenOptions) {
			exportationOptions.addItem(COMPLETE_OPTION);
			exportationOptions.setItemCaption(COMPLETE_OPTION, $("ExportView.completeOption"));
		}

		exportationOptions.addItem(OTHERS_OPTION);
		exportationOptions.setItemCaption(OTHERS_OPTION, $("ExportView.othersOption"));
		exportationOptions.setValue(OTHERS_OPTION);

		idsField = new BaseTextArea($("ExportView.exportedIds"));
		idsField.setWidth("100%");

		fastSaveStateCheckBox = new CheckBox($("ExportView.fastSaveState"));
		fastSaveStateCheckBox.setValue(true);

		exportWithoutContentsButton = new BaseButton($("ExportView.exportNoContents")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.exportWithoutContentsButtonClicked(fastSaveStateCheckBox.getValue());
			}
		};
		exportTools = new BaseButton($("ExportView.exportTools")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.exportToolsButtonClicked(fastSaveStateCheckBox.getValue());
			}
		};

		exportWithContentsButton = new BaseButton($("ExportView.exportAllContents")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.exportWithContentsButtonClicked(fastSaveStateCheckBox.getValue());
			}
		};
		exportLogs = new BaseButton($("ExportView.exportLogs")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.exportLogs();
			}
		};
		exportWithContentsButton.setVisible(false);

		othersLayout = new VerticalLayout(idsField, fastSaveStateCheckBox, exportWithoutContentsButton, exportWithContentsButton, exportTools,
				exportLogs);
		othersLayout.setSizeFull();
		othersLayout.setSpacing(true);

		mainLayout.addComponent(exportationOptions);
		if (presenter.hasCurrentCollectionRMModule()) {
			mainLayout.addComponents(collectionOptions, toolLayout, folderAndDocumentsLayout, idsLayout, administrativeUnitLayout,
					completeLayout);
		}

		Label emptyLabel = new Label("");
		emptyLabel.setHeight("1em");

		mainLayout.addComponents(emptyLabel, schemaLayout, othersLayout);

		return mainLayout;
	}

	private void initRMLayouts() {
		buildToolLayout();
		buildFolderAndDocumentLayout();
		buildAdministrativeUnitLayout();
		buildLegacyIdsLayout();
		buildCompleteLayout();
	}

	private Component buildCompleteLayout() {
		completeLayout = new VerticalLayout();
		completeLayout.setSizeFull();
		completeLayout.setSpacing(true);
		BaseButton exportComplete = new BaseButton($("ExportView.complete")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.exportCompleteClicked();
			}
		};
		completeLayout.addComponent(exportComplete);
		return completeLayout;
	}

	private Component buildToolLayout() {
		toolLayout = new VerticalLayout();
		toolLayout.setSizeFull();
		toolLayout.setSpacing(true);
		final CheckBox exportAuthorizationsCheckbox = new CheckBox($("ExportView.exportAuthorizationsCheckbox"));
		BaseButton exportTools = new BaseButton($("ExportView.exportTools")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				boolean includeAuthorizations = exportAuthorizationsCheckbox.getValue();
				if (SAME_COLLECTION.equals(collectionOptions.getValue())) {
					ConfirmDialog.show(ConstellioUI.getCurrent(), $("ExportView.confirmTitle"), buildConfirmMessage(), $("Ok"), $("cancel"), new ConfirmDialog.Listener() {
						@Override
						public void onClose(ConfirmDialog dialog) {
							if (dialog.isConfirmed()) {
								presenter.exportToolsToXMLButtonClicked(SAME_COLLECTION.equals(collectionOptions.getValue()), includeAuthorizations);
							}
						}
					});
				} else {
					presenter.exportToolsToXMLButtonClicked(SAME_COLLECTION.equals(collectionOptions.getValue()), includeAuthorizations);
				}
			}
		};
		toolLayout.addComponent(exportTools);
		toolLayout.addComponent(exportAuthorizationsCheckbox);
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
		final ListAddRemoveRecordLookupField containerField = new ListAddRemoveRecordLookupField(ContainerRecord.SCHEMA_TYPE);
		containerField.setCaption($("ExportView.containers"));
		BaseButton exportButton = new BaseButton($("ExportView.exportNoContents")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				if (SAME_COLLECTION.equals(collectionOptions.getValue())) {
					ConfirmDialog.show(ConstellioUI.getCurrent(), $("ExportView.confirmTitle"), buildConfirmMessage(), $("Ok"), $("cancel"), new ConfirmDialog.Listener() {
						@Override
						public void onClose(ConfirmDialog dialog) {
							if (dialog.isConfirmed()) {
								presenter.exportWithoutContentsXMLButtonClicked(SAME_COLLECTION.equals(collectionOptions.getValue()), folderField.getValue(), documentField.getValue(), containerField.getValue());
							}
						}
					});
				} else {
					presenter.exportWithoutContentsXMLButtonClicked(SAME_COLLECTION.equals(collectionOptions.getValue()), folderField.getValue(), documentField.getValue(), containerField.getValue());
				}
			}
		};
		folderAndDocumentsLayout.addComponents(folderField, documentField, containerField, exportButton);
		return folderAndDocumentsLayout;
	}

	private Component buildLegacyIdsLayout() {
		idsLayout = new VerticalLayout();
		idsLayout.setSizeFull();
		idsLayout.setSpacing(true);

		final TextField typeCodeField = new BaseTextField("type");
		typeCodeField.setImmediate(true);
		typeCodeField.setCaption($("ExportView.schemaType"));
		typeCodeField.setSizeFull();
		typeCodeField.setValue("folder");

		final TextArea textArea = new TextArea("legacyIds");
		textArea.setCaption($("ExportView.legacyIds"));
		textArea.setSizeFull();
		textArea.setRows(8);


		BaseButton exportButton = new BaseButton($("ExportView.exportNoContents")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				String textAreaValue = textArea.getValue();
				final String typeCode = typeCodeField.getValue();
				if (textAreaValue != null) {
					final List<String> ids = Arrays.asList(textAreaValue.split(","));
					presenter.exportWithoutContentsXMLButtonClicked(false, typeCode, ids);
				}
			}
		};
		idsLayout.addComponents(textArea, typeCodeField, exportButton);
		return idsLayout;
	}


	private Component buildAdministrativeUnitLayout() {
		administrativeUnitLayout = new VerticalLayout();
		administrativeUnitLayout.setSizeFull();
		administrativeUnitLayout.setSpacing(true);
		final ListAddRemoveRecordLookupField administrativeUnitField = new ListAddRemoveRecordLookupField(AdministrativeUnit.SCHEMA_TYPE);
		final CheckBox exportAuthorizationsCheckbox = new CheckBox($("ExportView.exportAuthorizationsCheckbox"));
		administrativeUnitField.setCaption($("ExportView.administrativeUnit"));
		BaseButton exportButton = new BaseButton($("ExportView.exportNoContents")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				boolean includeAuthorizations = exportAuthorizationsCheckbox.getValue();
				if (SAME_COLLECTION.equals(collectionOptions.getValue())) {
					ConfirmDialog.show(ConstellioUI.getCurrent(), $("ExportView.confirmTitle"), buildConfirmMessage(), $("Ok"), $("cancel"), new ConfirmDialog.Listener() {
						@Override
						public void onClose(ConfirmDialog dialog) {
							if (dialog.isConfirmed()) {
								presenter.exportAdministrativeUnitXMLButtonClicked(SAME_COLLECTION.equals(collectionOptions.getValue()), administrativeUnitField.getValue(), includeAuthorizations);
							}
						}
					});
				} else {
					presenter.exportAdministrativeUnitXMLButtonClicked(SAME_COLLECTION.equals(collectionOptions.getValue()), administrativeUnitField.getValue(), includeAuthorizations);
				}
			}
		};
		administrativeUnitLayout.addComponents(administrativeUnitField, exportButton);
		administrativeUnitLayout.addComponents(exportAuthorizationsCheckbox, exportButton);
		return administrativeUnitLayout;
	}

	private void adjustFields() {
		toolLayout.setVisible(false);
		folderAndDocumentsLayout.setVisible(false);
		administrativeUnitLayout.setVisible(false);
		schemaLayout.setVisible(false);
		othersLayout.setVisible(false);
		collectionOptions.setVisible(false);
		completeLayout.setVisible(false);

		idsLayout.setVisible(false);
		switch ((String) exportationOptions.getValue()) {
			case TOOL_OPTION:
				toolLayout.setVisible(true);
				collectionOptions.setVisible(true);
				break;
			case FOLDER_AND_DOCUMENT_OPTION:
				folderAndDocumentsLayout.setVisible(true);
				collectionOptions.setVisible(true);
				break;
			case ADMINISTRATIVE_UNIT_OPTION:
				administrativeUnitLayout.setVisible(true);
				collectionOptions.setVisible(true);
				break;
			case IDS_OPTION:
				idsLayout.setVisible(true);
				collectionOptions.setVisible(false);
				break;
			case SCHEMA_OPTION:
				schemaLayout.setVisible(true);
				break;
			case COMPLETE_OPTION:
				completeLayout.setVisible(true);
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

	private String buildConfirmMessage() {
		StringBuilder html = new StringBuilder();
		html.append("<span class=\"confirm-dialog-" + "warn" + "\">");
		html.append("<span class=\"confirm-dialog-message\">");
		html.append($("ExportView.confirmMessage"));
		html.append("</span>");
		html.append("</span>");
		return html.toString();
	}
}

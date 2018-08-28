package com.constellio.app.modules.rm.ui.components.document.newFile;

import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.model.entities.records.Content;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.Resource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class NewFileWindowImpl extends BaseWindow implements NewFileWindow {

	private boolean opened;

	private List<NewFileCreatedListener> newFileCreatedListeners = new ArrayList<>();

	private VerticalLayout mainLayout;

	private Label errorLabel;

	private LookupRecordField documentTypeField;

	private ComboBox extensionField;

	private ComboBox templateField;

	private HorizontalLayout labelAndTemplateLayout;

	private TextField fileNameField;

	private Button createFileButton;

	private NewFilePresenter presenter;

	public NewFileWindowImpl() {
		this(false);
	}

	public NewFileWindowImpl(boolean isViewOnly) {
		setModal(true);
		setWidth("70%");
		setHeight("340px");
		setZIndex(null);

		mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.setWidth("100%");

		String title = $("NewFileWindow.title");
		setCaption(title);

		errorLabel = new Label();
		errorLabel.addStyleName("error-label");
		errorLabel.setVisible(false);

		documentTypeField = new LookupRecordField(DocumentType.SCHEMA_TYPE);
		documentTypeField.setCaption($("NewFileWindow.documentType"));

		documentTypeField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				presenter.documentTypeIdSet(documentTypeField.getValue());
			}
		});

		extensionField = new BaseComboBox();
		extensionField.setCaption($("NewFileWindow.extension"));

		templateField = new BaseComboBox();
		templateField.setCaption($("NewFileWindow.templates"));

		extensionField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				presenter.extensionSet((String) extensionField.getValue());
			}
		});

		templateField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				presenter.templateSet((Content) templateField.getValue());
			}
		});

		Label label = new Label($("or"));

		labelAndTemplateLayout = new HorizontalLayout(label, templateField);
		labelAndTemplateLayout.setWidth("98%");
		labelAndTemplateLayout.setSpacing(true);
		labelAndTemplateLayout.setComponentAlignment(label, Alignment.BOTTOM_CENTER);

		HorizontalLayout extensionAndTemplateLayout = new HorizontalLayout(extensionField, labelAndTemplateLayout);
		extensionAndTemplateLayout.setSpacing(true);

		fileNameField = new BaseTextField();
		fileNameField.setCaption($("NewFileWindow.fileName"));
		fileNameField.setRequired(true);
		fileNameField.setWidth("98%");

		OnEnterKeyHandler onEnterHandler = new OnEnterKeyHandler() {
			@Override
			public void onEnterKeyPressed() {
				presenter.newFileNameSubmitted();
			}
		};
		onEnterHandler.installOn(fileNameField);

		createFileButton = new BaseButton($("NewFileWindow.createFile")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.newFileNameSubmitted();
			}
		};
		createFileButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setWidth("98%");
		buttonLayout.addComponent(createFileButton);
		buttonLayout.setSpacing(true);
		buttonLayout.setComponentAlignment(createFileButton, Alignment.MIDDLE_CENTER);

		setContent(mainLayout);
		mainLayout.addComponents(errorLabel, documentTypeField, extensionAndTemplateLayout, fileNameField, buttonLayout);

		presenter = new NewFilePresenter(this);
	}

	@Override
	public final String getFileName() {
		return fileNameField.getValue();
	}

	@Override
	public final String getExtension() {
		return (String) extensionField.getValue();
	}

	@Override
	public Content getTemplate() {
		return (Content) templateField.getValue();
	}

	@Override
	public void showErrorMessage(String key, Object... args) {
		errorLabel.setVisible(true);
		errorLabel.setValue($(key, args));
	}

	@Override
	public void setSupportedExtensions(List<String> extensions) {
		for (String extension : extensions) {
			String extensionCaption = $("NewFileWindow.supportedExtensions." + extension);
			extensionField.addItem(extension);
			extensionField.setItemCaption(extension, extensionCaption);
			Resource extensionIconResource = FileIconUtils.getIcon(extension);
			if (extensionIconResource != null) {
				extensionField.setItemIcon(extension, extensionIconResource);
			}
		}
	}

	@Override
	public void setTemplateOptions(List<Content> templates) {
		if (templates.isEmpty()) {
			labelAndTemplateLayout.setVisible(false);
		} else {
			labelAndTemplateLayout.setVisible(true);
			templateField.removeAllItems();
			for (Content template : templates) {
				templateField.addItem(template);
				templateField.setItemCaption(template, template.getCurrentVersion().getFilename());
			}
		}
	}

	@Override
	public void addNewFileCreatedListener(NewFileCreatedListener listener) {
		newFileCreatedListeners.add(listener);
	}

	@Override
	public void removeNewFileCreatedListener(NewFileCreatedListener listener) {
		newFileCreatedListeners.remove(listener);
	}

	@Override
	public void notifyNewFileCreated(Content content, String documentTypeId) {
		for (NewFileCreatedListener newFileCreatedListener : newFileCreatedListeners) {
			newFileCreatedListener.newFileCreated(content, documentTypeId);
		}
	}

	@Override
	public void open() {
		opened = true;
		extensionField.setValue(null);
		templateField.setValue(null);
		fileNameField.setValue(null);
		errorLabel.setVisible(false);
		extensionField.focus();

		boolean selectableTemplates = !templateField.getItemIds().isEmpty();
		documentTypeField.setVisible(documentTypeField.getValue() == null);
		labelAndTemplateLayout.setVisible(selectableTemplates);

		// Bugfix for windows opened twice because of ClassBasedViewProvider
		for (Window window : new ArrayList<>(UI.getCurrent().getWindows())) {
			if (window instanceof NewFileWindowImpl) {
				window.close();
			}
		}
		UI.getCurrent().addWindow(this);
	}

	@Override
	public void setDocumentTypeId(String documentTypeId) {
		presenter.documentTypeIdSet(documentTypeId);
		documentTypeField.setValue(documentTypeId);
	}

	@Override
	public void setExtensionFieldValue(String value) {
		extensionField.setValue(value);
	}

	@Override
	public void setTemplateFieldValue(String value) {
		templateField.setValue(value);
	}

	@Override
	public boolean isOpened() {
		return opened;
	}

	@Override
	public void close() {
		opened = false;
		super.close();
	}
}

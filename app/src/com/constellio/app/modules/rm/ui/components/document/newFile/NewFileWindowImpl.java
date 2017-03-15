package com.constellio.app.modules.rm.ui.components.document.newFile;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.model.entities.records.Content;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.Resource;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class NewFileWindowImpl extends BaseWindow implements NewFileWindow {

	private List<NewFileCreatedListener> newFileCreatedListeners = new ArrayList<>();

	private VerticalLayout mainLayout;

	private Label errorLabel;

	private ComboBox extensionField;

	private ComboBox templateField;

	private HorizontalLayout labelAndTemplateLayout;

	private TextField fileNameField;

	private Button createFileButton;

	private NewFilePresenter presenter;

	public NewFileWindowImpl() {
		setModal(true);
		setWidth("70%");
		setHeight("300px");
		setZIndex(null);

		mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.setWidth("100%");

		String title = $("NewFileWindow.title");
		setCaption(title);

		errorLabel = new Label();
		errorLabel.addStyleName("error-label");
		errorLabel.setVisible(false);

		extensionField = new ComboBox();
		extensionField.setCaption($("NewFileWindow.extension"));
		extensionField.setWidth("75%");

		templateField = new ComboBox();
		templateField.setCaption($("NewFileWindow.templates"));
		templateField.setWidth("75%");

		extensionField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				if (extensionField.getValue() != null) {
					templateField.setValue(null);
				}
			}
		});

		templateField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				if (templateField.getValue() != null) {
					extensionField.setValue(null);
				}
			}
		});

		Label label = new Label($("or"));

		labelAndTemplateLayout = new HorizontalLayout(label, templateField);
		labelAndTemplateLayout.setSpacing(true);
		labelAndTemplateLayout.setComponentAlignment(label, Alignment.BOTTOM_CENTER);

		HorizontalLayout extensionAndTemplateLayout = new HorizontalLayout(extensionField, labelAndTemplateLayout);
		extensionAndTemplateLayout.setWidth("100%");
		extensionAndTemplateLayout.setSpacing(true);

		fileNameField = new BaseTextField();
		fileNameField.setCaption($("NewFileWindow.fileName"));
		fileNameField.setRequired(true);
		fileNameField.setWidth("75%");

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
		buttonLayout.addComponent(createFileButton);
		buttonLayout.setSpacing(false);
		buttonLayout.setComponentAlignment(createFileButton, Alignment.BOTTOM_CENTER);

		setContent(mainLayout);
		mainLayout.addComponents(errorLabel, extensionAndTemplateLayout, fileNameField, buttonLayout);

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
	public void setTemplates(List<Content> templates) {
		if (templates.isEmpty()) {
			labelAndTemplateLayout.setVisible(false);
		} else {
			labelAndTemplateLayout.setVisible(true);
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
	public void notifyNewFileCreated(Content content) {
		for (NewFileCreatedListener newFileCreatedListener : newFileCreatedListeners) {
			newFileCreatedListener.newFileCreated(content);
		}
	}

	@Override
	public void open() {
		extensionField.setValue(null);
		templateField.setValue(null);
		fileNameField.setValue(null);
		errorLabel.setVisible(false);
		extensionField.focus();
		UI.getCurrent().addWindow(this);
	}

	@Override
	public void setDocumentTypeId(String documentTypeId) {
		presenter.setTemplatesByDocumentTypeId(documentTypeId);
	}

	public String getDocumentTypeId() {
		return presenter.getDocumentTypeId();
	}
}

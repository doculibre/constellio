package com.constellio.app.modules.rm.ui.components.document.newFile;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.model.entities.records.Content;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.Resource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class NewFileComponent extends CustomComponent {
	protected VerticalLayout mainLayout;

	private Label errorLabel;

	private LookupRecordField documentTypeField;

	private ComboBox extensionField;

	private ComboBox templateField;

	private HorizontalLayout labelAndTemplateLayout;

	private TextField fileNameField;

	private AppLayerFactory appLayerFactory;

	private NewFileComponentPresenter presenter;

	public NewFileComponent() {
		mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.setWidth("100%");

		String title = getMessage();
		Label labelTitle = null;
		if (title != null) {
			labelTitle = new Label(title);
		}

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
				extensionSet((String) extensionField.getValue());
			}
		});

		templateField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				templateSet((Content) templateField.getValue());
			}
		});

		Label label = new Label($("or"));

		labelAndTemplateLayout = new HorizontalLayout(label, templateField);
		labelAndTemplateLayout.setWidth("98%");
		labelAndTemplateLayout.setSpacing(true);
		labelAndTemplateLayout.setComponentAlignment(label, Alignment.BOTTOM_CENTER);

		HorizontalLayout extensionAndTemplateLayout = new HorizontalLayout(extensionField, labelAndTemplateLayout);
		extensionAndTemplateLayout.setSpacing(true);

		appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		mainLayout.addComponent(errorLabel);

		if (labelTitle != null) {
			mainLayout.addComponent(labelTitle);
		}

		mainLayout.addComponents(documentTypeField, extensionAndTemplateLayout);

		if (showFileNameField()) {
			fileNameField = new BaseTextField();
			fileNameField.setCaption($("NewFileWindow.fileName"));
			fileNameField.setRequired(true);
			fileNameField.setWidth("98%");

			mainLayout.addComponent(fileNameField);
		}

		presenter = new NewFileComponentPresenter(this);

		this.setCompositionRoot(mainLayout);
	}

	protected boolean showFileNameField() {
		return true;
	}

	public String getMessage() {
		return $("NewFileWindow.title");
	}

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

	public Layout getMainLayout() {
		return mainLayout;
	}

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

	public void clearValues() {
		extensionField.setValue(null);
		templateField.setValue(null);
		fileNameField.setValue(null);
		errorLabel.setVisible(false);
		extensionField.focus();

		boolean selectableTemplates = !templateField.getItemIds().isEmpty();
		documentTypeField.setVisible(documentTypeField.getValue() == null);
		labelAndTemplateLayout.setVisible(selectableTemplates);
	}

	void extensionSet(String value) {
		if (value != null) {
			setTemplateFieldValue((Content) null);
		}
	}

	void templateSet(Content value) {
		if (value != null) {
			setExtensionFieldValue(null);
		}
	}

	public void setDocumentTypeId(String documentTypeId) {
		presenter.documentTypeIdSet(documentTypeId);
		documentTypeField.setValue(documentTypeId);
	}

	public TextField getFileNameField() {
		return fileNameField;
	}

	public ComboBox getExtensionField() {
		return extensionField;
	}

	public ComboBox getTemplateField() {
		return templateField;
	}

	public final String getFileName() {
		return fileNameField.getValue();
	}

	public Field getDocumentTypeField() {
		return documentTypeField;
	}

	public final String getExtension() {
		return (String) extensionField.getValue();
	}

	public Content getTemplate() {
		return (Content) templateField.getValue();
	}

	public ContentVersionVO getTemplateVO() {
		Content content = (Content) templateField.getValue();

		if (content == null) {
			return null;
		}

		return presenter.getContentVO(content);
	}

	public void setExtensionFieldValue(String value) {
		extensionField.setValue(value);
	}

	public void setTemplateFieldValue(Content value) {
		templateField.setValue(value);
	}

	public void setTemplateFieldValue(ContentVersionVO value) {
		Content content = presenter.getContentFromVO(value);
		setTemplateFieldValue(content);
	}

	public String getDocumentTypeId() {
		return presenter.getDocumentTypeId();
	}

	public void showErrorMessage(String key, Object... args) {
		errorLabel.setVisible(true);
		errorLabel.setValue($(key, args));
	}

	public AppLayerFactory getAppLayerFactory() {
		return appLayerFactory;
	}
}

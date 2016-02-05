package com.constellio.app.modules.rm.ui.components.document.fields;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.ui.components.document.newFile.NewFileWindow;
import com.constellio.app.modules.rm.ui.components.document.newFile.NewFileWindowImpl;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.components.fields.upload.ContentVersionUploadField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.data.Property;

public class DocumentContentFieldImpl extends ContentVersionUploadField implements DocumentContentField {

	private Button newFileButton;

	private NewFileWindowImpl newFileWindow;

	private List<NewFileClickListener> newFileClickListeners = new ArrayList<>();

	private List<ContentUploadedListener> contentUploadedListeners = new ArrayList<>();

	public DocumentContentFieldImpl() {
		super();

		addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				for (ContentUploadedListener contentUploadedListener : contentUploadedListeners) {
					contentUploadedListener.newContentUploaded();
				}
			}
		});

		newFileButton = new Button($("DocumentContentField.newFile"));
		newFileButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				for (NewFileClickListener newFileClickListener : newFileClickListeners) {
					newFileClickListener.newFileClicked();
				}
			}
		});

		getMainLayout().addComponent(newFileButton, 0);
		getMainLayout().setComponentAlignment(newFileButton, Alignment.TOP_RIGHT);

		newFileWindow = new NewFileWindowImpl();
	}

	@Override
	public NewFileWindow getNewFileWindow() {
		return newFileWindow;
	}

	@Override
	public ContentVersionVO getFieldValue() {
		return (ContentVersionVO) getConvertedValue();
	}

	@Override
	public void setFieldValue(Object value) {
		setInternalValue(value);
	}

	@Override
	protected boolean isDeleteTempFilesOnDetach() {
		return false;
	}

	@Override
	public void setNewFileButtonVisible(boolean visible) {
		newFileButton.setVisible(visible);
	}

	@Override
	public void addNewFileClickListener(NewFileClickListener listener) {
		newFileClickListeners.add(listener);
	}

	@Override
	public void removeNewFileClickListener(NewFileClickListener listener) {
		newFileClickListeners.remove(listener);
	}

	@Override
	public void addContentUploadedListener(ContentUploadedListener listener) {
		contentUploadedListeners.add(listener);
	}

	@Override
	public void removeContentUploadedListener(ContentUploadedListener listener) {
		contentUploadedListeners.remove(listener);
	}

}

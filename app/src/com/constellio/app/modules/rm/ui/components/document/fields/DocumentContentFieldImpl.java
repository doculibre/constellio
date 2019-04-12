package com.constellio.app.modules.rm.ui.components.document.fields;

import com.constellio.app.modules.rm.ui.components.document.newFile.NewFileWindow;
import com.constellio.app.modules.rm.ui.components.document.newFile.NewFileWindowImpl;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.components.content.DownloadContentVersionLink;
import com.constellio.app.ui.framework.components.fields.upload.ContentVersionUploadField;
import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class DocumentContentFieldImpl extends ContentVersionUploadField implements DocumentContentField {

	private Button newFileButton;

	private NewFileWindowImpl newFileWindow;

	private List<NewFileClickListener> newFileClickListeners = new ArrayList<>();

	private List<ContentUploadedListener> contentUploadedListeners = new ArrayList<>();

	public DocumentContentFieldImpl() {
		this(false);
	}

	public DocumentContentFieldImpl(boolean isViewOnly) {
		super(false, !isViewOnly, isViewOnly);


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

		newFileButton.setVisible(!isViewOnly);

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
	public void setNewFileButtonVisible(boolean visible) {
		newFileButton.setVisible(visible);
	}

	@Override
	public void addNewFileClickListener(NewFileClickListener listener) {
		newFileClickListeners.add(listener);
	}

	@Override
	public void addNewFileClickListenerIfEmpty(NewFileClickListener listener) {
		if (newFileClickListeners.isEmpty()) {
			newFileClickListeners.add(listener);
		}
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

	@Override
	protected Component newItemCaption(Object itemId) {
		ContentVersionVO contentVersionVO = (ContentVersionVO) itemId;
		boolean majorVersionFieldVisible = isMajorVersionFieldVisible();
		return new ContentVersionUploadField.ContentVersionCaption(contentVersionVO, majorVersionFieldVisible) {
			@Override
			protected Component newCaptionComponent(ContentVersionVO contentVersionVO) {
				return new DownloadContentVersionLink(contentVersionVO, contentVersionVO.toString(false));
			}
		};
	}

}

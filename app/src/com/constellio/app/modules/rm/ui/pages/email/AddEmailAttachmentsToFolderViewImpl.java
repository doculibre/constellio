package com.constellio.app.modules.rm.ui.pages.email;

import static com.constellio.app.ui.i18n.i18n.$;
import com.constellio.app.modules.rm.ui.components.folder.fields.LookupFolderField;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;

public class AddEmailAttachmentsToFolderViewImpl extends BaseViewImpl implements AddEmailAttachmentsToFolderView {
	
	private String folderId;
	
	@PropertyId("folderId")
	private LookupFolderField folderField;
	
	private AddEmailAttachmentsToFolderPresenter presenter;

	public AddEmailAttachmentsToFolderViewImpl() {
		presenter = new AddEmailAttachmentsToFolderPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	public String getFolderId() {
		return folderId;
	}

	public void setFolderId(String folderId) {
		this.folderId = folderId;
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		folderField = new LookupFolderField();
		folderField.setCaption($("AddEmailAttachmentsToFolderView.folder"));
		folderField.setRequired(true);
		
		return new BaseForm<Object>(this, this, folderField) {
			@Override
			protected void saveButtonClick(Object viewObject)
					throws ValidationException {
				presenter.saveButtonClicked();
			}

			@Override
			protected void cancelButtonClick(Object viewObject) {
				presenter.cancelButtonClicked();
			}
		};
	}

	@Override
	protected String getTitle() {
		return $("AddEmailAttachmentsToFolderView.viewTitle");
	}

}

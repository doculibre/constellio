package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.modules.rm.ui.components.folder.FolderForm;
import com.constellio.app.modules.rm.ui.components.folder.FolderFormImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.CustomFolderField;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Buffered.SourceException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddEditFolderViewImpl extends BaseViewImpl implements AddEditFolderView {

	private RecordVO recordVO;

	private FolderFormImpl recordForm;

	private AddEditFolderPresenter presenter;

	private VerticalLayout mainComponent;

	public AddEditFolderViewImpl() {
		this(null);
	}

	public AddEditFolderViewImpl(RecordVO folderVO) {
		this(folderVO, false);
	}

	public AddEditFolderViewImpl(RecordVO folderVO, boolean popup) {
		presenter = newPresenter(folderVO);
	}

	protected AddEditFolderPresenter newPresenter(RecordVO folderVO) {
		return new AddEditFolderPresenter(this, folderVO);
	}

	public void setPresenter(AddEditFolderPresenter presenter) {
		this.presenter = presenter;
	}

	public AddEditFolderPresenter getPresenter() {
		return presenter;
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	public void afterViewAssembled(ViewChangeEvent event) {
		presenter.viewAssembled();
	}

	@Override
	public void setRecord(RecordVO recordVO) {
		this.recordVO = recordVO;
	}

	@Override
	protected String getTitle() {
		String titleKey;
		if (presenter.isAddView()) {
			if (presenter.isSubfolder()) {
				titleKey = "AddEditFolderView.addSubFolderViewTitle";
			} else {
				titleKey = "AddEditFolderView.addViewTitle";
			}
		} else {
			if (presenter.isSubfolder()) {
				titleKey = "AddEditFolderView.editSubFolderViewTitle";
			} else {
				titleKey = "AddEditFolderView.editViewTitle";
			}
		}
		return $(titleKey);
	}

	protected boolean showTab() {
		return true;
	}

	protected boolean validateRequiredFields() {
		return true;
	}

	protected boolean isSaveConfirmationMessage() {
		return true;
	}

	protected FolderFormImpl newForm() {
		recordForm = new FolderFormImpl(recordVO, getConstellioFactories()) {
			@Override
			protected void saveButtonClick(RecordVO viewObject) {
				presenter.saveButtonClicked();
			}

			@Override
			public void forceCancelSaveOfForm(ForceCancelSaveOfFormParams forceCancelSaveOfFormParams) {
				if (presenter.isMainCategoryEnteredAutomaticlyAssigned(forceCancelSaveOfFormParams.getRecord())) {
					forceCancelSaveOfFormParams.doNotShowConfirmationMessage();
				}
			}

			@Override
			public SaveAction showConfirmationMessage() {
				if (isSaveConfirmationMessage()) {
					return super.showConfirmationMessage();
				} else {
					return SaveAction.undefined;
				}
			}

			@Override
			public boolean validateFields() {
				if (validateRequiredFields()) {
					return true;
				} else {
					for (Field field : fieldGroup.getFields()) {
						field.setRequired(false);
						if (!field.isValid()) {
							return true;
						}
					}
					return false;
				}
			}

			@Override
			protected void cancelButtonClick(RecordVO viewObject) {
				presenter.cancelButtonClicked();
			}

			@Override
			public void reload() {
				((Layout) this.getParent()).replaceComponent(this, newForm());
			}

			@Override
			public void setFieldVisible(String metadataCode, boolean visible) {
				Field<?> field = recordForm.getField(metadataCode);
				if (field != null) {
					boolean wasVisible = field.isVisible();
					if (wasVisible != visible) {
						Layout layout = recordForm.getFieldLayout(field);

						if (layout != null) {
							field.setVisible(visible);
							layout.setVisible(visible);
						}
					}
				}
			}

			@Override
			protected String getTabCaption(Field<?> field, Object propertyId) {
				if (!showTab()) {
					return null;
				}

				return super.getTabCaption(field, propertyId);
			}



			@Override
			public void commit() {
				for (Field<?> field : fieldGroup.getFields()) {
					try {
						field.commit();
					} catch (SourceException | InvalidValueException e) {
					}
				}
			}
		};

		for (final Field<?> field : recordForm.getFields()) {
			if (field instanceof CustomFolderField) {
				field.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						presenter.customFieldValueChanged((CustomFolderField<?>) field);
					}
				});
			}
		}

		return recordForm;
	}


	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainComponent = new VerticalLayout();
		mainComponent.addStyleName("add-edit-folder-main-component");
		mainComponent.setWidth("100%");

		recordForm = newForm();
		mainComponent.addComponent(recordForm);
		return mainComponent;
	}

	@Override
	public FolderForm getForm() {
		return recordForm;
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

}

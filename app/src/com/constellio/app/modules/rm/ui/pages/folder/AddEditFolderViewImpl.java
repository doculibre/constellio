package com.constellio.app.modules.rm.ui.pages.folder;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.modules.rm.ui.components.folder.FolderForm;
import com.constellio.app.modules.rm.ui.components.folder.FolderFormImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.CustomFolderField;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Buffered.SourceException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;

public class AddEditFolderViewImpl extends BaseViewImpl implements AddEditFolderView {

	private RecordVO recordVO;

	private FolderFormImpl recordForm;

	private AddEditFolderPresenter presenter;

	public AddEditFolderViewImpl() {
		presenter = new AddEditFolderPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	protected void afterViewAssembled(ViewChangeEvent event) {
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
			titleKey = "AddEditFolderView.addViewTitle";
		} else {
			titleKey = "AddEditFolderView.editViewTitle";
		}
		return $(titleKey);
	}

	private FolderFormImpl newForm() {
		recordForm = new FolderFormImpl(recordVO) {
			@Override
			protected void saveButtonClick(RecordVO viewObject)
					throws ValidationException {
				presenter.saveButtonClicked();
			}

			@Override
			protected void cancelButtonClick(RecordVO viewObject) {
				presenter.cancelButtonClicked();
			}

			@Override
			public void reload() {
				replaceComponent(this, newForm());
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
		return newForm();
	}

	@Override
	public FolderForm getForm() {
		return recordForm;
	}

}

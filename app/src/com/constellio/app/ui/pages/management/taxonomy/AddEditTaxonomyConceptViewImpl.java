package com.constellio.app.ui.pages.management.taxonomy;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.framework.components.fields.upload.ContentVersionUploadField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("serial")
public class AddEditTaxonomyConceptViewImpl extends BaseViewImpl implements AddEditTaxonomyConceptView {

	AddEditTaxonomyConceptPresenter presenter;

	RecordVO recordVO;

	public AddEditTaxonomyConceptViewImpl() {
		this.presenter = new AddEditTaxonomyConceptPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forElementInTaxonomy(event.getParameters());
		recordVO = presenter.getRecordVO();
	}

	@Override
	protected String getTitle() {
		return $("AddEditTaxonomyConceptView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		RecordForm recordForm = new RecordForm(recordVO) {
			@Override
			protected void saveButtonClick(RecordVO recordVO)
					throws ValidationException {
				presenter.confirmBeforeSave(recordVO);
			}

			@Override
			protected void cancelButtonClick(RecordVO recordVO) {
				presenter.cancelButtonClicked(recordVO);
			}
		};

		for (final Field<?> field : recordForm.getFields()) {
			if (field instanceof ContentVersionUploadField) {
				field.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						presenter.contentVersionUploadFieldChanged((ContentVersionUploadField) field);
					}
				});
			}
		}

		return recordForm;
	}

}

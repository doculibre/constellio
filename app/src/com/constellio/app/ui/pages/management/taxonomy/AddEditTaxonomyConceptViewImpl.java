package com.constellio.app.ui.pages.management.taxonomy;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;

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
		return new RecordForm(recordVO) {
			@Override
			protected void saveButtonClick(RecordVO recordVO)
					throws ValidationException {
				presenter.saveButtonClicked(recordVO);
			}

			@Override
			protected void cancelButtonClick(RecordVO recordVO) {
				presenter.cancelButtonClicked(recordVO);
			}
		};
	}

}

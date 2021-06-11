package com.constellio.app.ui.pages.management.taxonomy;

import com.constellio.app.api.extensions.params.RecordFieldFactoryExtensionParams;
import com.constellio.app.modules.rm.wrappers.LegalRequirement;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.app.ui.framework.components.fields.upload.ContentVersionUploadField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.data.utils.dev.Toggle;
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
	RecordForm recordForm;

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

	private RecordFieldFactory getRecordFieldFactory() {
		RecordFieldFactoryExtensionParams params = new RecordFieldFactoryExtensionParams(getClass().getName(), null, recordVO);
		String collection = ConstellioUI.getCurrentSessionContext().getCurrentCollection();
		return ConstellioUI.getCurrent().getConstellioFactories().getAppLayerFactory().getExtensions().forCollection(collection).newRecordFieldFactory(params);
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {

		RecordFieldFactory formFieldFactory = getRecordFieldFactory();
		recordForm = new RecordForm(recordVO, formFieldFactory, getConstellioFactories()) {
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
			if (!Toggle.DISPLAY_LEGAL_REQUIREMENTS.isEnabled()
				&& field instanceof ListAddRemoveRecordLookupField
				&& LegalRequirement.SCHEMA_TYPE.equalsIgnoreCase(((ListAddRemoveRecordLookupField) field).getSchemaTypeCode())) {
				field.setVisible(false);
			}
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

	@Override
	public RecordForm getForm() {
		return recordForm;
	}
}

package com.constellio.app.ui.pages.management.schemaRecords;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.OverridingMetadataFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("serial")
public class AddEditSchemaRecordViewImpl extends BaseViewImpl implements AddEditSchemaRecordView {
	AddEditSchemaRecordPresenter presenter;
	RecordVO recordVO;
	RecordForm recordForm;

	public AddEditSchemaRecordViewImpl() {
		this(null, null);
	}

	public AddEditSchemaRecordViewImpl(RecordVO recordVO, String schemaType) {
		this.presenter = new AddEditSchemaRecordPresenter(this, recordVO, schemaType);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	public void setRecordVO(RecordVO recordVO) {
		this.recordVO = recordVO;
	}

	@Override
	public RecordForm getForm() {
		return this.recordForm;
	}

	@Override
	protected String getTitle() {
		return $("AddEditSchemaRecordView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		this.recordForm = new RecordForm(recordVO, new OverridingMetadataFieldFactory(presenter), getConstellioFactories()) {
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

		recordForm.getFields().stream()
				.filter(field -> presenter.filterFieldToListenTo(field))
				.forEach(field -> field.addValueChangeListener((ValueChangeListener) valueChangeEvent -> presenter.fieldValueChanged(field)));

		return recordForm;
	}
}

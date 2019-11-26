package com.constellio.app.ui.pages.management.schemaRecords;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.OverridingMetadataFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("serial")
public class AddEditSchemaRecordViewImpl extends BaseViewImpl implements AddEditSchemaRecordView {
	AddEditSchemaRecordPresenter presenter;
	RecordVO recordVO;

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
	protected String getTitle() {
		return $("AddEditSchemaRecordView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		return new RecordForm(recordVO, new OverridingMetadataFieldFactory(presenter)) {
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

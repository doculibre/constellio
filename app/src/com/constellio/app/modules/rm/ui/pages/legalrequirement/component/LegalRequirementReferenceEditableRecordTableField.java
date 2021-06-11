package com.constellio.app.modules.rm.ui.pages.legalrequirement.component;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.table.field.EditableRecordsTableField;
import com.constellio.model.services.records.RecordServicesException;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.function.Consumer;

import static com.constellio.app.ui.i18n.i18n.$;

public class LegalRequirementReferenceEditableRecordTableField extends EditableRecordsTableField {

	private LegalRequirementReferenceEditableRecordTablePresenter presenter;

	public LegalRequirementReferenceEditableRecordTableField(
			LegalRequirementReferenceEditableRecordTablePresenter presenter) {
		super(presenter);
		this.presenter = presenter;
		setCaption($("LegalRequirementManagement.legalRequirements"));
	}

	@Override
	public void createNewRecord(Consumer<RecordVO> newRecordCreatedCallback) {
		RecordVO record = presenter.newLegalRequirementReferenceRecordVO();
		WindowButton createButton = new AddEditRequirementReferenceWindow(record) {
			@Override
			public void saveButtonClicked(RecordVO recordVO) throws RecordServicesException {
				newRecordCreatedCallback.accept(presenter.updateRecord(recordVO));
				getWindow().close();
			}
		};
		createButton.click();
	}

	@Override
	public void editThisRecord(RecordVO record, Consumer<RecordVO> recordEditedCallback) {
		WindowButton editButton = new AddEditRequirementReferenceWindow(record) {
			@Override
			public void saveButtonClicked(RecordVO recordVO) throws RecordServicesException {
				recordEditedCallback.accept(presenter.updateRecord(recordVO));
				getWindow().close();
			}
		};
		editButton.click();
	}

	@Override
	public void thisRecordWillBeDeleted(RecordVO record, Consumer<RecordVO> deleteCallback) {
		BaseButton deleteButton = new DeleteButton() {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				deleteCallback.accept(record);
			}
		};
		deleteButton.click();
	}
}

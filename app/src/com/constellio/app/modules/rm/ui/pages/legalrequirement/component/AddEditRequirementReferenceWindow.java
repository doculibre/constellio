package com.constellio.app.modules.rm.ui.pages.legalrequirement.component;

import com.constellio.app.modules.rm.wrappers.LegalRequirementReference;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.model.services.records.RecordServicesException;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.VerticalLayout;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

@Slf4j
public abstract class AddEditRequirementReferenceWindow extends WindowButton {

	private RecordVO recordVO;

	public AddEditRequirementReferenceWindow(RecordVO recordVO) {
		super($("LegalRequirementManagement.addEditRequirementReference"),
				$("LegalRequirementManagement.addEditRequirementReference"),
				WindowConfiguration.modalDialog("650px", "380px"));

		this.recordVO = recordVO;
	}

	@Override
	protected Component buildWindowContent() {
		VerticalLayout mainLayout = new VerticalLayout();

		mainLayout.addComponent(buildRecordForm());

		return mainLayout;
	}

	private Component buildRecordForm() {
		return new RecordForm(recordVO, new LegalRequirementReferenceFieldFactory(), ConstellioFactories.getInstance()) {
			@Override
			protected void saveButtonClick(RecordVO viewObject) {
				try {
					saveButtonClicked(viewObject);
				} catch (RecordServicesException e) {
					log.error("Failed to save record", e);
					showErrorMessage($("AddEditRequirementReferenceWindow.couldNotSaveRecord"));
				}
			}

			@Override
			protected void cancelButtonClick(RecordVO viewObject) {
				getWindow().close();
			}
		};
	}

	private class LegalRequirementReferenceFieldFactory extends MetadataFieldFactory {
		@Override
		public Field<?> build(MetadataVO metadata, String recordId, Locale locale) {

			Field<?> field;
			switch (metadata.getLocalCode()) {
				case LegalRequirementReference.TITLE:
				case LegalRequirementReference.RULE_REQUIREMENT:
					field = null;
					break;
				default:
					field = super.build(metadata, recordId, locale);
			}

			return field;
		}
	}

	public abstract void saveButtonClicked(RecordVO recordVO) throws RecordServicesException;
}

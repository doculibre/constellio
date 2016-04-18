package com.constellio.app.modules.robots.ui.components.actionParameters;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class DynamicParametersField extends CustomField<String> {
	
	private final DynamicParametersPresenter presenter;
	private VerticalLayout layout;
	private Button button;

	private RecordVO record;
	
	private MetadataFieldFactory metadataFieldFactory;

	public DynamicParametersField(DynamicParametersPresenter presenter, MetadataFieldFactory metadataFieldFactory) {
		this.presenter = presenter;
		this.metadataFieldFactory = metadataFieldFactory;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (button != null) {
			button.setEnabled(enabled);
		}
	}

	public void resetWithRecord(RecordVO record) {
		this.record = record;
		layout.removeAllComponents();
		if (record == null) {
			initEmptyContent();
		} else {
			initRecordContent();
		}
		button.setEnabled(isEnabled());
	}

	@Override
	protected Component initContent() {
		layout = new VerticalLayout();
		layout.setSpacing(true);
		resetWithRecord(presenter.getDynamicParametersRecord());
		return layout;
	}

	@Override
	protected String getInternalValue() {
		return record != null ? record.getId() : null;
	}

	private void initEmptyContent() {
		button = buildEditButton();
		layout.addComponent(button);
	}

	private void initRecordContent() {
		button = buildEditButton();
		layout.addComponents(new RecordDisplay(record), button);
	}

	private WindowButton buildEditButton() {
		WindowButton button = new WindowButton(
				$("DynamicParametersField.editParametersButton"), $("DynamicRecordParametersField.editParametersWindow"),
				WindowConfiguration.modalDialog("75%", "75%")) {
			@Override
			protected Component buildWindowContent() {
				RecordVO effectiveRecord;
				if (record != null) {
					effectiveRecord = record;
				} else {
					effectiveRecord = presenter.newDynamicParametersRecord();
				}
				return new RecordForm(effectiveRecord, metadataFieldFactory) {
					@Override
					protected void saveButtonClick(RecordVO viewObject)
							throws ValidationException {
						if (presenter.saveParametersRecord(viewObject)) {
							getWindow().close();
						}
					}

					@Override
					protected void cancelButtonClick(RecordVO viewObject) {
						presenter.cancelParametersEdit(viewObject);
						getWindow().close();
					}
				};
			}
		};
		button.addStyleName(ValoTheme.BUTTON_LINK);
		return button;
	}

	@Override
	public Class<? extends String> getType() {
		return String.class;
	}

	public interface DynamicParametersPresenter {
		RecordVO getDynamicParametersRecord();

		RecordVO newDynamicParametersRecord();

		boolean saveParametersRecord(RecordVO viewObject);

		void cancelParametersEdit(RecordVO viewObject);
	}
}

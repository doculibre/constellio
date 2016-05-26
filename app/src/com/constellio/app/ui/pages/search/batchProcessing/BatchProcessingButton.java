package com.constellio.app.ui.pages.search.batchProcessing;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;

import java.util.List;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.pages.search.AdvancedSearchPresenter;
import com.constellio.app.ui.pages.search.AdvancedSearchView;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class BatchProcessingButton extends WindowButton {
	private AdvancedSearchPresenter presenter;
	private final AdvancedSearchView view;

	//fields
	LookupRecordField typeField;
	String currentSchema;
	BatchProcessingForm form;
	VerticalLayout vLayout;

	public BatchProcessingButton(AdvancedSearchPresenter presenter, AdvancedSearchView view) {
		super($("AdvancedSearchView.batchProcessing"), $("AdvancedSearchView.batchProcessing"),
				new WindowConfiguration(true, true,
						"75%", "75%"));
		this.presenter = presenter;
		this.view = view;
	}

	@Override
	protected Component buildWindowContent() {

		List<String> records = view.getSelectedRecordIds();
		if (!presenter.hasWriteAccessOnAllRecords(view.getSelectedRecordIds())) {
			return new Label($("AdvancedSearchView.requireWriteAccess"));
		}

		Panel panel = new Panel();
		vLayout = new VerticalLayout();
		String typeSchemaType = presenter.getTypeSchemaType(view.getSchemaType());
		typeField = new LookupRecordField(typeSchemaType);
		// FIXME All schemas don't have a type field
		typeField.setCaption($("BatchProcessingButton.type"));
		String originType = presenter.getOriginType(view.getSelectedRecordIds());
		if (originType != null) {
			typeField.setValue(originType);
		}
		typeField.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				refreshForm();
			}
		});
		vLayout.addComponent(typeField);
		form = newForm();
		vLayout.addComponent(form);

		panel.setContent(vLayout);
		panel.setSizeFull();
		return panel;
	}

	private void refreshForm() {
		BatchProcessingForm newForm = newForm();
		vLayout.replaceComponent(form, newForm);
		form = newForm;
	}

	private BatchProcessingForm newForm() {
		String selectedType = typeField.getValue();
		RecordFieldFactory fieldFactory = newFieldFactory(selectedType);
		String originSchema = presenter.getSchema(view.getSchemaType(), selectedType);
		return new BatchProcessingForm(presenter.newRecordVO(view.getSelectedRecordIds(), originSchema, view.getSessionContext()),
				fieldFactory);
	}

	private RecordFieldFactory newFieldFactory(String selectedType) {
		RecordFieldFactory fieldFactory = presenter.newRecordFieldFactory(selectedType);
		return new RecordFieldFactoryWithNoTypeNoContent(fieldFactory);
	}

	public class BatchProcessingForm extends RecordForm {
		Button simulateButton, processButton;

		public BatchProcessingForm(RecordVO record, RecordFieldFactory recordFieldFactory) {
			super(record, recordFieldFactory);
			simulateButton = new Button($("simulate"));
			simulateButton.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					form.commit();
					presenter.simulateButtonClicked(typeField.getValue(), viewObject);
				}
			});
			processButton = new Button($("process"));
			processButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
			processButton.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					form.commit();
					presenter.processBatchButtonClicked(typeField.getValue(), viewObject);
				}
			});
			buttonsLayout.addComponent(processButton);
			buttonsLayout.addComponentAsFirst(simulateButton);
			buttonsLayout.removeComponent(cancelButton);
			buttonsLayout.removeComponent(saveButton);
		}

		@Override
		protected void saveButtonClick(RecordVO viewObject)
				throws ValidationException {
		}

		@Override
		protected void cancelButtonClick(RecordVO viewObject) {
			getWindow().close();
		}

	}

	private class RecordFieldFactoryWithNoTypeNoContent extends RecordFieldFactory {
		final RecordFieldFactory fieldFactory;

		public RecordFieldFactoryWithNoTypeNoContent(RecordFieldFactory fieldFactory) {
			this.fieldFactory = fieldFactory;
		}

		@Override
		public Field<?> build(RecordVO recordVO, MetadataVO metadataVO) {
			if (metadataVO == null || metadataVO.getType().equals(CONTENT) || metadataVO.getLocalCode().equals("type")) {
				return null;
			}
			if (fieldFactory != null) {
				return fieldFactory.build(recordVO, metadataVO);
			}
			return super.build(recordVO, metadataVO);
		}
	}
}


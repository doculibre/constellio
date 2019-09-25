package com.constellio.app.ui.pages.search.batchProcessing;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.stream.DownloadStreamResource;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.records.RecordServicesException;
import com.vaadin.data.Property;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;

public class BatchProcessingButton extends WindowButton {
	private static final Logger LOGGER = LoggerFactory.getLogger(BatchProcessingButton.class);
	private BatchProcessingPresenter presenter;
	private final BatchProcessingView view;
	private boolean hasResultSelected;

	//fields
	LookupRecordField typeField;
	String currentSchema;
	BatchProcessingForm form;
	VerticalLayout vLayout;

	public BatchProcessingButton(BatchProcessingPresenter presenter, BatchProcessingView view) {
		super($("AdvancedSearchView.batchProcessing"), $("AdvancedSearchView.batchProcessing"),
				new WindowConfiguration(true, true,
						"75%", "75%"));
		this.presenter = presenter;
		this.view = view;
	}

	@Override
	protected Component buildWindowContent() {
		Component windowContent;
		if (!presenter.hasWriteAccessOnAllRecords(view.getSchemaType())) {
			windowContent = new Label($("AdvancedSearchView.requireWriteAccess"));
		} else if (presenter.isSearchResultsSelectionForm()) {
			windowContent = buildSearchResultsSelectionForm();
		} else {
			windowContent = buildBatchProcessingFormOrShowError();
		}
		return windowContent;
	}

	public BatchProcessingButton hasResultSelected(boolean value) {
		this.hasResultSelected = value;
		return this;
	}

	private Component buildSearchResultsSelectionForm() {
		getWindow().setHeight("220px");

		Panel panel = new Panel();
		vLayout = new VerticalLayout();
		vLayout.setSpacing(true);

		Label questionLabel = new Label($("AdvancedSearch.batchProcessingRecordSelection"));

		BaseButton allSearchResultsButton = new BaseButton($("AdvancedSearchView.allSearchResults")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.allSearchResultsButtonClicked();
				ValidationErrors validationErrors = presenter.validateBatchProcessing();
				if (!validationErrors.isEmpty()) {
					view.showErrorMessage($(validationErrors.getValidationErrors().get(0)));
					getWindow().close();
				}
				getWindow().setContent(buildBatchProcessingFormOrShowError());
				getWindow().setHeight(BatchProcessingButton.this.getConfiguration().getHeight());
				getWindow().setPosition(getWindow().getPositionX(), 30);
			}
		};

		BaseButton selectedSearchResultsButton = new BaseButton($("AdvancedSearchView.selectedSearchResults")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.selectedSearchResultsButtonClicked();
				ValidationErrors validationErrors = presenter.validateBatchProcessing();
				if (!validationErrors.isEmpty()) {
					view.showErrorMessage($(validationErrors.getValidationErrors().get(0)));
					getWindow().close();
				}
				getWindow().setContent(buildBatchProcessingFormOrShowError());
				getWindow().setHeight(BatchProcessingButton.this.getConfiguration().getHeight());
				getWindow().setPosition(getWindow().getPositionX(), 30);
			}
		};

		if (!hasResultSelected) {
			selectedSearchResultsButton.setEnabled(false);
		}

		vLayout.addComponents(questionLabel, allSearchResultsButton, selectedSearchResultsButton);

		panel.setContent(vLayout);
		panel.setSizeFull();
		return panel;
	}

	private Component buildBatchProcessingFormOrShowError() {
		if (!presenter.validateUserHaveBatchProcessPermissionOnAllRecords(view.getSchemaType())) {
			return new Label($("BatchProcess.batchProcessPermissionMissing"));
		}

		Panel panel = new Panel();
		vLayout = new VerticalLayout();
		vLayout.setSpacing(true);

		String typeSchemaType = presenter.getTypeSchemaType(view.getSchemaType());
		typeField = new LookupRecordField(typeSchemaType);
		// FIXME All schemas don't have a type field
		typeField.setCaption($("BatchProcessingButton.type"));
		String originType = presenter.getOriginType(view.getSchemaType());
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
		String selectedType = (String) typeField.getValue();
		RecordFieldFactory fieldFactory = newFieldFactory(selectedType);
		String originSchema = presenter.getSchema(view.getSchemaType(), selectedType);
		return new BatchProcessingForm(presenter.newRecordVO(originSchema, view.getSchemaType(), view.getSessionContext()),
				fieldFactory);
	}

	private RecordFieldFactory newFieldFactory(String selectedType) {
		RecordFieldFactory fieldFactory = presenter.newRecordFieldFactory(view.getSchemaType(), selectedType);
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
					BatchProcessingView batchProcessingView = BatchProcessingButton.this.view;

					try {
						InputStream inputStream = presenter.simulateButtonClicked((String) typeField.getValue(), view.getSchemaType(), viewObject);

						downloadBatchProcessingResults(inputStream);
					} catch (RecordServicesException.ValidationException e) {
						view.showErrorMessage($(e.getErrors()));
					} catch (Throwable e) {
						LOGGER.error("Unexpected error while executing batch process", e);
						batchProcessingView.showErrorMessage($(e.getMessage()));
					}
				}
			});
			processButton = new Button($("BatchProcessingButton.process", presenter.getNumberOfRecords(view.getSchemaType())));
			processButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
			processButton.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					form.commit();
					BatchProcessingView batchProcessingView = BatchProcessingButton.this.view;

					try {
						boolean success = presenter.processBatchButtonClicked((String) typeField.getValue(), view.getSchemaType(), viewObject);

						getWindow().close();

						if (success) {
							batchProcessingView.showMessage($("BatchProcessing.endedNormally"));
						}
					} catch (RecordServicesException.ValidationException e) {
						view.showErrorMessage($(e.getErrors()));
					} catch (Throwable e) {
						LOGGER.error("Unexpected error while executing batch process", e);
						batchProcessingView.showErrorMessage($(e.getMessage()));
					}
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

		private void downloadBatchProcessingResults(final InputStream stream) {
			Resource resource = new DownloadStreamResource(new StreamResource.StreamSource() {
				@Override
				public InputStream getStream() {
					return stream;
				}
			}, "results.xls");
			Page.getCurrent().open(resource, null, false);
		}

	}

	private class RecordFieldFactoryWithNoTypeNoContent extends RecordFieldFactory {
		final RecordFieldFactory fieldFactory;

		public RecordFieldFactoryWithNoTypeNoContent(RecordFieldFactory fieldFactory) {
			this.fieldFactory = fieldFactory;
		}

		@Override
		public Field<?> build(RecordVO recordVO, MetadataVO metadataVO, Locale locale) {
			if (metadataVO == null || metadataVO.getType().equals(CONTENT) || metadataVO.getLocalCode().equals("type")) {
				return null;
			}
			if (fieldFactory != null) {
				return fieldFactory.build(recordVO, metadataVO, locale);
			}
			return super.build(recordVO, metadataVO, locale);
		}
	}
}


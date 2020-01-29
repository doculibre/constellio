package com.constellio.app.ui.pages.search.batchProcessing;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.ConfirmDialogButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveStringLookupField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.stream.DownloadStreamResource;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.records.RecordServicesException;
import com.vaadin.data.Property;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
	ListAddRemoveStringLookupField metadatasToEmptyField;
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
			windowContent = buildBatchProcessingComponentOrShowError();
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
				getWindow().setContent(buildBatchProcessingComponentOrShowError());
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
				getWindow().setContent(buildBatchProcessingComponentOrShowError());
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

	private Component buildBatchProcessingComponentOrShowError() {
		if (!presenter.validateUserHaveBatchProcessPermissionOnAllRecords(view.getSchemaType())) {
			return new Label($("BatchProcess.batchProcessPermissionMissing"));
		}

		TabSheet tabSheet = new TabSheet();
		tabSheet.addTab(buildBatchProcessingModifyMetadataForm(), $("BatchProcess.tab.updateMetadata"));
		tabSheet.addTab(buildBatchProcessingEmptyMetadataForm(), $("BatchProcess.tab.emptyMetadata"));

		Component buttonLayout = buildBatchProcessingActions();

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.addComponent(tabSheet);
		mainLayout.addComponent(buttonLayout);
		mainLayout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_CENTER);

		return mainLayout;
	}

	private Component buildBatchProcessingModifyMetadataForm() {
		Panel panel = new Panel();
		vLayout = new VerticalLayout();
		vLayout.setSpacing(true);

		String typeSchemaType = presenter.getTypeSchemaType(view.getSchemaType());
		typeField = new LookupRecordField(typeSchemaType);
		// FIXME All schemas don't have a type field
		typeField.setCaption($("BatchProcessingButton.type"));
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
		String originSchema = presenter.getOriginSchema(view.getSchemaType(), selectedType);
		RecordFieldFactory fieldFactory = newFieldFactory();
		return new BatchProcessingForm(presenter.newRecordVO(originSchema, view.getSchemaType(), view.getSessionContext()),
				fieldFactory);
	}

	private RecordFieldFactory newFieldFactory() {
		RecordFieldFactory fieldFactory = presenter.newRecordFieldFactory(view.getSchemaType(), null);
		return new RecordFieldFactoryWithNoTypeNoContent(fieldFactory);
	}

	public class BatchProcessingForm extends RecordForm {
		public BatchProcessingForm(RecordVO record, RecordFieldFactory recordFieldFactory) {
			super(record, recordFieldFactory);

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
		public Field<?> build(RecordVO recordVO, MetadataVO metadataVO, Locale locale) {
			if (metadataVO == null || metadataVO.getType().equals(CONTENT)
				|| metadataVO.getLocalCode().equals("type")) {
				return null;
			}
			if (fieldFactory != null) {
				return fieldFactory.build(recordVO, metadataVO, locale);
			}
			return super.build(recordVO, metadataVO, locale);
		}
	}

	private Component buildBatchProcessingEmptyMetadataForm() {
		List<MetadataVO> metadatas = presenter.getMetadataAllowedInBatchEdit(view.getSchemaType());
		List<String> metadataCodes = metadatas.stream().map(MetadataVO::getLocalCode).collect(Collectors.toList());
		Collections.sort(metadataCodes);

		metadatasToEmptyField = new ListAddRemoveStringLookupField(metadataCodes);
		metadatasToEmptyField.setCaption($("BatchProcess.selectMetadatasToEmpty"));

		return metadatasToEmptyField;
	}

	private Component buildBatchProcessingActions() {
		Button simulateButton = new Button($("simulate"));
		simulateButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				form.commit();
				BatchProcessingView batchProcessingView = BatchProcessingButton.this.view;

				try {
					InputStream inputStream = presenter.simulateButtonClicked((String) typeField.getValue(),
							view.getSchemaType(), form.getViewObject(), metadatasToEmptyField.getValue());

					downloadBatchProcessingResults(inputStream);
				} catch (RecordServicesException.ValidationException e) {
					view.showErrorMessage($(e.getErrors()));
				} catch (Throwable e) {
					LOGGER.error("Unexpected error while executing batch process", e);
					batchProcessingView.showErrorMessage($(e.getMessage()));
				}
			}
		});

		ConfirmDialogButton processButton = new ConfirmDialogButton(
				$("BatchProcessingButton.process", presenter.getNumberOfRecords(view.getSchemaType()))) {
			@Override
			protected String getConfirmDialogMessage() {
				return $("BatchProcessingButton.confirm", presenter.getNumberOfRecords(view.getSchemaType()));
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				form.commit();
				BatchProcessingView batchProcessingView = BatchProcessingButton.this.view;

				try {
					boolean success = presenter.processBatchButtonClicked((String) typeField.getValue(),
							view.getSchemaType(), form.getViewObject(), metadatasToEmptyField.getValue());

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
		};
		processButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		HorizontalLayout actionsLayout = new HorizontalLayout();
		actionsLayout.setSpacing(true);
		actionsLayout.addComponent(processButton);
		actionsLayout.addComponentAsFirst(simulateButton);
		return actionsLayout;
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


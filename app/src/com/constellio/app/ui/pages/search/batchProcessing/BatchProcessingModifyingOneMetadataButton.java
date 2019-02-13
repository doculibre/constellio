package com.constellio.app.ui.pages.search.batchProcessing;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

public class BatchProcessingModifyingOneMetadataButton extends WindowButton {
	private MetadataFieldFactory factory;
	private HorizontalLayout valueArea;
	private ComboBox metadata;
	private Field value;
	private Button process;
	private BatchProcessingPresenter presenter;
	private final BatchProcessingView view;
	private boolean hasResultSelected;

	public BatchProcessingModifyingOneMetadataButton(BatchProcessingPresenter presenter, BatchProcessingView view) {
		super($("AdvancedSearchView.batchProcessing"), $("AdvancedSearchView.batchProcessing"));
		this.presenter = presenter;
		factory = new MetadataFieldFactory();
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
			windowContent = buildBatchProcessingForm();
		}
		return windowContent;
	}


	public BatchProcessingModifyingOneMetadataButton hasResultSelected(boolean value) {
		this.hasResultSelected = value;
		return this;
	}

	private Component buildSearchResultsSelectionForm() {
		getWindow().setHeight("220px");

		VerticalLayout layout = new VerticalLayout(buildMetadataComponent());

		Panel panel = new Panel();
		layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);

		Label questionLabel = new Label($("AdvancedSearch.batchProcessingRecordSelection"));

		BaseButton allSearchResultsButton = new BaseButton($("AdvancedSearchView.allSearchResults")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.allSearchResultsButtonClicked();
				getWindow().setContent(buildBatchProcessingForm());
				getWindow().setHeight(BatchProcessingModifyingOneMetadataButton.this.getConfiguration().getHeight());
				getWindow().center();
			}
		};

		BaseButton selectedSearchResultsButton = new BaseButton($("AdvancedSearchView.selectedSearchResults")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.selectedSearchResultsButtonClicked();
				getWindow().setContent(buildBatchProcessingForm());
				getWindow().setHeight(BatchProcessingModifyingOneMetadataButton.this.getConfiguration().getHeight());
				getWindow().center();
			}
		};


		if (!hasResultSelected) {
			selectedSearchResultsButton.setEnabled(false);
		}

		layout.addComponents(questionLabel, allSearchResultsButton, selectedSearchResultsButton);

		panel.setContent(layout);
		panel.setSizeFull();
		return panel;
	}

	private Component buildBatchProcessingForm() {
		if (!presenter.validateUserHaveBatchProcessPermissionOnAllRecords(view.getSchemaType())) {
			return new Label($("BatchProcess.batchProcessPermissionMissing"));
		}

		Label label = new Label($("AdvancedSearchView.batchProcessValue"));
		value = null;

		valueArea = new HorizontalLayout(label);
		valueArea.setSpacing(true);

		process = new Button($("AdvancedSearchView.batchProcessStart"), new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				MetadataVO metadataVO = (MetadataVO) metadata.getValue();
				presenter.batchEditRequested(metadataVO.getCode(),
						((AbstractField) value).getConvertedValue(), view.getSchemaType());
				getWindow().close();
				view.showMessage($("BatchProcessing.endedNormally"));
			}
		});
		process.addStyleName(ValoTheme.BUTTON_PRIMARY);

		VerticalLayout layout = new VerticalLayout(buildMetadataComponent(), valueArea, process);
		layout.setComponentAlignment(process, Alignment.MIDDLE_RIGHT);
		layout.setSpacing(true);
		return layout;
	}

	private Component buildMetadataComponent() {
		Label label = new Label($("AdvancedSearchView.batchProcessField"));

		metadata = new ComboBox();
		metadata.setItemCaptionMode(AbstractSelect.ItemCaptionMode.EXPLICIT);
		metadata.setNullSelectionAllowed(false);
		for (MetadataVO metadata : presenter.getMetadataAllowedInBatchEdit(view.getSchemaType())) {
			this.metadata.addItem(metadata);
			this.metadata.setItemCaption(metadata, metadata.getLabel());
		}
		metadata.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				if (value != null) {
					valueArea.removeComponent(value);
				}
				value = buildValueField();
				valueArea.addComponent(value);
			}
		});

		HorizontalLayout layout = new HorizontalLayout(label, metadata);
		layout.setSpacing(true);
		return layout;
	}

	private Field buildValueField() {
		final Field field = factory.build((MetadataVO) metadata.getValue());
		field.setCaption(null);
		field.setWidthUndefined();
		field.setPropertyDataSource(new ObjectProperty<>(null, Object.class));
		return field;
	}
}

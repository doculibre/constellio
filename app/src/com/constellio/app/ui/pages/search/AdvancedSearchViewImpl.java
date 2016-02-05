package com.constellio.app.ui.pages.search;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.buttons.LabelsButton;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.framework.components.ReportSelector;
import com.constellio.app.ui.framework.components.ReportViewer.DownloadStreamResource;
import com.constellio.app.ui.framework.components.SearchResultTable;
import com.constellio.app.ui.pages.base.ConstellioHeader;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.data.utils.Factory;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class AdvancedSearchViewImpl extends SearchViewImpl<AdvancedSearchPresenter> implements AdvancedSearchView {
	public static final String BATCH_PROCESS_BUTTONSTYLE = "searchBatchProcessButton";
	public static final String LABELS_BUTTONSTYLE = "searchLabelsButton";

	private final ConstellioHeader header;

	public AdvancedSearchViewImpl() {
		presenter = new AdvancedSearchPresenter(this);
		presenter.resetFacetAndOrder();
		header = ConstellioUI.getCurrent().getHeader();
	}

	@Override
	public List<Criterion> getSearchCriteria() {
		return header.getAdvancedSearchCriteria();
	}

	@Override
	public void setSearchCriteria(List<Criterion> criteria) {
		header.setAdvancedSearchCriteria(criteria);
	}

	@Override
	public String getSchemaType() {
		return header.getAdvancedSearchSchemaType();
	}

	@Override
	public void setSchemaType(String schemaTypeCode) {
		header.selectAdvancedSearchSchemaType(schemaTypeCode);
	}

	@Override
	public String getSearchExpression() {
		return header.getSearchExpression();
	}

	@Override
	protected Component buildSearchUI() {
		return new VerticalLayout();
	}

	@Override
	protected Component buildSummary(SearchResultTable results) {
		// TODO: Create an extension for this

		String schemaType = getSchemaType();
		List<Component> selectionActions = new ArrayList<>();

		WindowButton batchProcess = new BatchProcessingButton();
		batchProcess.addStyleName(ValoTheme.BUTTON_LINK);
		batchProcess.addStyleName(BATCH_PROCESS_BUTTONSTYLE);
		selectionActions.add(batchProcess);

		if (schemaType.equals(Folder.SCHEMA_TYPE) || schemaType.equals(ContainerRecord.SCHEMA_TYPE)) {
			Factory<List<LabelTemplate>> labelTemplatesFactory = new Factory<List<LabelTemplate>>() {
				@Override
				public List<LabelTemplate> get() {
					return presenter.getTemplates();
				}
			};
			LabelsButton labelsButton = new LabelsButton($("SearchView.labels"), $("SearchView.printLabels"), this,
					labelTemplatesFactory);
			labelsButton.addStyleName(ValoTheme.BUTTON_LINK);
			labelsButton.addStyleName(LABELS_BUTTONSTYLE);
			selectionActions.add(labelsButton);
		}

		if (schemaType.equals(Document.SCHEMA_TYPE)) {
			Component zipButton = new Link($("ReportViewer.download", "(zip)"),
					new DownloadStreamResource(presenter.getZippedContents(), presenter.getZippedContentsFilename()));
			zipButton.addStyleName(ValoTheme.BUTTON_LINK);
			selectionActions.add(zipButton);
		}

		if (schemaType.equals(Folder.SCHEMA_TYPE) || schemaType.equals(Document.SCHEMA_TYPE) ||
				schemaType.equals(ContainerRecord.SCHEMA_TYPE)) {
			Button addToCart = new LinkButton($("SearchView.addToCart")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.addToCartRequested(getSelectedRecordIds());
				}
			};
			selectionActions.add(addToCart);
		}

		List<Component> actions = Arrays.asList(
				buildSelectAllButton(), buildSavedSearchButton(), (Component) new ReportSelector(presenter));

		return results.createSummary(actions, selectionActions);
	}

	@Override
	public Boolean computeStatistics() {
		return presenter.computeStatistics();
	}

	private class BatchProcessingButton extends WindowButton {
		private MetadataFieldFactory factory;
		private HorizontalLayout valueArea;
		private ComboBox metadata;
		private Field value;
		private Button process;

		public BatchProcessingButton() {
			super($("AdvancedSearchView.batchProcessing"), $("AdvancedSearchView.batchProcessing"));
			factory = new MetadataFieldFactory();
		}

		@Override
		protected Component buildWindowContent() {
			Label label = new Label($("AdvancedSearchView.batchProcessValue"));
			value = null;

			valueArea = new HorizontalLayout(label);
			valueArea.setSpacing(true);

			process = new Button($("AdvancedSearchView.batchProcessStart"), new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					MetadataVO metadataVO = (MetadataVO) metadata.getValue();
					presenter.batchEditRequested(getSelectedRecordIds(), metadataVO.getCode(),
							((AbstractField) value).getConvertedValue());
					getWindow().close();
					showMessage($("AdvancedSearchView.batchProcessConfirm"));
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
			metadata.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
			metadata.setNullSelectionAllowed(false);
			for (MetadataVO metadata : presenter.getMetadataAllowedInBatchEdit()) {
				this.metadata.addItem(metadata);
				this.metadata.setItemCaption(metadata, metadata.getLabel());
			}
			metadata.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
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

}

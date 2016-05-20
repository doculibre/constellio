package com.constellio.app.ui.pages.search;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.buttons.LabelsButton;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.ReportSelector;
import com.constellio.app.ui.framework.components.ReportViewer.DownloadStreamResource;
import com.constellio.app.ui.framework.components.SearchResultTable;
import com.constellio.app.ui.pages.base.ConstellioHeader;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingButton;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingModifyingOneMetadataButton;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.enums.BatchProcessingMode;
import com.github.rjeschke.txtmark.Run;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.enums.BatchProcessingMode.ALL_METADATA_OF_SCHEMA;
import static com.constellio.model.entities.enums.BatchProcessingMode.ONE_METADATA;

public class AdvancedSearchViewImpl extends SearchViewImpl<AdvancedSearchPresenter> implements AdvancedSearchView {
	public static final String BATCH_PROCESS_BUTTONSTYLE = "searchBatchProcessButton";
	public static final String LABELS_BUTTONSTYLE = "searchLabelsButton";

	private final ConstellioHeader header;
	private WindowButton batchProcess;

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
	public void downloadBatchProcessingResults(final InputStream stream) {
		Resource resource = new DownloadStreamResource(new StreamResource.StreamSource() {
			@Override
			public InputStream getStream() {
				return stream;
			}
		}, "results.xls");
		Page.getCurrent().open(resource, null, false);
	}

	@Override
	public void closeBatchProcessingWindow() {
		batchProcess.getWindow().close();
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

		batchProcess = newBatchProcessingButton();
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

	private WindowButton newBatchProcessingButton() {
		BatchProcessingMode mode = presenter.getBatchProcessingMode();
		if(mode.equals(ALL_METADATA_OF_SCHEMA)){
			return new BatchProcessingButton(presenter, this);
		}else if (mode.equals(ONE_METADATA)){
			return new BatchProcessingModifyingOneMetadataButton(presenter, this);
		}else{
			throw new RuntimeException("Unsupported mode " + mode);
		}
	}

	@Override
	public Boolean computeStatistics() {
		return presenter.computeStatistics();
	}

}

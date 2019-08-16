package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.ui.pages.decommissioning.component.AddExistingContainerRecordListMenuBar;
import com.constellio.app.modules.rm.ui.pages.decommissioning.component.AddExistingContainerRecordListMenuBar.MenuBarItemAdder;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.ui.framework.components.SearchResultTable;
import com.constellio.app.ui.framework.components.menuBar.RecordListMenuBar;
import com.constellio.app.ui.pages.search.AdvancedSearchCriteriaComponent;
import com.constellio.app.ui.pages.search.SearchViewImpl;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.model.entities.records.Record;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddExistingContainerViewImpl extends SearchViewImpl<AddExistingContainerPresenter>
		implements AddExistingContainerView {
	private AdvancedSearchCriteriaComponent criteria;

	public AddExistingContainerViewImpl() {
		presenter = new AddExistingContainerPresenter(this);
		presenter.resetFacetAndOrder();
		criteria = new AdvancedSearchCriteriaComponent(presenter);
	}

	@Override
	protected String getTitle() {
		return $("AddExistingContainerView.viewTitle");
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
	}

	@Override
	public void addEmptyCriterion() {
		criteria.addEmptyCriterion();
	}

	@Override
	public void setCriteriaSchemaType(String schemaType) {
		criteria.setSchemaType(schemaType);
	}

	@Override
	public void setSearchCriteria(List<Criterion> criteria) {
		this.criteria.setSearchCriteria(criteria);
	}

	@Override
	public void setAdministrativeUnit(String administrativeUnitID) {
	}

	@Override
	public List<Criterion> getSearchCriteria() {
		return criteria.getSearchCriteria();
	}

	@Override
	protected Component buildSearchUI() {
		Button addCriterion = new Button($("AddExistingContainerView.addCriterion"));
		addCriterion.addStyleName(ValoTheme.BUTTON_LINK);
		addCriterion.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.addCriterionRequested();
			}
		});

		criteria.setWidth("100%");

		Button searchButton = new Button($("AddExistingContainerView.search"));
		searchButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		searchButton.setEnabled(true);
		searchButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.searchRequested();
			}
		});

		VerticalLayout searchUI = new VerticalLayout(addCriterion, criteria, searchButton);
		searchUI.addStyleName("add-existing-container-search-ui");
		searchUI.setComponentAlignment(addCriterion, Alignment.MIDDLE_RIGHT);
		searchUI.setSpacing(true);

		return searchUI;
	}

	@Override
	protected Component buildSummary(SearchResultTable results) {
		Button addContainers = new Button($("AddExistingContainerView.addContainers"));
		addContainers.addStyleName(ValoTheme.BUTTON_LINK);
		addContainers.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.containerAdditionRequested(getSelectedRecordIds());
			}
		});

		return results.createSummary(buildSelectAllButton(), buildAddToSelectionButton(), addContainers);
	}

	@Override
	public Boolean computeStatistics() {
		return false;
	}

	public RecordListMenuBar getRecordListMenuBar() {
		return new AddExistingContainerRecordListMenuBar($("ViewableRecordVOTablePanel.selectionActions"), new MenuBarItemAdder() {
			@Override
			public void addMenuBarItems(MenuItem rootItem, List<Record> selectedRecords) {
				MenuBar.MenuItem menuItem = rootItem.addItem($("AddExistingContainerView.addContainers"), new Command() {
					@Override
					public void menuSelected(MenuItem selectedItem) {
						presenter.containerAdditionRequested(selectedRecords.stream().map(record -> record.getId()).collect(Collectors.toList()));
					}
				});
				menuItem.setVisible(selectedRecords == null || selectedRecords.isEmpty() || allRecordAreContainers(selectedRecords));
				menuItem.setEnabled(selectedRecords != null && selectedRecords.size() > 0);
			}

		});
	}

	private boolean allRecordAreContainers(List<Record> records) {
		return records.stream().allMatch(record -> record.getSchemaCode().startsWith(ContainerRecord.SCHEMA_TYPE));
	}
}

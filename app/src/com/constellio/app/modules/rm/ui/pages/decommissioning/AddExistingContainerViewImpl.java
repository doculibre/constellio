package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.ui.framework.components.selection.SelectionComponent.SelectionChangeEvent;
import com.constellio.app.ui.framework.components.selection.SelectionComponent.SelectionChangeListener;
import com.constellio.app.ui.pages.search.AdvancedSearchCriteriaComponent;
import com.constellio.app.ui.pages.search.SearchViewImpl;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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

	@NotNull
	private Button buildAddExistingContainerButton() {
		Button addContainers = new Button($("AddExistingContainerView.addContainers"));
		addContainers.addStyleName(ValoTheme.BUTTON_LINK);
		addContainers.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.containerAdditionRequested(getSelectedRecordIds());
			}
		});

		this.addSelectionChangeListener(new SelectionChangeListener() {
			@Override
			public void selectionChanged(SelectionChangeEvent event) {
				addContainers.setEnabled(event.isAllItemsSelected() || (event != null && !event.getSelectedItemIds().isEmpty()));
			}
		});

		return addContainers;
	}

	@Override
	protected List<Button> getQuickActionMenuButtons() {
		List<Button> listButton = new ArrayList<>(super.getQuickActionMenuButtons());

		Button addContainerButton = buildAddExistingContainerButton();
		addContainerButton.setEnabled(!this.getSelectedRecordIds().isEmpty());
		listButton.add(0, addContainerButton);
		return listButton;
	}

	@Override
	public boolean isSelectionActionMenuBar() {
		return false;
	}

	@Override
	public Boolean computeStatistics() {
		return false;
	}
}

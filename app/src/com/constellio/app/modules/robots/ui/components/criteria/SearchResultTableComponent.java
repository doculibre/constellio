package com.constellio.app.modules.robots.ui.components.criteria;

import com.constellio.app.modules.robots.ui.pages.AddEditRobotPresenter;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.RecordDisplayFactory;
import com.constellio.app.ui.framework.components.SearchResultDetailedTable;
import com.constellio.app.ui.framework.containers.SearchResultContainer;
import com.constellio.app.ui.framework.containers.SearchResultVOLazyContainer;
import com.constellio.app.ui.pages.search.AdvancedSearchCriteriaComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

public class SearchResultTableComponent extends CustomComponent {

	private AddEditRobotPresenter presenter;
	private AdvancedSearchCriteriaComponent component;

	private VerticalLayout mainLayout;
	private VerticalLayout verticalLayoutTable;
	private UserVO currentUserVO;

	public SearchResultTableComponent(AddEditRobotPresenter presenter, AdvancedSearchCriteriaComponent component,
									  UserVO currentUserVO) {
		this.presenter = presenter;
		this.component = component;
		this.currentUserVO = currentUserVO;
		buildComponent(presenter);
	}

	private void buildComponent(final AddEditRobotPresenter presenter) {
		mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.setWidth("100%");

		Button testCriterion = new Button($("SearchResultTableComponent.testButtonCaption"));
		testCriterion.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				if (presenter.getSchemaFilter() != null && component.getSearchCriteria() != null && !component.getSearchCriteria()
						.isEmpty()) {
					buildResultTable();
				}
			}
		});
		testCriterion.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_RIGHT);

		verticalLayoutTable = new VerticalLayout();

		mainLayout.addComponents(testCriterion, verticalLayoutTable);
		mainLayout.setComponentAlignment(testCriterion, Alignment.TOP_RIGHT);
		setCompositionRoot(mainLayout);
	}

	private void buildResultTable() {
		verticalLayoutTable.removeAllComponents();
		SearchResultDetailedTable newTable = new SearchResultDetailedTable(buildResultContainer(), false, false) {
			@Override
			protected void onPreviousPageButtonClicked() {
				super.onPreviousPageButtonClicked();
			}

			@Override
			protected void onNextPageButtonClicked() {
				super.onNextPageButtonClicked();
			}

			@Override
			protected void onSetPageButtonClicked(int page) {
				super.onSetPageButtonClicked(page);
			}
		};
		newTable.setWidth("100%");
		verticalLayoutTable.addComponents(newTable, newTable.createControls());
	}

	private SearchResultContainer buildResultContainer() {
		RecordDisplayFactory displayFactory = new RecordDisplayFactory(currentUserVO, null);
		SearchResultVOLazyContainer results = new SearchResultVOLazyContainer(
				presenter.getSearchResults(component.getSearchCriteria()));

		return new SearchResultContainer(results, displayFactory, "");
	}

}

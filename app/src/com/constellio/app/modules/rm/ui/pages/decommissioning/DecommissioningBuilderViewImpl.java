package com.constellio.app.modules.rm.ui.pages.decommissioning;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningListParams;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.SearchResultTable;
import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.pages.search.AdvancedSearchCriteriaComponent;
import com.constellio.app.ui.pages.search.SearchViewImpl;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class DecommissioningBuilderViewImpl extends SearchViewImpl<DecommissioningBuilderPresenter>
		implements DecommissioningBuilderView {
	public static final String FILING_SPACE = "filing-space";
	public static final String ADMIN_UNIT = "admin-unit";
	public static final String SEARCH = "search";
	public static final String CREATE_LIST = "create-list";

	private AdvancedSearchCriteriaComponent criteria;
	private Button searchButton;
	private Button addToListButton;
	private LookupRecordField adminUnit;

	public DecommissioningBuilderViewImpl() {
		presenter = new DecommissioningBuilderPresenter(this);
		presenter.resetFacetAndOrder();
		criteria = new AdvancedSearchCriteriaComponent(presenter);

		adminUnit = new LookupRecordField(AdministrativeUnit.SCHEMA_TYPE) {
			@Override
			public boolean isEnabled() {
				return presenter.isAddMode();
			}
		};
		addStyleName("search-decommissioning");
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
		super.initBeforeCreateComponents(event);
	}

	@Override
	protected String getTitle() {
		SearchType type = presenter.getSearchType();
		return $("DecommissioningBuilderView.viewTitle." + type);
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				if(presenter.isAddMode()) {
					navigate().to(RMViews.class).decommissioning();
				} else if (presenter.getDecommissioningList().getDecommissioningListType().isFolderList()) {
					navigate().to(RMViews.class).displayDecommissioningList(presenter.getDecommissioningList().getId());
				} else {
					navigate().to(RMViews.class).displayDocumentDecommissioningList(presenter.getDecommissioningList().getId());
				}
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
		this.adminUnit.setValue(administrativeUnitID);
	}

	@Override
	public List<Criterion> getSearchCriteria() {
		return criteria.getSearchCriteria();
	}

	@Override
	protected Component buildSearchUI() {
		Button addCriterion = new Button($("DecommissioningBuilderView.addCriterion"));
		addCriterion.addStyleName(ValoTheme.BUTTON_LINK);
		addCriterion.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.addCriterionRequested();
			}
		});

		HorizontalLayout top = new HorizontalLayout(buildAdministrativeUnitComponent(), addCriterion);
		top.setComponentAlignment(addCriterion, Alignment.BOTTOM_RIGHT);
		top.setWidth("100%");

		criteria.setWidth("100%");

		searchButton = new Button($("DecommissioningBuilderView.search"));
		searchButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		searchButton.addStyleName(SEARCH);
		searchButton.setEnabled(presenter.mustDisplayResults());
		searchButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.searchRequested();
			}
		});

		VerticalLayout searchUI = new VerticalLayout(top, criteria, searchButton);
		searchUI.setSpacing(true);

		searchButton.setEnabled(adminUnit.getValue() != null);

		return searchUI;
	}

	@Override
	protected Component buildSummary(SearchResultTable results) {
		Button createList = new DecommissioningButton($("DecommissioningBuilderView.createDecommissioningList"));
		createList.addStyleName(ValoTheme.BUTTON_LINK);
		createList.addStyleName(CREATE_LIST);
		return results.createSummary(buildSelectAllButton(), buildAddToSelectionButton(), createList, buildAddToListButton());
	}

	private Component buildAdministrativeUnitComponent() {
		Label label = new Label($("DecommissioningBuilderView.administrativeUnit"));
		adminUnit.setRequired(true);
		adminUnit.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				searchButton.setEnabled(adminUnit.getValue() != null);
				presenter.administrativeUnitSelected(adminUnit.getValue());
				presenter.saveTemporarySearch(false);
			}
		});
		adminUnit.addStyleName(ADMIN_UNIT);

		HorizontalLayout layout = new HorizontalLayout(label, adminUnit);
		layout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
		layout.setSpacing(true);
		return layout;
	}

	@Override
	public Boolean computeStatistics() {
		return false;
	}

	public class DecommissioningButton extends WindowButton {
		public static final String TITLE = "dl-title";
		public static final String DESCRIPTION = "dl-description";

		@PropertyId("title") private BaseTextField title;
		@PropertyId("description") private BaseTextArea description;

		public DecommissioningButton(String caption) {
			super(caption, caption);
		}

		@Override
		protected Component buildWindowContent() {
			title = new BaseTextField($("DecommissioningBuilderView.title"));
			title.setRequired(true);
			title.setId(TITLE);

			description = new BaseTextArea($("DecommissioningBuilderView.description"));
			description.setId(DESCRIPTION);

			return new BaseForm<DecommissioningListParams>(
					new DecommissioningListParams(), this, title, description) {
				@Override
				protected void saveButtonClick(DecommissioningListParams params)
						throws ValidationException {
					getWindow().close();
					params.setSelectedRecordIds(getSelectedRecordIds());
					presenter.decommissioningListCreationRequested(params);
				}

				@Override
				protected void cancelButtonClick(DecommissioningListParams params) {
					getWindow().close();
				}
			};
		}

		@Override
		public boolean isVisible(){
			return presenter.isAddMode();
		}
	}

	private Button buildAddToListButton() {
		addToListButton = new Button($("DecommissioningBuilderView.addToList")) {
			@Override
			public boolean isVisible() {
				return !presenter.isAddMode();
			}
		};
		addToListButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.addToListButtonClicked(getSelectedRecordIds());
			}
		});
		return addToListButton;
	}
}

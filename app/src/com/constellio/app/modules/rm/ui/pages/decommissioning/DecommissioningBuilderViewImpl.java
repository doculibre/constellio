/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.ui.pages.decommissioning;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

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
	private LookupRecordField adminUnit;

	public DecommissioningBuilderViewImpl() {
		presenter = new DecommissioningBuilderPresenter(this);
		presenter.resetFacetAndOrder();
		criteria = new AdvancedSearchCriteriaComponent(presenter);
		addStyleName("search-decommissioning");
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
				navigateTo().decommissioning();
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

		searchButton.setEnabled(false);

		return searchUI;
	}

	@Override
	protected Component buildSummary(SearchResultTable results) {
		Button createList = new DecommissioningButton($("DecommissioningBuilderView.createDecommissioningList"));
		createList.addStyleName(ValoTheme.BUTTON_LINK);
		createList.addStyleName(CREATE_LIST);
		return results.createSummary(null, createList);
	}

	private Component buildAdministrativeUnitComponent() {
		Label label = new Label($("DecommissioningBuilderView.administrativeUnit"));
		adminUnit = new LookupRecordField(AdministrativeUnit.SCHEMA_TYPE);
		adminUnit.setRequired(true);
		adminUnit.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				searchButton.setEnabled(true);
				presenter.administrativeUnitSelected(adminUnit.getValue());
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
					params.setSelectedFolderIds(getSelectedRecordIds());
					presenter.decommissioningListCreationRequested(params);
				}

				@Override
				protected void cancelButtonClick(DecommissioningListParams params) {
					getWindow().close();
				}
			};
		}
	}
}

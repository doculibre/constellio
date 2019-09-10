package com.constellio.app.ui.pages.management.taxonomy;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.SearchButton;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.containers.TaxonomyConceptsWithChildrenCountContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.schemas.Schemas;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class TaxonomyManagementSearchViewImpl extends BaseViewImpl implements TaxonomyManagementSearchView {

	private VerticalLayout layout;
	private HorizontalLayout searchLayout;
	private TaxonomyManagementSearchPresenter presenter;
	public static final String STYLE_NAME = "display-taxonomy";
	private VerticalLayout mainLayout;

	public TaxonomyManagementSearchViewImpl() {
		this.presenter = new TaxonomyManagementSearchPresenter(this);
	}

	@Override
	protected String getTitle() {
		return presenter.getTaxonomy().getTitle();
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		super.initBeforeCreateComponents(event);
		presenter.forParams(event.getParameters());
	}

	@Override
	protected void afterViewAssembled(ViewChangeEvent event) {
		presenter.viewAssembled();
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		buildSearchTaxonomies();
		mainLayout.addComponents(searchLayout);

		VerticalLayout taxonomyDisplayLayout = new VerticalLayout(buildConceptsTables());
		taxonomyDisplayLayout.setSizeFull();
		taxonomyDisplayLayout.setSpacing(true);

		mainLayout.addComponent(taxonomyDisplayLayout);

		return mainLayout;
	}

	private Component buildConceptsTables() {

		layout = new VerticalLayout();
		for (final RecordVODataProvider dataProvider : presenter.getDataProviders()) {
			Container recordsContainer = new RecordVOLazyContainer(dataProvider);
			TaxonomyConceptsWithChildrenCountContainer adaptedContainer = new TaxonomyConceptsWithChildrenCountContainer(
					recordsContainer, getCollection(), getSessionContext().getCurrentUser().getUsername(),
					presenter.getTaxonomy().getCode(), dataProvider.getSchema().getCode().split("_")[0]) {
				@Override
				protected Collection<?> getOwnContainerPropertyIds() {
					return new ArrayList<>();
				}
			};
			ButtonsContainer buttonsContainer = new ButtonsContainer<>(adaptedContainer, "buttons");
			buttonsContainer.addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
					return new DisplayButton() {
						@Override
						protected void buttonClick(ClickEvent event) {
							Integer index = (Integer) itemId;
							RecordVO entity = dataProvider.getRecordVO(index);
							presenter.displayButtonClicked(entity);
						}
					};
				}
			});

			if (!presenter.canOnlyConsultTaxonomy()) {
				buttonsContainer.addButton(new ContainerButton() {
					@Override
					protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
						return new EditButton() {
							@Override
							protected void buttonClick(ClickEvent event) {
								Integer index = (Integer) itemId;
								RecordVO entity = dataProvider.getRecordVO(index);
								presenter.editButtonClicked(entity);
							}
						};
					}
				});

				buttonsContainer.addButton(new ContainerButton() {
					@Override
					protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
						DeleteButton deleteButton = new DeleteButton() {
							@Override
							protected void confirmButtonClick(ConfirmDialog dialog) {
								Integer index = (Integer) itemId;
								RecordVO entity = dataProvider.getRecordVO(index);
								presenter.deleteButtonClicked(entity);
							}
						};
						return deleteButton;
					}
				});
			}

			// TODO Implement deleteLogically for taxonomy concepts
			recordsContainer = buttonsContainer;

			RecordVOTable table = new RecordVOTable($(dataProvider.getSchema().getLabel(), dataProvider.getSchema().getCode()),
					recordsContainer);
			table.setWidth("100%");
			table.setId("childrenTable");
			table.setColumnHeader("buttons", "");
			table.setColumnHeader("taxonomyChildrenCount", $("TaxonomyManagementView.childrenCount"));
			table.setColumnWidth(dataProvider.getSchema().getCode() + "_id", 120);
			table.setColumnWidth("buttons", 120);
			table.setColumnExpandRatio(dataProvider.getSchema().getCode() + "_" + Schemas.TITLE_CODE, 1.0f);
			table.setPageLength(table.getItemIds().size());
			setDefaultOrderBy(presenter.getDefaultOrderField(), dataProvider, table);
			table.sort();
			layout.addComponents(table);
		}
		return layout;
	}

	private void setDefaultOrderBy(String localCode, RecordVODataProvider dataProvider, Table table) {
		Object[] properties = {dataProvider.getSchema().getMetadata(localCode)};
		boolean[] ordering = {true};
		table.sort(properties, ordering);
	}

	private void buildSearchTaxonomies() {
		searchLayout = new HorizontalLayout();
		final TextField searchField = new BaseTextField();
		searchField.setValue(presenter.getQueryExpression());
		searchField.focus();
		searchField.setNullRepresentation("");
		Button searchButton = new SearchButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.searchConcept(searchField.getValue());
			}
		};
		searchLayout.addComponents(searchField, searchButton);

		OnEnterKeyHandler onEnterHandler = new OnEnterKeyHandler() {
			@Override
			public void onEnterKeyPressed() {
				presenter.searchConcept(searchField.getValue());
			}
		};
		onEnterHandler.installOn(searchField);
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> actionMenuButtons = new ArrayList<Button>();
		return actionMenuButtons;
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
	public void refreshTable() {
		layout.removeAllComponents();
		layout.addComponent(buildConceptsTables());
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return null;
	}
}

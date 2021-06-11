package com.constellio.app.modules.rm.ui.pages.legalrequirement;

import com.constellio.app.modules.rm.wrappers.LegalReference;
import com.constellio.app.modules.rm.wrappers.LegalRequirement;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.buttons.SearchButton;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.schemas.Schemas;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.constellio.app.ui.i18n.i18n.$;

public class LegalRequirementListViewImpl extends BaseViewImpl implements LegalRequirementListView {

	private static Logger LOGGER = LoggerFactory.getLogger(LegalRequirementListViewImpl.class);

	private LegalRequirementListPresenter presenter;

	private Map<String, VerticalLayout> tableContainers;
	private List<TextField> searchFields;

	public LegalRequirementListViewImpl() {
		presenter = new LegalRequirementListPresenter(this);

		tableContainers = new HashMap<>();
		searchFields = new ArrayList<>();
	}

	@Override
	protected String getTitle() {
		return $("LegalRequirementManagement.legalRequirements");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout mainLayout = new VerticalLayout();

		TabSheet tabSheet = new TabSheet();
		tabSheet.addSelectedTabChangeListener(new SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				for (TextField field : searchFields) {
					field.setValue("");
				}
				presenter.clearSearchRequested();
			}
		});
		mainLayout.addComponent(tabSheet);

		tabSheet.addTab(buildTab(LegalRequirement.SCHEMA_TYPE), $("LegalRequirementManagement.requirements"));
		tabSheet.addTab(buildTab(LegalReference.SCHEMA_TYPE), $("LegalRequirementManagement.references"));

		refreshTables();

		return mainLayout;
	}

	private Component buildTab(String schemaType) {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);

		mainLayout.addComponent(buildSearch());

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setMargin(new MarginInfo(false, true));
		mainLayout.addComponent(buttonLayout);
		mainLayout.setComponentAlignment(buttonLayout, Alignment.TOP_RIGHT);

		if (presenter.canEdit()) {
			Component addButton = buildAddButton(schemaType);
			buttonLayout.addComponent(addButton);
		}

		VerticalLayout tableLayout = new VerticalLayout();
		tableContainers.put(schemaType, tableLayout);
		mainLayout.addComponent(tableLayout);

		return mainLayout;
	}

	private Component buildSearch() {
		TextField searchField = new BaseTextField();
		searchField.setWidth("100%");
		searchField.addStyleName("folder-search-field");
		searchFields.add(searchField);

		OnEnterKeyHandler onEnterHandler = new OnEnterKeyHandler() {
			@Override
			public void onEnterKeyPressed() {
				String searchValue = searchField.getValue();
				presenter.searchRequested(searchValue);
			}
		};
		onEnterHandler.installOn(searchField);

		SearchButton searchButton = new SearchButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				String searchValue = searchField.getValue();
				presenter.searchRequested(searchValue);
			}
		};
		searchButton.addStyleName("folder-search-button");
		searchButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		searchButton.setIconOnly(true);

		LinkButton clearSearchButton = new LinkButton($("CollectionSecurityManagement.clearSearch")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.clearSearchRequested();
				searchField.setValue("");
			}
		};
		clearSearchButton.addStyleName("folder-search-clear");

		VerticalLayout searchLayout = new VerticalLayout();
		searchLayout.addStyleName("folder-search-layout");
		searchLayout.setSpacing(true);
		searchLayout.setWidth("50%");

		I18NHorizontalLayout searchFieldAndButtonLayout = new I18NHorizontalLayout(searchField, searchButton);
		searchFieldAndButtonLayout.addStyleName("folder-search-field-and-button-layout");
		searchFieldAndButtonLayout.setWidth("100%");
		searchFieldAndButtonLayout.setExpandRatio(searchField, 1);

		I18NHorizontalLayout extraFieldsSearchLayout = new I18NHorizontalLayout(clearSearchButton);
		extraFieldsSearchLayout.addStyleName("folder-search-extra-fields-layout");
		extraFieldsSearchLayout.setSpacing(true);

		searchLayout.addComponents(searchFieldAndButtonLayout, extraFieldsSearchLayout);
		return searchLayout;
	}

	private Component buildTable(RecordVODataProvider dataProvider) {
		ButtonsContainer recordsContainer = new ButtonsContainer<>(new RecordVOLazyContainer(dataProvider), "buttons");
		recordsContainer.addButton(new ContainerButton() {
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
		if (presenter.canEdit()) {
			recordsContainer.addButton(new ContainerButton() {
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
			recordsContainer.addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
					return new DeleteButton() {
						@Override
						protected void confirmButtonClick(ConfirmDialog dialog) {
							Integer index = (Integer) itemId;
							RecordVO entity = dataProvider.getRecordVO(index);

							try {
								presenter.deleteButtonClicked(entity);
							} catch (Exception e) {
								LOGGER.error(e.getMessage());
								showErrorMessage($("LegalRequirementManagement.deleteRecordFailed"));
							}
						}
					};
				}
			});
		}

		RecordVOTable table = new RecordVOTable($(dataProvider.getSchema().getLabel(), dataProvider.getSchema().getCode()), recordsContainer);
		table.setWidth("100%");
		table.setColumnHeader("buttons", "");
		table.setColumnWidth(dataProvider.getSchema().getCode() + "_id", 120);
		table.setColumnWidth("buttons", presenter.canEdit() ? 120 : 40);
		table.setColumnCollapsible("buttons", false);
		table.setColumnExpandRatio(dataProvider.getSchema().getCode() + "_" + Schemas.TITLE_CODE, 1.0f);
		table.setPageLength(Math.min(15, dataProvider.size()));
		table.sort();

		return table;
	}

	private Component buildAddButton(String schemaType) {
		BaseButton addButton = new BaseButton($("LegalRequirementManagement.add_" + schemaType), FontAwesome.PLUS) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addButtonClicked(schemaType);
			}
		};

		addButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		addButton.addStyleName("add-button");
		addButton.addStyleName(ValoTheme.BUTTON_LINK);

		return addButton;
	}

	@Override
	public void refreshTables() {
		for (Entry<String, VerticalLayout> entry : tableContainers.entrySet()) {
			entry.getValue().removeAllComponents();
			entry.getValue().addComponent(buildTable(presenter.getDataProvider(entry.getKey())));
		}
	}
}

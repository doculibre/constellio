package com.constellio.app.ui.pages.search;

import com.constellio.app.ui.entities.SearchBoostVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.SearchBoostLazyContainer;
import com.constellio.app.ui.framework.data.SearchBoostDataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.searchConfig.SearchConfigurationViewImpl;
import com.vaadin.data.Container.Filterable;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class SearchBoostByQueryViewImpl extends BaseViewImpl implements SearchBoostView {
	private SearchBoostByQueryPresenter presenter;
	private static final String PROPERTY_BUTTONS = "buttons";
	private VerticalLayout viewLayout;
	private HorizontalLayout horizontalLayout;

	private Table table;

	public SearchBoostByQueryViewImpl() {
		this.presenter = new SearchBoostByQueryPresenter(this);
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return SearchConfigurationViewImpl.getSearchConfigurationBreadCrumbTrail(this, getTitle());
	}

	@Override
	protected String getTitle() {
		return $("SearchBoostByQueryView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		viewLayout.setSpacing(true);
		table = buildTable();

		horizontalLayout = buildActionButtonLayout();

		viewLayout.addComponent(horizontalLayout);
		viewLayout.setComponentAlignment(horizontalLayout, Alignment.TOP_RIGHT);

		viewLayout.addComponents(table);
		viewLayout.setExpandRatio(table, 1);

		return viewLayout;
	}

	private HorizontalLayout buildActionButtonLayout() {
		HorizontalLayout horizontalLayout = new HorizontalLayout();

		Button add = buildAddEditForm(null);
		add.setCaption($("SearchBoostByQueryView.add"));
		add.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		add.addStyleName(ValoTheme.BUTTON_LINK);
		add.addStyleName("marginright");

		horizontalLayout.addComponent(add);

		return horizontalLayout;
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

	Table buildTable() {
		final SearchBoostDataProvider dataProvider = presenter.newDataProvider();

		List<SearchBoostVO> searchBoostVOs = dataProvider.listSearchBoostVOs();
		dataProvider.setSearchBoostVOs(searchBoostVOs);

		Filterable tableContainer = new SearchBoostLazyContainer(dataProvider);
		ButtonsContainer buttonsContainer = new ButtonsContainer(tableContainer, PROPERTY_BUTTONS);
		addButtons(dataProvider, buttonsContainer);
		tableContainer = buttonsContainer;

		Table table = new BaseTable(getClass().getName(), $("SearchBoostByQueryView.records", tableContainer.size()), tableContainer);
		table.setPageLength(Math.min(15, dataProvider.size()));
		table.setWidth("100%");
		table.setColumnHeader("label", $("SearchBoostByQueryView.labelColumn"));
		table.setColumnHeader("value", $("SearchBoostByQueryView.valueColumn"));
		table.setColumnHeader(PROPERTY_BUTTONS, "");
		table.setColumnWidth(PROPERTY_BUTTONS, 120);
		return table;
	}

	private void addButtons(final SearchBoostDataProvider provider, ButtonsContainer buttonsContainer) {
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				Button button = buildAddEditForm(presenter.getSearchBoostVO((Integer) itemId, provider));
				button.setStyleName(ValoTheme.BUTTON_BORDERLESS);
				button.addStyleName(EditButton.BUTTON_STYLE);
				button.setIcon(EditButton.ICON_RESOURCE);
				return button;
			}
		});
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						SearchBoostVO searchBoostVO = presenter.getSearchBoostVO((Integer) itemId, provider);
						presenter.deleteButtonClicked(searchBoostVO);

					}
				};
			}
		});
	}

	public void refreshTable() {
		Table newTable = buildTable();
		viewLayout.replaceComponent(table, newTable);
		table = newTable;
	}

	@Override
	public List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> result = super.buildActionMenuButtons(event);

		return result;
	}

	@Override
	public Button buildAddEditForm(final SearchBoostVO currentSearchBoostVO) {
		WindowConfiguration windowConfiguration = new WindowConfiguration(true, false, "450px", "290px");
		return new WindowButton("",
				$("SearchBoostByQueryView.addEdit"), windowConfiguration) {
			@Override
			protected Component buildWindowContent() {

				final TextField labelField = new TextField($("SearchBoostByQueryView.labelField"));
				labelField.setRequired(true);
				labelField.setId("labelField");
				labelField.addStyleName("labelField");
				labelField.setWidth("100%");
				if (currentSearchBoostVO != null) {
					labelField.setValue(currentSearchBoostVO.getLabel());
				}

				final TextField valueField = new TextField($("SearchBoostByQueryView.valueField"));
				valueField.setRequired(true);
				valueField.setId("valueField");
				valueField.addStyleName("valueField");
				valueField.setWidth("100%");
				if (currentSearchBoostVO != null) {
					valueField.setValue(String.valueOf(currentSearchBoostVO.getValue()));
				}

				final TextField keyField = new TextField($("SearchBoostByQueryView.keyField"));
				keyField.setRequired(true);
				keyField.setId("keyField");
				keyField.addStyleName("keyField");
				keyField.setWidth("90%");
				if (currentSearchBoostVO != null) {
					keyField.setValue(currentSearchBoostVO.getKey());
				}

				BaseButton addButton = new BaseButton($("save")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						boolean valid = false;
						SearchBoostVO searchBoostVO = new SearchBoostVO();
						searchBoostVO.setLabel(labelField.getValue());
						searchBoostVO.setKey(keyField.getValue());
						if (presenter.validate(searchBoostVO, valueField.getValue())) {
							if (currentSearchBoostVO != null) {
								presenter.editButtonClicked(searchBoostVO, valueField.getValue(), currentSearchBoostVO);
							} else {
								presenter.addButtonClicked(searchBoostVO, valueField.getValue());
							}
							getWindow().close();
						}
					}
				};
				addButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				BaseButton cancelButton = new BaseButton($("cancel")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						getWindow().close();
					}
				};

				HorizontalLayout buttonsLayout = new HorizontalLayout();
				buttonsLayout.setSpacing(true);
				buttonsLayout.addComponents(addButton, cancelButton);

				HorizontalLayout horizontalLayout = new HorizontalLayout();
				horizontalLayout.setSpacing(true);
				horizontalLayout.addComponents(labelField, valueField);

				//				HorizontalLayout horizontalLayoutQuery = new HorizontalLayout();
				//				horizontalLayoutQuery.setSpacing(true);
				//				horizontalLayoutQuery.addComponents(keyField);
				//				horizontalLayoutQuery.setExpandRatio(keyField, 1);

				VerticalLayout verticalLayout = new VerticalLayout();
				verticalLayout.setSizeFull();
				verticalLayout
						.addComponents(horizontalLayout, keyField, buttonsLayout);
				verticalLayout.setComponentAlignment(buttonsLayout, Alignment.TOP_CENTER);
				verticalLayout.setSpacing(true);

				return verticalLayout;
			}
		};
	}
}

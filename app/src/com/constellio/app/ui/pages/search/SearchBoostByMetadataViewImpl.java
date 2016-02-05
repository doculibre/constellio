package com.constellio.app.ui.pages.search;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.ui.entities.SearchBoostVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.SearchBoostLazyContainer;
import com.constellio.app.ui.framework.data.SearchBoostDataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Container.Filterable;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class SearchBoostByMetadataViewImpl extends BaseViewImpl implements SearchBoostView {
	private SearchBoostByMetadataPresenter presenter;
	private static final String PROPERTY_BUTTONS = "buttons";
	private VerticalLayout viewLayout;

	private Table table;

	public SearchBoostByMetadataViewImpl() {
		this.presenter = new SearchBoostByMetadataPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("SearchBoostByMetadataView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		viewLayout.setSpacing(true);
		table = buildTable();

		viewLayout.addComponents(table);
		viewLayout.setExpandRatio(table, 1);

		return viewLayout;
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

		Table table = new Table($("SearchBoostByMetadataView.viewTitle"), tableContainer);
		table.setPageLength(Math.min(15, dataProvider.size()));
		table.setWidth("100%");
		table.setColumnHeader("label", $("SearchBoostByMetadataView.labelColumn"));
		table.setColumnHeader("value", $("SearchBoostByMetadataView.valueColumn"));
		table.setColumnHeader(PROPERTY_BUTTONS, "");
		table.setColumnWidth(PROPERTY_BUTTONS, 120);
		return table;
	}

	private void addButtons(final SearchBoostDataProvider provider, ButtonsContainer buttonsContainer) {
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				Button button = buildAddEditForm(presenter.getSearchBoostVO((Integer) itemId, provider));
				button.setStyleName(ValoTheme.BUTTON_BORDERLESS);
				button.addStyleName(EditButton.BUTTON_STYLE);
				button.setIcon(EditButton.ICON_RESOURCE);
				return button;
			}
		});
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
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

		Button add = buildAddEditForm(null);
		add.setCaption($("SearchBoostByMetadataView.add"));

		result.add(add);
		return result;
	}

	@Override
	public Button buildAddEditForm(final SearchBoostVO currentSearchBoostVO) {
		return new WindowButton("",
				$("SearchBoostByMetadataView.addEdit")) {
			@Override
			protected Component buildWindowContent() {

				final ComboBox metadataField = new ComboBox();
				metadataField.setCaption($("SearchBoostByMetadataView.metadataField"));
				for (SearchBoostVO searchBoostVO : presenter.getMetadatasSearchBoostVO()) {
					metadataField.addItem(searchBoostVO);
					metadataField
							.setItemCaption(searchBoostVO, searchBoostVO.getLabel());
					if (currentSearchBoostVO != null && currentSearchBoostVO.getKey()
							.equals(searchBoostVO.getKey())) {
						metadataField.setValue(searchBoostVO);
					}
				}
				metadataField.setRequired(true);
				metadataField.setNullSelectionAllowed(false);

				final TextField valueField = new TextField($("SearchBoostByMetadataView.valueField"));
				valueField.setRequired(true);
				valueField.setId("valueField");
				valueField.addStyleName("valueField");
				if (currentSearchBoostVO != null) {
					valueField.setValue(String.valueOf(currentSearchBoostVO.getValue()));
				}

				BaseButton addButton = new BaseButton($("save")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						if (presenter.validate((SearchBoostVO) metadataField.getValue(), valueField.getValue())) {
							if (currentSearchBoostVO != null) {
								presenter.editButtonClicked((SearchBoostVO) metadataField.getValue(), valueField.getValue(),
										currentSearchBoostVO);
							} else {
								presenter.addButtonClicked((SearchBoostVO) metadataField.getValue(), valueField.getValue());
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
				cancelButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				HorizontalLayout horizontalLayoutButtons = new HorizontalLayout();
				horizontalLayoutButtons.setSpacing(true);
				horizontalLayoutButtons.addComponents(addButton, cancelButton);

				HorizontalLayout horizontalLayout = new HorizontalLayout();
				horizontalLayout.setSpacing(true);
				horizontalLayout.addComponents(metadataField, valueField);

				VerticalLayout verticalLayout = new VerticalLayout();
				verticalLayout
						.addComponents(horizontalLayout, horizontalLayoutButtons);
				verticalLayout.setSpacing(true);

				return verticalLayout;
			}
		}

				;
	}
}

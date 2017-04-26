package com.constellio.app.ui.pages.management.facet;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisableButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.EnableButton;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.records.wrappers.Facet;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class ListFacetConfigurationViewImpl extends BaseViewImpl implements ListFacetConfigurationView {
	private ListFacetConfigurationPresenter presenter;
	private RecordVOTable listFacet;
	private VerticalLayout mainLayout;

	public ListFacetConfigurationViewImpl() {
		presenter = new ListFacetConfigurationPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListFacetConfigurationView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);
		listFacet = valuesTable();
		mainLayout.addComponents(listFacet);
		return mainLayout;
	}

	@Override
	public List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> result = super.buildActionMenuButtons(event);

		Button add = new Button($("ListFacetConfigurationView.add"));
		add.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.addButtonClicked();
			}
		});

		Button order = new Button($("ListFacetConfigurationView.order"));
		order.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.orderButtonClicked();
			}
		});

		result.add(add);
		result.add(order);
		return result;
	}

	private RecordVOTable valuesTable() {
		final RecordVODataProvider dataProvider = presenter.getDataProvider();
		Container recordsContainer = new RecordVOLazyContainer(dataProvider);
		ButtonsContainer buttonsContainer = new ButtonsContainer(recordsContainer, "buttons");
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
				Integer index = (Integer) itemId;
				final RecordVO entity = dataProvider.getRecordVO(index);
				Button activateButton = new EnableButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						presenter.activate(entity);
					}
				};
				activateButton.setVisible(!presenter.isActive(entity));
				return activateButton;
			}
		});

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				Integer index = (Integer) itemId;
				final RecordVO entity = dataProvider.getRecordVO(index);
				Button deactivateButton = new DisableButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						presenter.deactivate(entity);
					}
				};

				deactivateButton.setVisible(presenter.isActive(entity));
				return deactivateButton;
			}
		});

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						Integer index = (Integer) itemId;
						RecordVO entity = dataProvider.getRecordVO(index);
						presenter.deleteButtonClicked(entity);
					}
				};
			}
		});

		recordsContainer = buttonsContainer;
		RecordVOTable table = new RecordVOTable(
				$("ListFacetConfiguration.tableTitle", dataProvider.size()), recordsContainer, false);
		table.setWidth("100%");
		table.setColumnHeader("buttons", "");
		table.setColumnHeader(Facet.TITLE, $("title"));
		table.setColumnHeader(Facet.FACET_TYPE, $("facetType"));
		table.setColumnHeader(Facet.ORDER_RESULT, $("elementPerPage"));
		table.setColumnHeader(Facet.ORDER, $("facerOrder"));
		table.setColumnWidth(dataProvider.getSchema().getCode() + "_id", 120);
		table.setColumnWidth("buttons", 160);
		table.setPageLength(Math.min(15, dataProvider.size()));

		return table;
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
	}

	@Override
	public void refreshTable() {
		RecordVOTable newListFacet = valuesTable();
		mainLayout.replaceComponent(listFacet, newListFacet);
		listFacet = newListFacet;
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
}

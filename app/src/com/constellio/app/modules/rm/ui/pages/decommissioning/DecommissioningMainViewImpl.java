package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import static com.constellio.app.ui.i18n.i18n.$;

public class DecommissioningMainViewImpl extends BaseViewImpl implements DecommissioningMainView {
	public static final String BUTTONS_PROPERTY_ID = "buttons";
	public static final String CREATE = "create-";

	private final DecommissioningMainPresenter presenter;
	private TabSheet sheet;

	public DecommissioningMainViewImpl() {
		presenter = new DecommissioningMainPresenter(this);
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	@Override
	protected String getTitle() {
		return $("DecommissioningMainView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		sheet = new TabSheet();
		sheet.setSizeFull();
		sheet.addSelectedTabChangeListener(new SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				reloadCurrentTab();
			}
		});

		for (String tabId : presenter.getTabs()) {
			sheet.addTab(buildEmptyTab(tabId));
		}

		return sheet;
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
	public void reloadCurrentTab() {
		presenter.tabSelected(sheet.getSelectedTab().getId());
	}

	@Override
	public void displayListCreation() {
		VerticalLayout layout = getEmptiedSelectedTab();

		Label foldersWithoutDateCaption = new Label($("DecommissioningMainView.create.foldersWithoutDate"));
		foldersWithoutDateCaption.addStyleName(ValoTheme.LABEL_H2);
		foldersWithoutDateCaption.setVisible(presenter.getUser().has(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).onSomething());

		VerticalLayout foldersWithoutDate = new VerticalLayout(foldersWithoutDateCaption);
		for (final SearchType type : presenter.getCriteriaForFoldersWithoutPlanifiedDate()) {
			Button button = new Button($("DecommissioningMainView.create." + type));
			button.addStyleName(ValoTheme.BUTTON_LINK);
			button.addStyleName(CREATE + type);
			button.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					presenter.creationRequested(type);
				}
			});
			foldersWithoutDate.addComponent(button);
			button.setEnabled(presenter.getUser().has(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).onSomething());
			button.setVisible(button.isEnabled());
		}

		Label foldersWithDateCaption = new Label($("DecommissioningMainView.create.foldersWithDate"));
		foldersWithDateCaption.addStyleName(ValoTheme.LABEL_H2);

		VerticalLayout foldersWithDate = new VerticalLayout(foldersWithDateCaption);
		for (final SearchType type : presenter.getCriteriaForFoldersWithPlanifiedDate()) {
			Button button = new Button($("DecommissioningMainView.create." + type));
			button.addStyleName(ValoTheme.BUTTON_LINK);
			button.addStyleName(CREATE + type);
			button.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					presenter.creationRequested(type);
				}
			});
			foldersWithDate.addComponent(button);
			if (SearchType.transfer.equals(type)) {
				button.setEnabled(presenter.getUser().has(RMPermissionsTo.CREATE_TRANSFER_DECOMMISSIONING_LIST).globally() ||
								  presenter.getUser().has(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).onSomething());
				button.setVisible(button.isEnabled());
			} else {
				button.setEnabled(presenter.getUser().has(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).onSomething());
				button.setVisible(button.isEnabled());
			}
		}

		layout.addComponents(foldersWithoutDate, foldersWithDate);

		if (presenter.isDocumentDecommissioningSupported()) {
			Label documentsCaption = new Label($("DecommissioningMainView.create.documents"));
			documentsCaption.addStyleName(ValoTheme.LABEL_H2);

			VerticalLayout documents = new VerticalLayout(documentsCaption);
			for (final SearchType type : presenter.getCriteriaForDocuments()) {
				Button button = new Button($("DecommissioningMainView.create." + type));
				button.addStyleName(ValoTheme.BUTTON_LINK);
				button.addStyleName(CREATE + type);
				button.addClickListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						presenter.creationRequested(type);
					}
				});
				documents.addComponent(button);
				button.setEnabled(presenter.getUser().has(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).onSomething());
				button.setVisible(button.isEnabled());
			}

			layout.addComponent(documents);
			documentsCaption.setVisible(presenter.getUser().has(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).onSomething());
		}
	}

	@Override
	public void displayEditableTable(final RecordVODataProvider dataProvider) {
		ButtonsContainer container = buildContainer(dataProvider);
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						RecordVO entity = dataProvider.getRecordVO((int) itemId);
						presenter.displayButtonClicked(entity);
					}
				};
			}
		});

		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new EditButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						RecordVO entity = dataProvider.getRecordVO((int) itemId);
						presenter.editButtonClicked(entity);
					}

					@Override
					public boolean isVisible() {
						RecordVO decommissionningList = dataProvider.getRecordVO((int) itemId);
						return presenter.isEditable(decommissionningList);
					}
				};
			}
		});
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						RecordVO entity = dataProvider.getRecordVO((int) itemId);
						presenter.deleteButtonClicked(entity);
					}

					@Override
					protected String getConfirmDialogMessage() {
						RecordVO entity = dataProvider.getRecordVO((int) itemId);
						return presenter.getDeleteConfirmMessage(entity);
					}

					@Override
					public boolean isVisible() {
						RecordVO decommissionningList = dataProvider.getRecordVO((int) itemId);
						return presenter.isDeletable(decommissionningList);
					}
				};
			}
		});

		VerticalLayout layout = getEmptiedSelectedTab();
		layout.addComponent(buildTable(container));
	}

	@Override
	public void displayReadOnlyTable(final RecordVODataProvider dataProvider) {
		if (dataProvider.size() != 0) {
			ButtonsContainer container = buildContainer(dataProvider);
			container.addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
					return new DisplayButton() {
						@Override
						protected void buttonClick(ClickEvent event) {
							RecordVO entity = dataProvider.getRecordVO((int) itemId);
							presenter.displayButtonClicked(entity);
						}
					};
				}
			});

			VerticalLayout layout = getEmptiedSelectedTab();
			layout.addComponent(buildTable(container));
		}
	}

	private VerticalLayout buildEmptyTab(String tabId) {
		VerticalLayout tab = new VerticalLayout();
		tab.setCaption($("DecommissioningMainView.tab." + tabId));
		tab.addStyleName(tabId);
		tab.setId(tabId);
		tab.setSpacing(true);
		return tab;
	}

	private VerticalLayout getEmptiedSelectedTab() {
		VerticalLayout tab = (VerticalLayout) sheet.getSelectedTab();
		tab.removeAllComponents();
		return tab;
	}

	private ButtonsContainer<RecordVOLazyContainer> buildContainer(RecordVODataProvider dataProvider) {
		RecordVOLazyContainer container = new RecordVOLazyContainer(dataProvider);
		return new ButtonsContainer<>(container, BUTTONS_PROPERTY_ID);
	}

	private Table buildTable(ButtonsContainer container) {
		RecordVOTable table = new RecordVOTable($("DecommissioningMainView.lists", container.size()));
		table.setContainerDataSource(container);
		table.setColumnHeader(BUTTONS_PROPERTY_ID, "");
		table.setPageLength(Math.min(15, container.size()));
		table.setWidth("100%");
		return table;
	}
}

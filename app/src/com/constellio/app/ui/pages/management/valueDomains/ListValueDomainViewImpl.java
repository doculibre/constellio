package com.constellio.app.ui.pages.management.valueDomains;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.TabWithTable;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;

public class ListValueDomainViewImpl extends BaseViewImpl implements ListValueDomainView {
	private final ListValueDomainPresenter presenter;
	private VerticalLayout mainLayout;
	private TabSheet sheet;
	private List<TabWithTable> tabs;

	public ListValueDomainViewImpl() {
		presenter = new ListValueDomainPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListValueDomainView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		initTabs();
		mainLayout = new VerticalLayout(buildCreationComponent(), sheet);
		mainLayout.setSpacing(true);
		mainLayout.setWidth("100%");
		return mainLayout;
	}

	private void initTabs() {
		sheet = new TabSheet();
		tabs = new ArrayList<>();
		addTab(SYSTEM_TAB, $("ListValueDomainView.systemTabCaption"));
		addTab(CUSTOM_TAB, $("ListValueDomainView.customTabCaption"));
	}

	private void addTab(final String id, String caption) {
		TabWithTable tab = new TabWithTable(id) {
			@Override
			public Table buildTable() {
				return ListValueDomainViewImpl.this.buildTable(id);
			}
		};
		tabs.add(tab);
		sheet.addTab(tab.getTabLayout(), caption);
	}

	private void removeTab(String id) {
		TabWithTable tabToRemove = null;
		for(TabWithTable tab: tabs) {
			if(tab.getId().equals(id)) {
				tabToRemove = tab;
				sheet.removeComponent(tab.getTabLayout());
				break;
			}
		}
		tabs.remove(tabToRemove);
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
		for(TabWithTable tab: tabs) {
			tab.refreshTable();
		}
	}

	private Component buildCreationComponent() {
		final TextField valueDomainFrench = new BaseTextField();
		valueDomainFrench.setImmediate(true);
		final Button create = new AddButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.valueDomainCreationRequested(valueDomainFrench.getValue(), valueDomainFrench.getValue());
				this.setEnabled(false);
			}
		};
		create.setEnabled(false);
		valueDomainFrench.addTextChangeListener(new TextChangeListener() {
			@Override
			public void textChange(TextChangeEvent event) {
				if (presenter.canCreate(event.getText())) {
					create.setEnabled(true);
				} else {
					create.setEnabled(false);
				}
			}
		});

		HorizontalLayout creation = new HorizontalLayout(valueDomainFrench, create);
		creation.setSpacing(true);

		return creation;
	}

	private Table buildTable(String id) {
		BeanItemContainer elements = new BeanItemContainer<>(
				MetadataSchemaTypeVO.class, presenter.getDomainValues(CUSTOM_TAB.equals(id)));

		ButtonsContainer container = new ButtonsContainer<>(elements, "buttons");
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.displayButtonClicked((MetadataSchemaTypeVO) itemId);
					}
				};
			}
		});

		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				WindowButton editButton = new WindowButton(
						$("edit"), $("ListValueDomainView.labelColumn"), WindowConfiguration.modalDialog("400px", "150px")) {
					@Override
					protected Component buildWindowContent() {
						final MetadataSchemaTypeVO typeVO = (MetadataSchemaTypeVO) itemId;

						Label caption = new Label($("ListValueDomainView.labelColumn"));
						caption.addStyleName(ValoTheme.LABEL_BOLD);

						final BaseTextField title = new BaseTextField();
						title.setValue(typeVO.getLabel());
						title.setWidth("250px");

						BaseButton save = new BaseButton("Save") {
							@Override
							protected void buttonClick(ClickEvent event) {
								presenter.editButtonClicked(typeVO, title.getValue());
								getWindow().close();
							}
						};

						save.addStyleName(ValoTheme.BUTTON_PRIMARY);

						HorizontalLayout line = new HorizontalLayout(caption, title);
						line.setComponentAlignment(caption, Alignment.MIDDLE_LEFT);
						line.setSizeUndefined();
						line.setSpacing(true);

						VerticalLayout window = new VerticalLayout(line, save);
						window.setComponentAlignment(save, Alignment.MIDDLE_CENTER);
						window.setSpacing(true);

						return window;
					}
				};
				editButton.setIcon(EditButton.ICON_RESOURCE);
				editButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
				editButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
				editButton.setIconAlternateText(editButton.getCaption());
				return editButton;
			}
		});

		container.addButton(new ContainerButton() {

			@Override
			protected Button newButtonInstance(Object itemId, ButtonsContainer<?> container) {
				final MetadataSchemaTypeVO typeVO = (MetadataSchemaTypeVO) itemId;
				final String schemaTypeCode = typeVO.getCode();
				DeleteButton deleteButton = new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {

						try {
							presenter.deleteButtonClicked(schemaTypeCode);
						} catch (ValidationException e) {
							showErrorMessage($(e));
						}
					}
				};

				deleteButton.setVisible(presenter.isValueListPossiblyDeletable(schemaTypeCode));

				return deleteButton;
			}
		});

		Table table = new BaseTable(getClass().getName(), $("ListValueDomainView.tableTitle", container.size()), container);
		table.setPageLength(Math.min(15, container.size()));
		table.setVisibleColumns("label", "buttons");
		table.setColumnHeader("label", $("ListValueDomainView.labelColumn"));
		table.setColumnHeader("buttons", "");
		table.setColumnWidth("buttons", 124);
		table.setWidth("100%");

		return table;
	}
}

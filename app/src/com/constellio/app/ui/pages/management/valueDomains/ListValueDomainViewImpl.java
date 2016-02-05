package com.constellio.app.ui.pages.management.valueDomains;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class ListValueDomainViewImpl extends BaseViewImpl implements ListValueDomainView {
	private final ListValueDomainPresenter presenter;
	private VerticalLayout layout;
	private Table domainValues;

	public ListValueDomainViewImpl() {
		presenter = new ListValueDomainPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListValueDomainView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		domainValues = buildTable();
		layout = new VerticalLayout(buildCreationComponent(), domainValues);
		layout.setSpacing(true);
		layout.setWidth("100%");
		return layout;
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
		Table table = buildTable();
		layout.replaceComponent(domainValues, table);
		domainValues = table;
	}

	private Component buildCreationComponent() {
		final TextField valueDomain = new BaseTextField();
		valueDomain.setImmediate(true);
		final Button create = new AddButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.valueDomainCreationRequested(valueDomain.getValue());
				this.setEnabled(false);
			}
		};
		create.setEnabled(false);
		valueDomain.addTextChangeListener(new TextChangeListener() {
			@Override
			public void textChange(TextChangeEvent event) {
				if (presenter.canCreate(event.getText())) {
					create.setEnabled(true);
				} else {
					create.setEnabled(false);
				}
			}
		});

		HorizontalLayout creation = new HorizontalLayout(valueDomain, create);
		creation.setSpacing(true);

		return creation;
	}

	private Table buildTable() {
		BeanItemContainer elements = new BeanItemContainer<>(
				MetadataSchemaTypeVO.class, presenter.getDomainValues());

		ButtonsContainer container = new ButtonsContainer<>(elements, "buttons");
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
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
			protected Button newButtonInstance(final Object itemId) {
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

		Table table = new Table($("ListValueDomainView.tableTitle", container.size()), container);
		table.setPageLength(Math.min(15, container.size()));
		table.setVisibleColumns("label", "buttons");
		table.setColumnHeader("label", $("ListValueDomainView.labelColumn"));
		table.setColumnHeader("buttons", "");
		table.setColumnWidth("buttons", 88);
		table.setWidth("100%");

		return table;
	}
}

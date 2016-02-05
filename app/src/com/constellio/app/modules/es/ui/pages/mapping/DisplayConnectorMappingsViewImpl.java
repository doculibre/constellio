package com.constellio.app.modules.es.ui.pages.mapping;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.modules.es.services.mapping.MappingParams;
import com.constellio.app.modules.es.services.mapping.TargetParams;
import com.constellio.app.modules.es.ui.entities.DocumentType;
import com.constellio.app.modules.es.ui.entities.MappingVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class DisplayConnectorMappingsViewImpl extends BaseViewImpl implements DisplayConnectorMappingsView {
	private final DisplayConnectorMappingsPresenter presenter;
	private RecordVO instance;
	private VerticalLayout content;

	public DisplayConnectorMappingsViewImpl() {
		presenter = new DisplayConnectorMappingsPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		instance = presenter.forParams(event.getParameters()).getConnectorInstance();
	}

	@Override
	protected String getTitle() {
		return $("DisplayConnectorMappingView.viewTitle", instance.getTitle());
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
	protected Component buildMainComponent(ViewChangeEvent event) {
		List<DocumentType> types = presenter.getDocumentTypes();
		return types.size() == 1 ? buildMappingLayout(types.get(0)) : buildMappingTabs(types);
	}

	@Override
	public void reload() {
		final String documentType = content.getId();

		Button quick = new LinkButton($("DisplayConnectorMappingsView.quickConfig")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.quickConfigRequested(documentType);
			}
		};
		quick.addStyleName(ValoTheme.BUTTON_LINK);
		quick.setEnabled(presenter.canQuickConfig(documentType));

		Button add = new AddButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addMappingRequested(documentType);
			}
		};
		add.addStyleName(ValoTheme.BUTTON_PRIMARY);

		HorizontalLayout buttons = new HorizontalLayout(quick, add);
		buttons.setSpacing(true);

		content.removeAllComponents();
		content.addComponents(buttons, buildMappingTable(documentType));
		content.setComponentAlignment(buttons, Alignment.TOP_RIGHT);
	}

	@Override
	public void displayQuickConfig(final String documentType) {
		final List<MappingParams> config = presenter.getDefaultQuickConfig(documentType);
		final Table table = buildQuickConfigTable(config);

		Button selectAll = new LinkButton($("DisplayConnectorMappingView.selectAll")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				for (MappingParams params : config) {
					params.setActive(true);
				}
				table.refreshRowCache();
			}
		};

		Button deselectAll = new LinkButton($("DisplayConnectorMappingView.selectNone")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				for (MappingParams params : config) {
					params.setActive(false);
				}
				table.refreshRowCache();
			}
		};

		HorizontalLayout selection = new HorizontalLayout(selectAll, deselectAll);
		selection.setSpacing(true);

		Button create = new BaseButton($("DisplayConnectorMappingView.create")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.quickConfigRequested(documentType, config);
			}
		};
		create.addStyleName(ValoTheme.BUTTON_PRIMARY);

		Button cancel = new BaseButton($("cancel")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				reload();
			}
		};

		HorizontalLayout buttons = new HorizontalLayout(create, cancel);
		buttons.setSpacing(true);

		content.removeAllComponents();
		content.addComponents(selection, table, buttons);
		content.setComponentAlignment(buttons, Alignment.BOTTOM_CENTER);
	}

	private Component buildMappingTabs(List<DocumentType> documentTypes) {
		final TabSheet sheet = new TabSheet();
		sheet.setSizeFull();
		sheet.addSelectedTabChangeListener(new SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				content = (VerticalLayout) sheet.getSelectedTab();
				reload();
			}
		});
		for (DocumentType type : documentTypes) {
			sheet.addTab(buildEmptyTab(type, true));
		}
		return sheet;
	}

	private Component buildMappingLayout(DocumentType type) {
		content = buildEmptyTab(type, false);
		reload();
		return content;
	}

	private VerticalLayout buildEmptyTab(DocumentType type, boolean withCaption) {
		VerticalLayout tab = new VerticalLayout();
		if (withCaption) {
			tab.setCaption(type.getLabel());
		}
		tab.addStyleName(type.getCode());
		tab.setId(type.getCode());
		tab.setSpacing(true);
		return tab;
	}

	private Component buildMappingTable(String documentType) {
		MappingsContainer mappings = new MappingsContainer(documentType);
		Table table = new Table($("DisplayConnectorMappingsView.mappings", mappings.size()));
		table.setContainerDataSource(mappings);
		table.setVisibleColumns(MappingVO.METADATA_LABEL, MappingVO.FIELD_LABELS, ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID);
		table.setColumnHeader(MappingVO.METADATA_LABEL, $("DisplayConnectorMappingView.metadata"));
		table.setColumnHeader(MappingVO.FIELD_LABELS, $("DisplayConnectorMappingView.fields"));
		table.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		table.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 90);
		table.setPageLength(Math.min(15, mappings.size()));
		table.setWidth("100%");
		return table;
	}

	private Table buildQuickConfigTable(List<MappingParams> config) {
		final BeanItemContainer<MappingParams> container = new BeanItemContainer<>(MappingParams.class, config);

		final Table table = new Table("", container);

		table.addGeneratedColumn("active", new ColumnGenerator() {
			@Override
			public Object generateCell(Table source, Object itemId, Object columnId) {
				CheckBox box = new CheckBox("", container.getItem(itemId).getItemProperty(columnId));
				return box;
			}
		});

		table.addGeneratedColumn("target", new ColumnGenerator() {
			@Override
			public Object generateCell(Table source, Object itemId, Object columnId) {
				final MappingParams params = container.getItem(itemId).getBean();
				final TargetParams target = params.getTarget();

				Label label = new Label(target.getLabel());
				label.addStyleName(ValoTheme.LABEL_BOLD);

				final CheckBox searchable = new CheckBox(
						$("AddEditMappingView.metadata.searchable"), target.isSearchable());
				searchable.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						target.setSearchable(searchable.getValue());
					}
				});

				final CheckBox advancedSearch = new CheckBox(
						$("AddEditMappingView.metadata.advancedSearch"), target.isAdvancedSearch());
				advancedSearch.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						target.setAdvancedSearch(advancedSearch.getValue());
					}
				});

				final CheckBox searchResults = new CheckBox(
						$("AddEditMappingView.metadata.searchResults"), target.isSearchResults());
				searchResults.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						target.setSearchResults(searchResults.getValue());
					}
				});

				CssLayout options = new CssLayout(searchable, advancedSearch, searchResults) {
					@Override
					protected String getCss(Component c) {
						if (c instanceof CheckBox) {
							return "margin-right: 25px;";
						}
						return super.getCss(c);
					}
				};

				for (final String flag : presenter.getCustomFlags()) {
					final CheckBox box = new CheckBox(
							$("AddEditMappingView.metadata." + flag), target.hasCustomFlag(flag));
					box.addValueChangeListener(new ValueChangeListener() {
						@Override
						public void valueChange(ValueChangeEvent event) {
							if (box.getValue()) {
								target.setCustomFlag(flag);
							} else {
								target.unsetCustomFlag(flag);
							}
						}
					});

					options.addComponent(box);
				}

				if (target.isExisting()) {
					options.removeAllComponents();
					options.addComponent(new Label($("DisplayConnectorMappingsView.metadataExists")));
				}

				VerticalLayout layout = new VerticalLayout(label, options);
				layout.setSpacing(true);

				return layout;
			}
		});

		table.setPageLength(Math.min(10, container.size()));
		table.setVisibleColumns("active", "target");
		table.setColumnHeader("target", $("DisplayConnectorMappingView.fields"));
		table.setColumnHeader("active", "");
		table.setColumnWidth("active", 50);
		table.setWidth("100%");
		return table;
	}

	public class MappingsContainer extends ButtonsContainer<BeanItemContainer<MappingVO>> {

		public MappingsContainer(final String documentType) {
			super(new BeanItemContainer<>(MappingVO.class, presenter.getMappings(documentType)));
			addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(final Object itemId) {
					return new EditButton() {
						@Override
						protected void buttonClick(ClickEvent event) {
							presenter.editMappingRequested(documentType, (MappingVO) itemId);
						}
					};
				}
			});
			addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(final Object itemId) {
					return new DeleteButton() {
						@Override
						protected void confirmButtonClick(ConfirmDialog dialog) {
							presenter.deleteMappingRequested(documentType, (MappingVO) itemId);
						}
					};
				}
			});
		}
	}
}

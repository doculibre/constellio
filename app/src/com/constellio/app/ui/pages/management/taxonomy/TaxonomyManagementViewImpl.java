package com.constellio.app.ui.pages.management.taxonomy;

import com.constellio.app.api.extensions.taxonomies.TaxonomyExtraField;
import com.constellio.app.api.extensions.taxonomies.TaxonomyManagementClassifiedType;
import com.constellio.app.modules.rm.wrappers.structures.CommentFactory;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.buttons.ListSequencesButton;
import com.constellio.app.ui.framework.buttons.SearchButton;
import com.constellio.app.ui.framework.components.BaseDisplay;
import com.constellio.app.ui.framework.components.BaseDisplay.CaptionAndComponent;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.taxonomy.TaxonomyBreadcrumbTrail;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.containers.TaxonomyConceptsWithChildrenCountContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.CustomGuideUrl;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.StructureFactory;
import com.vaadin.data.Container;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class TaxonomyManagementViewImpl extends BaseViewImpl implements TaxonomyManagementView, CustomGuideUrl {
	VerticalLayout layout;
	private HorizontalLayout searchLayout;
	private TaxonomyManagementPresenter presenter;
	public static final String STYLE_NAME = "display-taxonomy";
	private TabSheet tabSheet;
	private Map<String, Component> tabComponents;
	private VerticalLayout mainLayout;

	public TaxonomyManagementViewImpl() {
		this.presenter = new TaxonomyManagementPresenter(this);
	}

	@Override
	protected String getTitle() {
		String conceptId = presenter.conceptId;
		if (conceptId != null) {
			return null;
		} else {
			return presenter.getTaxonomy().getTitle();
		}
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
		tabSheet = new TabSheet();
		tabComponents = new HashMap<>();

		buildSearchTaxonomies();
		mainLayout.addComponents(searchLayout);

		List<TaxonomyManagementClassifiedType> classifiedTypes = presenter.getClassifiedTypes();

		for (TaxonomyManagementClassifiedType classifiedType : classifiedTypes) {
			tabComponents.put(classifiedType.getSchemaType().getCode(), new CustomComponent());
		}

		VerticalLayout taxonomyDisplayLayout = new VerticalLayout(buildRootConceptsTables());
		taxonomyDisplayLayout.setSizeFull();
		taxonomyDisplayLayout.setSpacing(true);

		if (presenter.conceptId != null) {
			searchLayout.setVisible(false);
			if (!classifiedTypes.isEmpty()) {
				Component additionalInfo = buildAdditionalInformation();
				if (additionalInfo != null) {
					taxonomyDisplayLayout.addComponentAsFirst(additionalInfo);
				}
			}
			taxonomyDisplayLayout.addComponentAsFirst(buildConceptRecordDisplay(false));
			taxonomyDisplayLayout.addComponent(buildConceptRecordDisplay(true));
		}

		if (!classifiedTypes.isEmpty()) {
			tabSheet.addStyleName(STYLE_NAME);
			tabSheet.addTab(taxonomyDisplayLayout, $("TaxonomyManagementView.tabs.metadata")).setStyleName("metadata");
			for (TaxonomyManagementClassifiedType classifiedType : classifiedTypes) {
				MetadataSchemaTypeVO schemaType = classifiedType.getSchemaType();
				Component component = tabComponents.get(schemaType.getCode());
				component.addStyleName(schemaType.getCode() + "Table");
				tabSheet.addTab(component, classifiedType.getTabLabel()).setStyleName(schemaType.getCode() + "Tab");
				mainLayout.addComponent(tabSheet);
			}
		} else {
			mainLayout.addComponent(taxonomyDisplayLayout);
		}

		return mainLayout;
	}

	private void buildSearchTaxonomies() {
		searchLayout = new HorizontalLayout();
		searchLayout.setSpacing(true);
		final TextField searchField = new BaseTextField();
		Button searchButton = new SearchButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.searchConcept(searchField.getValue());
			}
		};
		searchField.focus();
		searchLayout.addComponents(searchField, searchButton);

		OnEnterKeyHandler onEnterHandler = new OnEnterKeyHandler() {
			@Override
			public void onEnterKeyPressed() {
				presenter.searchConcept(searchField.getValue());
			}
		};
		onEnterHandler.installOn(searchField);
	}

	private Component buildAdditionalInformation() {

		List<BaseDisplay.CaptionAndComponent> captionsAndComponents = new ArrayList<>();

		for (TaxonomyManagementClassifiedType classifiedType : presenter.getClassifiedTypes()) {
			MetadataSchemaTypeVO schemaType = classifiedType.getSchemaType();
			Label label = new Label(classifiedType.getCountLabel());
			label.setId("count-" + schemaType.getCode());
			label.addStyleName("count-" + schemaType.getCode());

			Label count = new Label("" + classifiedType.getDataProvider().size());
			count.addStyleName("display-value-count-" + schemaType.getCode());
			captionsAndComponents.add(new CaptionAndComponent(label, count));
		}

		for (TaxonomyExtraField extraField : presenter.getExtraFields()) {
			Label label = new Label(extraField.getLabel());
			label.setId(extraField.getCode());
			label.addStyleName(extraField.getCode());
			Component component = extraField.buildComponent();
			if (component != null) {
				captionsAndComponents.add(new CaptionAndComponent(label, component));
			}
		}

		BaseDisplay additionalInformationDisplay = new BaseDisplay(captionsAndComponents);
		return additionalInformationDisplay;

	}

	private RecordDisplay buildConceptRecordDisplay(boolean comments) {
		return new RecordDisplay(presenter.getCurrentConcept(), new SplitCommentsMetadataDisplayFactory(comments));
	}

	private Component buildRootConceptsTables() {
		layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);

		for (final RecordVODataProvider dataProvider : presenter.getDataProviders()) {
			Container recordsContainer = new RecordVOLazyContainer(dataProvider);
			TaxonomyConceptsWithChildrenCountContainer adaptedContainer = new TaxonomyConceptsWithChildrenCountContainer(
					recordsContainer, getCollection(), getSessionContext().getCurrentUser().getUsername(),
					presenter.getTaxonomy().getCode(), dataProvider.getSchema().getCode().split("_")[0]);
			ButtonsContainer buttonsContainer = new ButtonsContainer<>(adaptedContainer, "buttons");
			Button addButton = new AddButton() {
				@Override
				public void buttonClick(ClickEvent event) {
					presenter.addLinkClicked(presenter.getTaxonomy().getCode(), dataProvider.getSchema().getCode());
				}
			};
			addButton.addStyleName("add-taxo-element");
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

			boolean canOnlyConsultTaxonomy = presenter.canOnlyConsultTaxonomy();
			if(!canOnlyConsultTaxonomy) {
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

			RecordVOTable table = new RecordVOTable($(dataProvider.getSchema().getLabel(), dataProvider.getSchema().getCode()), recordsContainer);
			table.setWidth("100%");
			table.setId("childrenTable");
			table.setColumnHeader("buttons", "");
			table.setColumnHeader("taxonomyChildrenCount", $("TaxonomyManagementView.childrenCount"));
			table.setColumnWidth(dataProvider.getSchema().getCode() + "_id", 120);
			table.setColumnWidth("buttons", 120);
			table.setColumnExpandRatio(dataProvider.getSchema().getCode() + "_" + Schemas.TITLE_CODE, 1.0f);
			table.setPageLength(Math.min(15, dataProvider.size()));
			setDefaultOrderBy(presenter.getDefaultOrderField(), dataProvider, table);
			table.sort();

			if(!canOnlyConsultTaxonomy) {
				layout.addComponents(addButton);
				layout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);
			}

			layout.addComponent(table);
		}
		return layout;
	}

	private void setDefaultOrderBy(String localCode, RecordVODataProvider dataProvider, Table table) {
		Object[] properties = {dataProvider.getSchema().getMetadata(localCode)};
		boolean[] ordering = {true};
		table.sort(properties, ordering);
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> actionMenuButtons = new ArrayList<Button>();
		if(!presenter.canOnlyConsultTaxonomy()) {
			if (presenter.getCurrentConcept() != null && presenter.isPrincipalTaxonomy()) {
				actionMenuButtons.add(new LinkButton($("TaxonomyManagementView.manageAuthorizations")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.manageAccessAuthorizationsButtonClicked();
					}
				});
				actionMenuButtons.add(new LinkButton($("TaxonomyManagementView.manageRoles")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.manageRoleAuthorizationsButtonClicked();
					}
				});
			}
			RecordVO currentConcept = presenter.getCurrentConcept();
			if (currentConcept != null) {
				actionMenuButtons.add(new EditButton($("TaxonomyManagementView.edit")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.editButtonClicked(presenter.getCurrentConcept());
					}
				});
				actionMenuButtons.add(new DeleteButton($("TaxonomyManagementView.delete")) {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						presenter.deleteButtonClicked(presenter.getCurrentConcept());
					}
				});
				if (presenter.isSequenceTable(currentConcept)) {
					actionMenuButtons.add(new ListSequencesButton(currentConcept.getId(), $("TaxonomyManagementView.sequences")));
				}
			}
		}

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
		layout.addComponent(buildRootConceptsTables());
	}

	@Override
	public void setTabs(List<TaxonomyManagementClassifiedType> classifiedTypes) {
		for (TaxonomyManagementClassifiedType classifiedType : classifiedTypes) {
			MetadataSchemaTypeVO schemaType = classifiedType.getSchemaType();
			RecordVODataProvider provider = classifiedType.getDataProvider();
			RecordVOTable table = new RecordVOTable(provider);
			table.setWidth("100%");
			table.addItemClickListener(new ItemClickListener() {
				@Override
				public void itemClick(ItemClickEvent event) {
					RecordVOItem item = (RecordVOItem) event.getItem();
					RecordVO recordVO = item.getRecord();
					presenter.tabElementClicked(recordVO);
				}
			});
			table.setPageLength(Math.min(15, provider.size()));

			table.addStyleName(classifiedType.getSchemaType().getCode() + "Table");

			Component oldComponent = tabComponents.get(schemaType.getCode());
			tabComponents.put(schemaType.getCode(), table);
			tabSheet.replaceComponent(oldComponent, table);
		}
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		String conceptId = presenter.conceptId;
		if (conceptId != null) {
			return new TaxonomyBreadcrumbTrail(presenter.getTaxonomy().getCode(), conceptId, this, getTitle());
		} else {
			return null;
		}
	}

	public boolean hasCurrentUserAccessToCurrentConcept() {
		return presenter.hasCurrentUserAccessToCurrentConcept();
	}

	public RecordVO getCurrentConcept() {
		return presenter.getCurrentConcept();
	}

	public TaxonomyVO getTaxonomy() {
		return presenter.getTaxonomy();
	}

	@Override
	public String getGuideKey() {
		return presenter.getGuideKey();
	}

	public static class SplitCommentsMetadataDisplayFactory extends MetadataDisplayFactory {
		private final boolean comments;

		public SplitCommentsMetadataDisplayFactory(boolean comments) {
			this.comments = comments;
		}

		@Override
		public Component build(RecordVO recordVO, MetadataValueVO metadataValue) {
			return comments == isComments(metadataValue.getMetadata()) ?
				   super.build(recordVO, metadataValue) : null;
		}

		private boolean isComments(MetadataVO metadata) {
			StructureFactory factory = metadata.getStructureFactory();
			return factory != null && factory instanceof CommentFactory;
		}

	}

}

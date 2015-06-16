/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.management.taxonomy;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.wrappers.structures.CommentFactory;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.components.BaseDisplay;
import com.constellio.app.ui.framework.components.BaseDisplay.CaptionAndComponent;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.containers.TaxonomyConceptsWithChildrenCountContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
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
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class TaxonomyManagementViewImpl extends BaseViewImpl implements TaxonomyManagementView {

	VerticalLayout layout;
	private TaxonomyManagementPresenter presenter;
	public static final String STYLE_NAME = "display-taxonomy";
	private TabSheet tabSheet;
	private Component foldersCompoment;
	private VerticalLayout mainLayout;

	public TaxonomyManagementViewImpl() {
		this.presenter = new TaxonomyManagementPresenter(this);
	}

	@Override
	protected String getTitle() {
		TaxonomyVO taxonomy = presenter.getTaxonomy();
		for (String rmTaxoCode : RMTaxonomies.ALL_RM_TAXONOMIES) {
			if (rmTaxoCode.equals(taxonomy.getCode())) {
				return taxonomy.getTitle();
			}
		}
		return taxonomy.getTitle();
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
		foldersCompoment = new CustomComponent();

		VerticalLayout taxonomyDisplayLayout = new VerticalLayout(buildRootConceptsTables());
		taxonomyDisplayLayout.setSizeFull();
		taxonomyDisplayLayout.setSpacing(true);

		boolean enableTabSheet = false;
		if (presenter.conceptId != null) {
			if (presenter.getTaxonomy().getCode().equals("admUnits") || presenter.getTaxonomy()
					.getCode().equals("plan")) {
				enableTabSheet = true;
				Component additionalInfo = buildAdditionalInformation();
				if (additionalInfo != null) {
					taxonomyDisplayLayout.addComponentAsFirst(additionalInfo);
				}
			}
			taxonomyDisplayLayout.addComponentAsFirst(buildConceptRecordDisplay(false));
			taxonomyDisplayLayout.addComponent(buildConceptRecordDisplay(true));
		}

		if (enableTabSheet) {
			tabSheet.addStyleName(STYLE_NAME);
			tabSheet.addTab(taxonomyDisplayLayout, $("TaxonomyManagementView.tabs.metadata")).setStyleName("metadata");
			foldersCompoment.addStyleName("foldersTable");
			tabSheet.addTab(foldersCompoment, $("TaxonomyManagementView.tabs.folders")).setStyleName("folders");
			mainLayout.addComponent(tabSheet);
		} else {
			mainLayout.addComponent(taxonomyDisplayLayout);
		}

		return mainLayout;
	}

	private Component buildAdditionalInformation() {
		Label numberOfFoldersCaptionLabel = new Label($("TaxonomyManagementView.numberOfFolders"));
		numberOfFoldersCaptionLabel.setId("numberOfFolders");
		numberOfFoldersCaptionLabel.addStyleName("numberOfFolders");
		Label numberOfFoldersDisplayComponent = new Label(presenter.getNumberOfFolders());
		numberOfFoldersDisplayComponent.addStyleName("display-value-numberOfFolders");

		Label retentionRulesCaptionLabel = null;
		Component retentionRulesDisplayComponent = null;
		String taxonomyCode = presenter.getTaxonomy().getCode();
		if (taxonomyCode.equals("admUnits")) {
			retentionRulesCaptionLabel = new Label($("TaxonomyManagementView.retentionRules"));
			retentionRulesCaptionLabel.setId("retentionRules");
			retentionRulesCaptionLabel.addStyleName("retentionRules");
			retentionRulesDisplayComponent = buildDisplayList(presenter.getRetentionRules());
		}

		List<BaseDisplay.CaptionAndComponent> captionsAndComponents = new ArrayList<>();
		captionsAndComponents.add(new CaptionAndComponent(numberOfFoldersCaptionLabel, numberOfFoldersDisplayComponent));
		if (retentionRulesDisplayComponent != null) {
			captionsAndComponents.add(new CaptionAndComponent(retentionRulesCaptionLabel, retentionRulesDisplayComponent));
		}
		BaseDisplay additionalInformationDisplay = new BaseDisplay(captionsAndComponents);
		return additionalInformationDisplay;

	}

	private RecordDisplay buildConceptRecordDisplay(boolean comments) {
		return new RecordDisplay(presenter.getCurrentConcept(), new SplitCommentsMetadataDisplayFactory(comments));
	}

	private Component buildRootConceptsTables() {
		VerticalLayout layout = new VerticalLayout();
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
				protected Button newButtonInstance(final Object itemId) {
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
				protected Button newButtonInstance(final Object itemId) {
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
				protected Button newButtonInstance(final Object itemId) {
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
			// TODO Implement deleteLogically for taxonomy concepts
			recordsContainer = buttonsContainer;

			Table table = new Table($("TaxonomyManagementView.tableTitle", dataProvider.getSchema().getCode()), recordsContainer);
			table.setWidth("100%");
			table.setId("childrenTable");
			table.setColumnHeader("buttons", "");
			table.setColumnHeader("taxonomyChildrenCount", $("TaxonomyManagementView.childrenCount"));
			table.setColumnWidth(dataProvider.getSchema().getCode() + "_id", 120);
			table.setColumnWidth("buttons", 120);
			table.setPageLength(table.getItemIds().size());

			layout.addComponents(addButton, table);
			layout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);
		}
		return layout;
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> actionMenuButtons = new ArrayList<Button>();
		if (presenter.getCurrentConcept() != null && presenter.isPrincipalTaxonomy()) {
			actionMenuButtons.add(new LinkButton($("TaxonomyManagementView.manageAuthorizations")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.manageAuthorizationsButtonClicked();
				}
			});
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
	public void setFolders(RecordVODataProvider dataProvider) {
		Table foldersTable = new RecordVOTable(dataProvider);
		foldersTable.setSizeFull();
		//		foldersTable.addStyleName("foldersTable");
		foldersTable.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				RecordVOItem item = (RecordVOItem) event.getItem();
				RecordVO recordVO = item.getRecord();
				presenter.folderClicked(recordVO);
			}
		});
		Component oldFoldersComponent = foldersCompoment;
		foldersCompoment = foldersTable;
		foldersCompoment.addStyleName("foldersTable");
		tabSheet.replaceComponent(oldFoldersComponent, foldersCompoment);

	}

	@Override
	public void selectFoldersTab() {
		tabSheet.setSelectedTab(foldersCompoment);
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

	private Component buildDisplayList(List<String> list) {
		Component retentionRulesDisplayComponent;
		MetadataDisplayFactory metadataDisplayFactory = new MetadataDisplayFactory();
		List<Component> elementDisplayComponents = new ArrayList<Component>();
		for (String elementDisplayValue : list) {
			Component elementDisplayComponent = new ReferenceDisplay(elementDisplayValue);
			if (elementDisplayComponent != null) {
				elementDisplayComponent.setSizeFull();
				elementDisplayComponents.add(elementDisplayComponent);
			}
		}
		if (!elementDisplayComponents.isEmpty()) {
			retentionRulesDisplayComponent = metadataDisplayFactory.newCollectionValueDisplayComponent(elementDisplayComponents);
		} else {
			retentionRulesDisplayComponent = null;
		}
		return retentionRulesDisplayComponent;
	}
}

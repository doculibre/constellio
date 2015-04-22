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

import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.wrappers.structures.CommentFactory;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.containers.TaxonomyConceptsWithChildrenCountContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.schemas.StructureFactory;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class TaxonomyManagementViewImpl extends BaseViewImpl implements TaxonomyManagementView {

	TaxonomyManagementPresenter presenter;

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
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout layout = new VerticalLayout(buildRootConceptsTables());
		layout.setSizeFull();
		layout.setSpacing(true);

		if (presenter.conceptId != null) {
			layout.addComponentAsFirst(buildConceptRecordDisplay(false));
			layout.addComponent(buildConceptRecordDisplay(true));
		}

		return layout;
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

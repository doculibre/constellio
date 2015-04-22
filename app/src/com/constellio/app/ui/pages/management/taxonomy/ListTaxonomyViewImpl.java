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

import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class ListTaxonomyViewImpl extends BaseViewImpl implements ListTaxonomyView {
	private final ListTaxonomyPresenter presenter;
	private VerticalLayout layout;
	private Table taxonomies;
	private Button addButton;

	public ListTaxonomyViewImpl() {
		presenter = new ListTaxonomyPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListTaxonomyView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		taxonomies = buildTable();
		addButton = new AddButton() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.addButtonClicked();
			}
		};
		layout = new VerticalLayout(addButton, taxonomies);
		layout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);
		layout.setSpacing(true);
		layout.setWidth("100%");
		return layout;
	}

	@Override
	public void refreshTable() {
		Table table = buildTable();
		layout.replaceComponent(taxonomies, table);
		taxonomies = table;
	}

	private Table buildTable() {
		Container elements = new BeanItemContainer<>(TaxonomyVO.class, presenter.getTaxonomies());

		ButtonsContainer buttonsContainer = new ButtonsContainer(elements, "buttons");

		addButtons(buttonsContainer);
		elements = buttonsContainer;

		Table table = new Table($("ListTaxonomyView.tableTitle", elements.size()), elements);
		table.setPageLength(elements.size());
		table.setVisibleColumns("title", "buttons");
		table.setColumnHeader("title", $("ListTaxonomyView.titleColumn"));
		table.setColumnHeader("buttons", "");
		table.setColumnWidth("buttons", 88);
		table.setWidth("100%");

		return table;
	}

	private void addButtons(ButtonsContainer buttonsContainer) {
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.displayButtonClicked((TaxonomyVO) itemId);
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
						TaxonomyVO taxonomyVO = (TaxonomyVO) itemId;
						String taxonomyCode = taxonomyVO.getCode();
						presenter.editButtonClicked(taxonomyCode);

					}
				};
			}
		});
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				navigateTo().adminModule();
			}
		};
	}
}

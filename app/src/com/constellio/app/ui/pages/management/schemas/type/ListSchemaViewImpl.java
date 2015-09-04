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
package com.constellio.app.ui.pages.management.schemas.type;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Map;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.framework.buttons.*;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.SchemaVOLazyContainer;
import com.constellio.app.ui.framework.data.SchemaVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.vaadin.data.Container;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class ListSchemaViewImpl extends BaseViewImpl implements ListSchemaView, ClickListener {

	ListSchemaPresenter presenter;

	public ListSchemaViewImpl() {
		this.presenter = new ListSchemaPresenter(this);
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	@Override
	protected String getTitle() {
		return $("ListSchemaView.viewTitle");
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return this;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		presenter.backButtonClicked();
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		String parameters = event.getParameters();
		Map<String, String> paramsMap = ParamUtils.getParamsMap(parameters);
		presenter.setSchemaTypeCode(paramsMap.get("schemaTypeCode"));
		presenter.setParameters(paramsMap);

		VerticalLayout viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();

		Button addButton = new AddButton() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.addButtonClicked();
			}
		};

		viewLayout.addComponents(addButton, buildTables());
		viewLayout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);
		return viewLayout;
	}

	private Component buildTables() {
		final SchemaVODataProvider dataProvider = presenter.getDataProvider();
		Container schemaContainer = new SchemaVOLazyContainer(dataProvider);
		ButtonsContainer buttonsContainer = new ButtonsContainer(schemaContainer, "buttons");

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new EditButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						MetadataSchemaVO entity = dataProvider.getSchemaVO(index);
						presenter.editButtonClicked(entity);
					}
				};
			}
		});

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new MetadataButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						MetadataSchemaVO entity = dataProvider.getSchemaVO(index);
						presenter.editMetadataButtonClicked(entity);
					}
				};
			}
		});

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new FormOrderButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						MetadataSchemaVO entity = dataProvider.getSchemaVO(index);
						presenter.orderButtonClicked(entity);
					}
				};
			}
		});

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new FormDisplay() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						MetadataSchemaVO entity = dataProvider.getSchemaVO(index);
						presenter.formButtonClicked(entity);
					}
				};
			}
		});

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new SearchDisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						MetadataSchemaVO entity = dataProvider.getSchemaVO(index);
						presenter.searchButtonClicked(entity);
					}
				};
			}
		});

		schemaContainer = buttonsContainer;

		Table table = new Table($("ListSchemaView.tableTitle", schemaContainer.size()), schemaContainer);
		table.setSizeFull();
		table.setPageLength(table.size());
		table.setColumnHeader("buttons", "");
		table.setColumnHeader("caption", $("ListSchemaView.caption", schemaContainer.size()));
		table.setColumnExpandRatio("caption", 1);
		table.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				Integer index = (Integer) event.getItemId();
				MetadataSchemaVO entity = dataProvider.getSchemaVO(index);
				presenter.editButtonClicked(entity);
			}
		});

		return table;
	}

}

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
package com.constellio.app.ui.pages.management.schemas.schema;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Map;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.MetadataVOLazyContainer;
import com.constellio.app.ui.framework.data.MetadataVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class AddEditSchemaMetadataViewImpl extends BaseViewImpl implements AddEditSchemaMetadataView, ClickListener {

	AddEditSchemaMetadataPresenter presenter;
	private final int batchSize = 100;

	public AddEditSchemaMetadataViewImpl() {
		this.presenter = new AddEditSchemaMetadataPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		Map<String, String> params = ParamUtils.getParamsMap(event.getParameters());
		presenter.setSchemaCode(params.get("schemaCode"));
		presenter.setParameters(params);
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	@Override
	protected String getTitle() {
		return $("AddEditSchemaMetadataView.viewTitle", presenter.getSchemaVO().getLabel());
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

		Button addButton = new AddButton() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.addButtonClicked();
			}
		};

		VerticalLayout viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		viewLayout.addComponents(addButton, buildTables());
		viewLayout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);
		return viewLayout;
	}

	private Component buildTables() {
		final MetadataVODataProvider dataProvider = presenter.getDataProvider();

		Container recordsContainer = new MetadataVOLazyContainer(dataProvider, batchSize);
		ButtonsContainer buttonsContainer = new ButtonsContainer(recordsContainer, "buttons");
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new EditButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						MetadataVO entity = dataProvider.getMetadataVO(index);
						presenter.editButtonClicked(entity);
					}
				};
			}
		});
		recordsContainer = buttonsContainer;

		Table table = new Table($("AddEditSchemaMetadataView.tableTitle", recordsContainer.size()), recordsContainer);
		table.setSizeFull();
		table.setPageLength(table.size());
		table.setColumnHeader("caption", $("AddEditSchemaMetadataView.caption"));
		table.setColumnHeader("enabledCaption", $("AddEditSchemaMetadataView.enabledCaption"));
		table.setColumnHeader("valueCaption", $("AddEditSchemaMetadataView.valueCaption"));
		table.setColumnHeader("inputCaption", $("AddEditSchemaMetadataView.inputCaption"));
		table.setColumnHeader("requiredCaption", $("AddEditSchemaMetadataView.requiredCaption"));
		table.setColumnHeader("buttons", "");
		table.setColumnWidth("buttons", 60);

		return table;
	}
}

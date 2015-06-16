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
package com.constellio.app.modules.rm.ui.pages.retentionRule;

import static com.constellio.app.ui.i18n.i18n.$;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.modules.rm.ui.components.retentionRule.ExportRetentionRulesLink;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class ListRetentionRulesViewImpl extends BaseViewImpl implements ListRetentionRulesView {
	public static final String STYLE_NAME = "list-retention-rules";

	private final ListRetentionRulesPresenter presenter;
	private RecordVODataProvider dataProvider;
	
	private ExportRetentionRulesLink exportRetentionRulesLink;

	public ListRetentionRulesViewImpl() {
		presenter = new ListRetentionRulesPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.viewAssembled();
	}

	@Override
	public void setDataProvider(RecordVODataProvider dataProvider) {
		this.dataProvider = dataProvider;
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
	protected String getTitle() {
		return $("ListRetentionRulesView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		Button add = new AddButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addButtonClicked();
			}
		};
		
		exportRetentionRulesLink = new ExportRetentionRulesLink($("ListRetentionRulesView.exportRetentionRules"));

		RecordVOTable table = new RecordVOTable($("ListRetentionRulesView.tableTitle", dataProvider.size()), buildContainer());
		table.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		table.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 120);
		table.setSizeFull();
		table.setPageLength(table.size());

		VerticalLayout mainLayout = new VerticalLayout(exportRetentionRulesLink, add, table);
		mainLayout.setComponentAlignment(exportRetentionRulesLink, Alignment.TOP_RIGHT);
		mainLayout.setComponentAlignment(add, Alignment.TOP_RIGHT);
		mainLayout.setExpandRatio(table, 1);
		mainLayout.setSpacing(true);
		mainLayout.setSizeFull();

		return mainLayout;
	}

	private Container buildContainer() {
		final ButtonsContainer<RecordVOLazyContainer> rules = new ButtonsContainer<>(new RecordVOLazyContainer(dataProvider));
		rules.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						RecordVO recordVO = rules.getNestedContainer().getRecordVO((int) itemId);
						presenter.displayButtonClicked(recordVO);
					}
				};
			}
		});
		rules.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new EditButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						RecordVO recordVO = rules.getNestedContainer().getRecordVO((int) itemId);
						presenter.editButtonClicked(recordVO);
					}
				};
			}
		});
		rules.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						RecordVO recordVO = rules.getNestedContainer().getRecordVO((int) itemId);
						presenter.deleteButtonClicked(recordVO);
					}
				};
			}
		});
		return rules;
	}

}

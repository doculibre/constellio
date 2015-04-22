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

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class ListRetentionRulesViewImpl extends BaseViewImpl implements ListRetentionRulesView {

	public static final String STYLE_NAME = "liste-retention-rules";

	private RecordVODataProvider dataProvider;

	private VerticalLayout mainLayout;

	private Button addButton;

	private RecordVOTable table;

	private ListRetentionRulesPresenter presenter;

	public ListRetentionRulesViewImpl() {
		presenter = new ListRetentionRulesPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.viewAssembled();
	}

	@Override
	protected void afterViewAssembled(ViewChangeEvent event) {
	}

	@Override
	public void setDataProvider(RecordVODataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}

	@Override
	protected String getTitle() {
		return $("ListRetentionRulesView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		addButton = new AddButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addButtonClicked();
			}
		};

		table = new RecordVOTable(dataProvider);
		table.setSizeFull();
		table.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				if (event.getButton() == MouseButton.LEFT) {
					Object itemId = event.getItemId();
					RecordVOItem item = (RecordVOItem) table.getItem(itemId);
					RecordVO retentionRuleVO = item.getRecord();
					presenter.retentionRuleClicked(retentionRuleVO);
				}
			}
		});
		table.setPageLength(table.size());

		mainLayout.addComponents(addButton, table);
		mainLayout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);
		mainLayout.setExpandRatio(table, 1);

		return mainLayout;
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

}

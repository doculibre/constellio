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
package com.constellio.app.modules.rm.ui.components.decommissioning;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningListPresenter;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.app.ui.framework.components.BooleanLabel;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.ColumnGenerator;

public class ContainerDetailTableGenerator implements ColumnGenerator {
	public static final String IDENTIFIER = "identifier";
	public static final String FULL = "full";

	private final DecommissioningListPresenter presenter;

	public ContainerDetailTableGenerator(DecommissioningListPresenter presenter) {
		this.presenter = presenter;
	}

	public Table attachTo(Table table) {
		table.addGeneratedColumn(IDENTIFIER, this);
		table.setColumnHeader(IDENTIFIER, $("DecommissioningListView.containerDetails.id"));
		table.setColumnExpandRatio(IDENTIFIER, 1);

		table.addGeneratedColumn(FULL, this);
		table.setColumnHeader(FULL, $("DecommissioningListView.containerDetails.full"));
		table.setColumnAlignment(FULL, Align.CENTER);

		table.setVisibleColumns(IDENTIFIER, FULL);

		return table;
	}

	@Override
	public Object generateCell(Table source, Object itemId, Object columnId) {
		DecomListContainerDetail detail = (DecomListContainerDetail) itemId;

		switch ((String) columnId) {
		case IDENTIFIER:
			return new ReferenceDisplay(detail.getContainerRecordId());
		case FULL:
			return presenter.isEditable() ? buildFullCheckBox(detail) : buildFullDisplay(detail);
		}

		return null;
	}

	private Component buildFullDisplay(DecomListContainerDetail detail) {
		return new BooleanLabel(detail.isFull());
	}

	private Component buildFullCheckBox(final DecomListContainerDetail detail) {
		final CheckBox checkBox = new CheckBox();
		checkBox.setValue(detail.isFull());
		checkBox.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				presenter.containerStatusChanged(detail, checkBox.getValue());
			}
		});
		return checkBox;
	}
}

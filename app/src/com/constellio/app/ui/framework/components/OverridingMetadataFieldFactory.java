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
package com.constellio.app.ui.framework.components;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.ui.entities.MetadataVO;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;

public class OverridingMetadataFieldFactory extends MetadataFieldFactory {
	public interface FieldOverridePresenter extends Serializable {
		OverrideMode getOverride(String metadataCode);

		List<Choice> getChoices(String metadataCode);
	}

	public static class Choice implements Serializable {
		private final Object value;
		private final String caption;

		public Choice(Object value, String caption) {
			this.value = value;
			this.caption = caption;
		}

		public Object getValue() {
			return value;
		}

		public String getCaption() {
			return caption;
		}
	}

	public enum OverrideMode {
		NONE, DROPDOWN
	}

	private final FieldOverridePresenter presenter;

	public OverridingMetadataFieldFactory(FieldOverridePresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	protected Field<?> newSingleValueField(MetadataVO metadata) {
		switch (presenter.getOverride(metadata.getCode())) {
		case DROPDOWN:
			return buildDropdown(metadata);
		}
		return super.newSingleValueField(metadata);
	}

	private Field<?> buildDropdown(MetadataVO metadata) {
		ComboBox comboBox = new ComboBox();
		for (Choice choice : presenter.getChoices(metadata.getCode())) {
			comboBox.addItem(choice.getValue());
			comboBox.setItemCaption(choice.getValue(), choice.getCaption());
		}
		comboBox.setNullSelectionAllowed(!metadata.isRequired());
		postBuild(comboBox, metadata);
		return comboBox;
	}
}

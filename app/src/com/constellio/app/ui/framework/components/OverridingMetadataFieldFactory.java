package com.constellio.app.ui.framework.components;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.OverridingMetadataFieldFactory.FieldOverridePresenter;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;

import java.io.Serializable;
import java.util.List;

public class OverridingMetadataFieldFactory<T extends FieldOverridePresenter> extends MetadataFieldFactory {
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

	protected final T presenter;

	public OverridingMetadataFieldFactory(T presenter) {
		this.presenter = presenter;
	}

	@Override
	protected Field<?> newSingleValueField(MetadataVO metadata, String recordId) {
		switch (presenter.getOverride(metadata.getCode())) {
			case DROPDOWN:
				return buildDropdown(metadata);
		}
		return super.newSingleValueField(metadata, recordId);
	}

	private Field<?> buildDropdown(MetadataVO metadata) {
		ComboBox comboBox = new BaseComboBox();
		for (Choice choice : presenter.getChoices(metadata.getCode())) {
			comboBox.addItem(choice.getValue());
			comboBox.setItemCaption(choice.getValue(), choice.getCaption());
		}
		comboBox.setNullSelectionAllowed(!metadata.isRequired());
		postBuild(comboBox, metadata);
		return comboBox;
	}
}

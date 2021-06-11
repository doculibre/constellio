package com.constellio.app.ui.framework.components;

import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import org.jetbrains.annotations.NotNull;

public class ExpandableTextDisplay extends ExpandableDisplay {

	private Label view;

	private final Property<String> valueProperty;

	public ExpandableTextDisplay(String value) {
		this(new ObjectProperty(sanitizeValue(value), String.class));
	}

	public ExpandableTextDisplay(Property<String> valueProperty) {
		if (valueProperty == null) {
			valueProperty = new ObjectProperty<>("", String.class);
		}

		this.valueProperty = valueProperty;
	}

	@Override
	public Component buildMainComponent() {
		view = new Label();
		view.setContentMode(ContentMode.HTML);
		view.setPropertyDataSource(valueProperty);
		view.addValueChangeListener(event -> computeResponsive());

		return view;
	}

	@Override
	public void attach() {
		super.attach();
	}

	public String getValue() {
		return valueProperty.getValue();
	}

	public void setValue(String value) {
		valueProperty.setValue(sanitizeValue(value));
	}

	private static String sanitizeValue(String value) {
		String sanitizedString = value != null ? value : "";

		sanitizedString = removeTag(sanitizedString, "script");

		return sanitizedString;
	}

	private static String removeTag(@NotNull final String value, @NotNull final String tag) {
		final String searchTagBegining = "<" + tag;

		final String searchTagEnd = "</" + tag + ">";
		final int searchTagEndNameLength = searchTagEnd.length();

		String valueWithoutTag = value;

		int tagIndex = valueWithoutTag.indexOf(searchTagBegining);
		while (tagIndex >= 0) {
			int endTagIndex = valueWithoutTag.indexOf(searchTagEnd);
			if (endTagIndex >= tagIndex) {
				valueWithoutTag = valueWithoutTag.substring(0, tagIndex) + valueWithoutTag.substring(endTagIndex + searchTagEndNameLength);
			} else {
				valueWithoutTag = valueWithoutTag.substring(0, tagIndex);
			}

			tagIndex = valueWithoutTag.indexOf(searchTagBegining);
		}

		return valueWithoutTag;
	}
}

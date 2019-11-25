package com.constellio.app.modules.tasks.ui.components;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

public class FilterWindowButtonField extends CustomField {
	private final Field field;

	public FilterWindowButtonField(Field field) {
		this.field = field;
	}

	@Override
	public Class<?> getType() {
		return field.getType();
	}

	@Override
	protected Component initContent() {
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setWidth("100%");

		WindowButton.WindowConfiguration windowConfiguration = WindowButton.WindowConfiguration.modalDialog("600px", "300px");
		WindowButton windowButton = new WindowButton(null, $("DetailsFieldGroup.detailsWindow"), windowConfiguration) {
			@Override
			protected Component buildWindowContent() {
				VerticalLayout windowLayout = new VerticalLayout();

				windowLayout.setSpacing(true);

				field.setWidth("90%");
				field.setHeight("100%");

				Button closeButton = new BaseButton($("Ok")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						Object value = field.getValue();
						setValue(value);
						getWindow().close();
					}
				};
				closeButton.setStyleName(ValoTheme.BUTTON_PRIMARY);

				windowLayout.addComponents(field, closeButton);
				return windowLayout;
			}
		};
		windowButton.setIcon(null);
		windowButton.setCaption("...");
		windowButton.setWidth("100%");
		windowButton.setHeight("24px");

		horizontalLayout.addComponent(windowButton);
		horizontalLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);


		return horizontalLayout;
	}

	@Override
	public void setValue(Object newFieldValue) throws ReadOnlyException, ConversionException {
		if (field instanceof ListAddRemoveField) {
			setInternalValue(null);
		}
		super.setValue(newFieldValue);
	}
}

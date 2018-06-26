package com.constellio.app.modules.tasks.ui.components;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
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

        WindowButton.WindowConfiguration windowConfiguration = WindowButton.WindowConfiguration.modalDialog("50%", "500px");
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
                        setValue(field.getValue());
                        getWindow().close();
                    }
                };
                closeButton.setStyleName(ValoTheme.BUTTON_PRIMARY);

                windowLayout.addComponents(field, closeButton);

                return windowLayout;
            }
        };
        windowButton.setIcon(new ThemeResource("images/icons/actions/view.png"));

        horizontalLayout.addComponent(windowButton);
        horizontalLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);


        return horizontalLayout;
    }
}

package com.constellio.app.ui.framework.buttons;

import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

public abstract class ReportDisplayButton extends Button{
    public static final Resource ICON_RESOURCE = new ThemeResource("images/icons/config/report_mini.png");

    public ReportDisplayButton() {
        this($("ListSchemaView.button.report"));
    }

    public ReportDisplayButton(String caption) {
        super(caption);
        setIcon(ICON_RESOURCE);
        addStyleName(ValoTheme.BUTTON_BORDERLESS);
        addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ReportDisplayButton.this.buttonClick(event);
            }
        });
    }

    protected abstract void buttonClick(Button.ClickEvent event);
}

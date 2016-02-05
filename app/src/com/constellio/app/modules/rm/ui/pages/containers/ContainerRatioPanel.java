package com.constellio.app.modules.rm.ui.pages.containers;

import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.ui.framework.components.BaseDisplay;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

public class ContainerRatioPanel extends Panel {
    public ContainerRatioPanel(Double ratio) {
        this(ratio.toString());
    }

    public ContainerRatioPanel(String ratioAsStringOrMessage) {
        addStyleName(ValoTheme.PANEL_BORDERLESS);
        addStyleName(BaseDisplay.STYLE_NAME);
        addStyleName(RecordDisplay.STYLE_NAME);
        HorizontalLayout layout = new HorizontalLayout();
        Label ratioCaption = new Label($("ContainerRatioPanel.containerRatio"));
        ratioCaption.addStyleName(RecordDisplay.STYLE_CAPTION);
        ratioCaption.setSizeFull();
        layout.addComponent(ratioCaption);
        Label ratio = new Label(ratioAsStringOrMessage);
        ratio.setSizeFull();
        ratio.addStyleName(RecordDisplay.STYLE_VALUE);
        layout.addComponent(ratio);
        setContent(layout);
    }
}

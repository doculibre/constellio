package com.constellio.app.api.extensions.params;

import com.vaadin.ui.MenuBar;

public class ListSchemaExtraCommandReturnParams {
    MenuBar.Command command;
    String caption;

    public ListSchemaExtraCommandReturnParams(MenuBar.Command command, String caption){
        this.command = command;
        this.caption = caption;
    }

    public MenuBar.Command getCommand() {
        return command;
    }

    public String getCaption() {
        return caption;
    }
}

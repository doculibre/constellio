package com.constellio.app.api.extensions.params;

import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.ui.Component;

import java.util.List;

/**
 * Created by Constellio on 2017-02-14.
 */
public class AvailableActionsParam {
    private List<String> ids;
    private List<String> schemaTypeCodes;
    private User user;
    private Component component;

    public AvailableActionsParam(List<String> ids, List<String> schemaTypeCodes, User user, Component component) {
        this.ids = ids;
        this.schemaTypeCodes = schemaTypeCodes;
        this.user = user;
        this.component = component;
    }

    public List<String> getIds() {
        return ids;
    }

    public List<String> getSchemaTypeCodes() {
        return schemaTypeCodes;
    }

    public User getUser() {
        return user;
    }

    public Component getComponent() {
        return component;
    }
}

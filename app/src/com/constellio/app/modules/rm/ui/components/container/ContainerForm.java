package com.constellio.app.modules.rm.ui.components.container;

import com.vaadin.ui.Field;

import java.io.Serializable;

/**
 * Created by Constellio on 2017-01-11.
 */

public interface ContainerForm extends Serializable {

    @SuppressWarnings("unchecked")
    public Field<String> getTypeField();

    @SuppressWarnings("unchecked")
    public Field<String> getDecommissioningTypeField();

    @SuppressWarnings("unchecked")
    public Field<String> getAdministrativeUnitField();
}


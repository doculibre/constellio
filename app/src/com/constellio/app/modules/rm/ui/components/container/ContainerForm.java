package com.constellio.app.modules.rm.ui.components.container;

import java.io.Serializable;

import com.vaadin.ui.Field;

/**
 * Created by Constellio on 2017-01-11.
 */

public interface ContainerForm extends Serializable {

	@SuppressWarnings("unchecked")
	public Field<String> getTypeField();

	@SuppressWarnings("unchecked")
	public Field<String> getDecommissioningTypeField();

}


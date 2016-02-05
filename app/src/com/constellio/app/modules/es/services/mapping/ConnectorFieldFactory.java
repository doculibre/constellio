package com.constellio.app.modules.es.services.mapping;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.StructureFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class ConnectorFieldFactory implements StructureFactory {

	private GsonBuilder gsonBuilder;
	private Gson gson;

	public ConnectorFieldFactory() {
		gsonBuilder = new GsonBuilder();
		gson = gsonBuilder.create();
	}

	@Override
	public String toString(ModifiableStructure structure) {
		return gson.toJson(structure);
	}

	@Override
	public ModifiableStructure build(String structure) {
		ConnectorField connectorField = new ConnectorField();
		if (StringUtils.isNotBlank(structure)) {
			TypeToken<ConnectorField> listTypeToken = new TypeToken<ConnectorField>() {
			};

			connectorField = gson.fromJson(structure, listTypeToken.getType());
			connectorField.dirty = false;
		}
		return connectorField;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
}

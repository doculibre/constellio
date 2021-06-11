package com.constellio.model.entities.structures;

import com.constellio.model.entities.schemas.CombinedStructureFactory;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class MapStringListStringStructureFactory implements CombinedStructureFactory {

	private transient GsonBuilder gsonBuilder;
	private transient Gson gson;

	public MapStringListStringStructureFactory() {
		gsonBuilder = new GsonBuilder();
		gson = gsonBuilder.create();
	}

	@Override
	public String toString(ModifiableStructure structure) {
		if (gson == null) {
			gsonBuilder = new GsonBuilder();
			gson = gsonBuilder.create();
		}
		return gson.toJson(structure);
	}

	@Override
	public ModifiableStructure build(String structure) {
		if (gson == null) {
			gsonBuilder = new GsonBuilder();
			gson = gsonBuilder.create();
		}
		MapStringListStringStructure mapStringListStringStructure = new MapStringListStringStructure();
		if (StringUtils.isNotBlank(structure)) {
			TypeToken<MapStringListStringStructure> listTypeToken = new TypeToken<MapStringListStringStructure>() {
			};

			mapStringListStringStructure = gson.fromJson(structure, listTypeToken.getType());
			mapStringListStringStructure.dirty = false;
		}
		return mapStringListStringStructure;
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

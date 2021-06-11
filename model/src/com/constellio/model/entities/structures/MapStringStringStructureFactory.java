package com.constellio.model.entities.structures;

import com.constellio.model.entities.schemas.CombinedStructureFactory;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.IOException;
import java.io.ObjectInputStream;

public class MapStringStringStructureFactory implements CombinedStructureFactory {

	protected transient GsonBuilder gsonBuilder;
	protected transient Gson gson;

	public MapStringStringStructureFactory() {
		initTransient();
	}

	private void initTransient() {
		gsonBuilder = new GsonBuilder();
		gson = gsonBuilder.create();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		initTransient();
	}

	@Override
	public String toString(ModifiableStructure structure) {
		return gson.toJson(structure);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public ModifiableStructure build(String structure) {
		MapStringStringStructure mapStringStringStructure = new MapStringStringStructure();
		if (StringUtils.isNotBlank(structure)) {
			TypeToken<MapStringStringStructure> listTypeToken = new TypeToken<MapStringStringStructure>() {
			};

			mapStringStringStructure = gson.fromJson(structure, listTypeToken.getType());
			mapStringStringStructure.dirty = false;
		}
		return mapStringStringStructure;
	}

}

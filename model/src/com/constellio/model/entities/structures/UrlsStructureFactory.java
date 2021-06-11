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

public class UrlsStructureFactory implements CombinedStructureFactory {

	protected transient GsonBuilder gsonBuilder;
	protected transient Gson gson;

	public UrlsStructureFactory() {
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
		UrlsStructure urlsStructure = new UrlsStructure();
		if (StringUtils.isNotBlank(structure)) {
			TypeToken<UrlsStructure> listTypeToken = new TypeToken<UrlsStructure>() {
			};

			urlsStructure = gson.fromJson(structure, listTypeToken.getType());
			urlsStructure.dirty = false;
		}
		return urlsStructure;
	}
}

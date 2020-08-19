package com.constellio.model.entities.batchprocess;

import com.google.gson.Gson;

public interface JsonSerializable {
	public default String serialize() {
		Gson gson = new Gson();
		return gson.toJson(this).toString();
	}

	public static JsonSerializable deserialize(String jsonString, Class<? extends JsonSerializable> clazz) {
		Gson gson = new Gson();
		return gson.fromJson(jsonString, clazz);
	}
}

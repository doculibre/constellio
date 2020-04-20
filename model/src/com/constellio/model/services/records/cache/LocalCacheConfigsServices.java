package com.constellio.model.services.records.cache;

import com.constellio.model.services.factories.ModelLayerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.json.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class LocalCacheConfigsServices {

	ModelLayerFactory modelLayerFactory;

	public LocalCacheConfigsServices(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	public LocalCacheConfigs read(File file) {

		String jsonContent = null;
		if (file.exists()) {
			try {
				jsonContent = FileUtils.readFileToString(file, "UTF-8");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return parseFromJSON(jsonContent);
	}

	public void write(File file, LocalCacheConfigs cacheConfigs) {

		String jsonContent = writeToJSON(cacheConfigs);
		try {
			FileUtils.write(file, jsonContent, "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public LocalCacheConfigs parseFromJSON(String json) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();

		if (StringUtils.isNotBlank(json)) {
			TypeToken<LocalCacheConfigs> listTypeToken = new TypeToken<LocalCacheConfigs>() {
			};

			return gson.fromJson(json, listTypeToken.getType());
		} else {
			return new LocalCacheConfigs(0, new HashMap<>());
		}
	}

	public LocalCacheConfigs buildFull(String json) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();

		if (StringUtils.isNotBlank(json)) {
			TypeToken<LocalCacheConfigs> listTypeToken = new TypeToken<LocalCacheConfigs>() {
			};

			return gson.fromJson(json, listTypeToken.getType());
		} else {
			return null;
		}
	}

	public String writeToJSON(LocalCacheConfigs cacheConfigs) {

		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();
		return gson.toJson(cacheConfigs);

	}

}

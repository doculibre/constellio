package com.constellio.data.dao.services.factories;

import com.constellio.data.dao.managers.StatefulService;

public interface LayerFactory {

	public short getInstanceId();

	public String getInstanceName();

	public <T extends StatefulService> T add(T statefulService);

	public void initialize();

	public void close();

	public void close(boolean closeBottomLayers);

	public String toResourceName(String name);
}

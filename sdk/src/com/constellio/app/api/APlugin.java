package com.constellio.app.api;

import com.constellio.model.entities.modules.ConstellioPlugin;

public interface APlugin extends ConstellioPlugin {

	void doSomething(String withParameter);

}

package com.constellio.model.entities.modules;

import net.xeoh.plugins.base.Plugin;

public interface ConstellioPlugin extends Plugin {

	String DOCULIBRE = "DocuLibre";

	String getId();

	String getName();

	String getPublisher();

}
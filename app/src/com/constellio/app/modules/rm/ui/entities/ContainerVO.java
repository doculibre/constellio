package com.constellio.app.modules.rm.ui.entities;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;

import java.io.Serializable;
import java.util.List;

public class ContainerVO implements Serializable {
	private final String id;
	private final String caption;
	private final Double availableSize;
	private final List<String> administrativeUnits;

	public ContainerVO(String id, String caption, Double availableSize, List<String> administrativeUnits) {
		this.id = id;
		this.caption = caption;
		this.availableSize = availableSize;
		this.administrativeUnits = administrativeUnits;
	}

	public String getId() {
		return id;
	}

	public String getCaption() {
		return caption;
	}

	public Double getAvailableSize() {
		return availableSize;
	}

	public List<String> getAdministrativeUnits() {
		return administrativeUnits;
	}
}

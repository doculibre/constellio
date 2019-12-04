package com.constellio.app.api.extensions;

import com.vaadin.ui.Component;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public abstract class ExtraTabForSimpleSearchResultExtention {
	public abstract List<ExtraTabInfo> getExtraTabs();

	@AllArgsConstructor
	@Getter
	public static class ExtraTabInfo {
		private Component tabComponent;
		private String tabCaption;
	}
}

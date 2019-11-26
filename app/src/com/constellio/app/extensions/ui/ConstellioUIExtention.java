package com.constellio.app.extensions.ui;

import com.constellio.app.ui.application.ConstellioUI;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class ConstellioUIExtention {
	@Getter
	@AllArgsConstructor
	public static class ConstellioUIExtentionParams {
		ConstellioUI constellioUI;
	}

	public void addToInitialisation(ConstellioUIExtentionParams constellioUIExtentionParams) {

	}
}

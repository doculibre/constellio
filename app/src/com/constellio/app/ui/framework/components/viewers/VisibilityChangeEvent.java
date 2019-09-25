package com.constellio.app.ui.framework.components.viewers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class VisibilityChangeEvent {
	private boolean newVisibilily;
	@Setter
	private boolean removeThisVisiblityLisener;
}

package com.constellio.app.services.actionDisplayManager;

import lombok.Getter;
import org.apache.commons.lang.StringUtils;

@Getter
public class MenuPositionActionOptions {
	enum Position {
		BEFORE,
		AFTER,
		AT_END,
		AT_BEGINNING,
	}

	private Position position;
	private String relativeActionCode;

	private MenuPositionActionOptions(Position position, String relativeActionCode) {
		if (position == null || (position == Position.BEFORE && StringUtils.isBlank(relativeActionCode)
								 || position == Position.AFTER && StringUtils.isBlank(relativeActionCode))) {
			throw new IllegalArgumentException("position cannot be null and relativeActionCode has to not be blank when using BEFORE or AFTER");
		}

		this.position = position;
		this.relativeActionCode = relativeActionCode;
	}

	public static MenuPositionActionOptions displayActionAtEnd() {
		return new MenuPositionActionOptions(Position.AT_END, null);
	}

	public static MenuPositionActionOptions displayActionAtBeginning() {
		return new MenuPositionActionOptions(Position.AT_BEGINNING, null);
	}

	public static MenuPositionActionOptions displayActionBefore(String code) {
		return new MenuPositionActionOptions(Position.BEFORE, code);
	}

	public static MenuPositionActionOptions displayActionAfter(String code) {
		return new MenuPositionActionOptions(Position.AFTER, code);
	}
}

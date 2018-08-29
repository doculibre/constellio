package com.constellio.app.modules.restapi.document.enumeration;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum VersionType {
	MAJOR,
	MINOR
}

package com.constellio.app.modules.restapi.apis.v1.document.enumeration;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum VersionType {
	MAJOR,
	MINOR
}

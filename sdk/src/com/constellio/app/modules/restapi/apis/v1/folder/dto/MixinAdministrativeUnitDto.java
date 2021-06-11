package com.constellio.app.modules.restapi.apis.v1.folder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_WRITE;

public class MixinAdministrativeUnitDto {
	@JsonProperty(access = READ_WRITE)
	String code;

	@JsonProperty(access = READ_WRITE)
	String title;
}

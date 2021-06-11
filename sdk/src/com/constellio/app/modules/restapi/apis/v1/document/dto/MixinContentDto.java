package com.constellio.app.modules.restapi.apis.v1.document.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class MixinContentDto {
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	private String version;
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	private String hash;
}

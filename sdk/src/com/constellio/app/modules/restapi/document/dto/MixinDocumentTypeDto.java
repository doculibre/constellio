package com.constellio.app.modules.restapi.document.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class MixinDocumentTypeDto {
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	private String title;
}

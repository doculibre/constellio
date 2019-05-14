package com.constellio.app.modules.restapi.folder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class MixInFolderDto {
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	private String mediaType;
}

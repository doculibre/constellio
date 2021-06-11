package com.constellio.app.modules.restapi.apis.v1.folder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class MixinFolderTypeDto {
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	private String title;
}

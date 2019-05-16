package com.constellio.app.modules.restapi.folder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MixinFolderTypeDto {
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	private String title;
}

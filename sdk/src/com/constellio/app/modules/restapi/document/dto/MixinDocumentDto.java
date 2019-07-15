package com.constellio.app.modules.restapi.document.dto;

import com.constellio.app.modules.restapi.resource.dto.AceDto;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public abstract class MixinDocumentDto {
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	private List<AceDto> inheritedAces;
}

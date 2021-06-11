package com.constellio.app.modules.restapi.apis.v1.document.dto;

import com.constellio.app.modules.restapi.apis.v1.resource.dto.AceDto;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public abstract class MixinDocumentDto {
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	private List<AceDto> inheritedAces;
}

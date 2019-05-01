package com.constellio.app.modules.restapi.document.dto;

import com.constellio.app.modules.restapi.resource.dto.ResourceTypeDto;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;

@JsonRootName("DocumentType")
public class DocumentTypeDto extends ResourceTypeDto {

	@Builder
	public DocumentTypeDto(String id, String code, String title) {
		super(id, code, title);
	}

}

package com.constellio.app.modules.restapi.apis.v1.document.dto;

import com.constellio.app.modules.restapi.apis.v1.resource.dto.ResourceTypeDto;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonRootName("DocumentType")
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DocumentTypeDto extends ResourceTypeDto {

	@Builder
	public DocumentTypeDto(String id, String code, String title) {
		super(id, code, title);
	}

}

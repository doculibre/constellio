package com.constellio.app.modules.restapi.document.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonRootName("DocumentType")
public class DocumentTypeDto {
	@Schema(description = "When adding/updating a document, specify either the id or the code, not both.")
	private String id;
	private String code;
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String title;
}

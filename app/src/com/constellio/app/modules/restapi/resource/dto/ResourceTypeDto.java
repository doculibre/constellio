package com.constellio.app.modules.restapi.resource.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class ResourceTypeDto {
	@Schema(description = "When adding/updating a folder, specify either the id or the code, not both.")
	private String id;
	private String code;
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String title;
}

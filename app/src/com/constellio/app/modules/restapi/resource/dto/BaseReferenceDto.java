package com.constellio.app.modules.restapi.resource.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseReferenceDto {
	String id;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	String title;
}

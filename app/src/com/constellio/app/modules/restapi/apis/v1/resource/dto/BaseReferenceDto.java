package com.constellio.app.modules.restapi.apis.v1.resource.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseReferenceDto {
	@NotNull
	String id;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	String title;
}

package com.constellio.app.modules.restapi.core.exception.mapper;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonRootName("Error")
public class RestApiErrorResponse {
	private int code;
	private String description;
	@Schema(description = "Localized error message, based on the Accept-Language header or Constellio settings.")
	private String message;
}

package com.constellio.app.modules.restapi.apis.v1.document.dto;

import com.constellio.app.modules.restapi.apis.v1.document.enumeration.VersionType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
@JsonRootName("Content")
public class ContentDto {
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String version;
	@NotNull
	private VersionType versionType;
	@Schema(description = "Required when adding first version")
	private String filename;
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String hash;
}

package com.constellio.app.modules.restapi.apis.v1.resource.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@JsonRootName("ExtendedAttribute")
public class ExtendedAttributeDto {
	@NotNull
	private String key;
	private List<String> values;
}

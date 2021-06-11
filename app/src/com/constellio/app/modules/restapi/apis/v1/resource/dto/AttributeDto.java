package com.constellio.app.modules.restapi.apis.v1.resource.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@JsonRootName("Attribute")
public class AttributeDto {
	@NotNull
	private String key;
	@NotEmpty @Schema(required = true)
	private List<String> values;
}

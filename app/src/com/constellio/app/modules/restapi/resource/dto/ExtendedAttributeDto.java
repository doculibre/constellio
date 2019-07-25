package com.constellio.app.modules.restapi.resource.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@JsonRootName("ExtendedAttribute")
public class ExtendedAttributeDto {
	@NotNull
	private String key;
	@NotEmpty @Schema(required = true)
	private List<String> values;
}

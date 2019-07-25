package com.constellio.app.modules.restapi.resource.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Set;

@Data
@Builder
@EqualsAndHashCode(exclude = {"authorizationId"})
@JsonRootName("Ace")
public class AceDto {
	@JsonIgnore
	private String authorizationId;

	@NotEmpty @Schema(required = true, description = "Can be a username or a group code")
	private Set<String> principals;
	@NotEmpty @Schema(required = true, allowableValues = {"READ", "WRITE", "DELETE"})
	private Set<String> permissions;
	@Schema(type = "string", format = "date")
	private String startDate;
	@Schema(type = "string", format = "date")
	private String endDate;

}

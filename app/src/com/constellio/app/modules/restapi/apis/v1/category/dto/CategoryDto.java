package com.constellio.app.modules.restapi.apis.v1.category.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class CategoryDto {
	@NotNull
	String id;

	String title;
}

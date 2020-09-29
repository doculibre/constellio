package com.constellio.app.modules.restapi.category.dto;

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

package com.constellio.app.modules.restapi.cart.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartDto {
	private String id;
	private String owner;
	private String title;
}

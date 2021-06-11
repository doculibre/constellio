package com.constellio.app.modules.restapi.apis.v1.cart.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonRootName("CartContent")
public class CartContentUpdateDto {
	private List<String> itemsToAdd;
	private List<String> itemsToRemove;
}

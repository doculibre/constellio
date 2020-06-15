package com.constellio.app.modules.restapi.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserCredentialsConfigDto {
	private String localCode;
	private List<String> value;
}

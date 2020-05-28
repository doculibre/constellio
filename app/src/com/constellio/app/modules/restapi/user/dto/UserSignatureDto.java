package com.constellio.app.modules.restapi.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSignatureDto {
	private String filename;
}

package com.constellio.app.modules.restapi.user.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;

@JsonRootName("UserCredential")
@Data
@Builder
public class UserCredentialsDto {
	private String id;
}

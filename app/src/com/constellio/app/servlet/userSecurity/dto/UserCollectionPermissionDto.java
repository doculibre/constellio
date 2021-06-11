package com.constellio.app.servlet.userSecurity.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonRootName("UserCollectionPermission")
public class UserCollectionPermissionDto {
	private String collection;
	private List<String> permissionCodes;
}

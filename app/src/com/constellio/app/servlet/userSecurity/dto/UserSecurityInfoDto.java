package com.constellio.app.servlet.userSecurity.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonRootName("UserSecurityInfo")
public class UserSecurityInfoDto {
	private List<UserCollectionPermissionDto> collectionPermissions;
	private List<UserCollectionAccessDto> collectionAccess;
}

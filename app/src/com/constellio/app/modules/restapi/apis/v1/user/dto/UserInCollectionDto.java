package com.constellio.app.modules.restapi.apis.v1.user.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;

@JsonRootName("UserCollection")
@Data
@Builder
public class UserInCollectionDto {
	private String userId;
	private String collectionCode;
	private String collectionTitle;
}

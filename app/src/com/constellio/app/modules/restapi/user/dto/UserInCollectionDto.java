package com.constellio.app.modules.restapi.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInCollectionDto {
	private String userId;
	private String collectionCode;
	private String collectionTitle;
}

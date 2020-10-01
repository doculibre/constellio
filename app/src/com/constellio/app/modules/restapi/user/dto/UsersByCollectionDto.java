package com.constellio.app.modules.restapi.user.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@JsonRootName("UserCollections")
@Data
@Builder
public class UsersByCollectionDto {
	private List<UserInCollectionDto> usersByCollection;
}

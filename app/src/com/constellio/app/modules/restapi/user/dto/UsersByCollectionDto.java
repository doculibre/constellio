package com.constellio.app.modules.restapi.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UsersByCollectionDto {
	private List<UserInCollectionDto> usersByCollection;
}

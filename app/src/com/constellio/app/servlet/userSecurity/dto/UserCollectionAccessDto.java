package com.constellio.app.servlet.userSecurity.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonRootName("UserCollectionAccess")
public class UserCollectionAccessDto {
	private String collection;
	private boolean hasReadAccess;
	private boolean hasWriteAccess;
	private List<String> unitsWithReadAccess;
	private List<String> unitsWithWriteAccess;
}

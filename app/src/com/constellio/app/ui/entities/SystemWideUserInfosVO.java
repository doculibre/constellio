package com.constellio.app.ui.entities;

import com.constellio.model.entities.security.global.UserCredentialStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class SystemWideUserInfosVO {
	@Getter
	private String userCredentialId;

	@Getter
	private String username;

	@Getter
	private String firstName;

	@Getter
	private String lastName;

	@Getter
	private String email;

	@Getter
	private String title;

	@Getter
	private List<String> collections = new ArrayList<>();

	@Getter
	private Map<String, List<String>> groupCodes = new HashMap<>();

	@Getter
	private Map<String, List<String>> groupIds = new HashMap<>();

	@Getter
	private Map<String, UserCredentialStatus> statuses = new HashMap<>();
}

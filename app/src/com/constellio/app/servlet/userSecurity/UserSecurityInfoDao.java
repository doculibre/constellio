package com.constellio.app.servlet.userSecurity;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.servlet.BaseServletDao;
import com.constellio.app.servlet.userSecurity.dto.UserCollectionAccessDto;
import com.constellio.app.servlet.userSecurity.dto.UserCollectionPermissionDto;
import com.constellio.app.servlet.userSecurity.dto.UserSecurityInfoDto;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.users.SystemWideUserInfos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UserSecurityInfoDao extends BaseServletDao {

	public UserSecurityInfoDao() {

	}

	public UserSecurityInfoDto getSecurityInfo(String username) {
		return UserSecurityInfoDto.builder()
				.collectionPermissions(getPermissionInfo(username))
				.collectionAccess(getAccessInfo(username))
				.build();
	}

	private List<UserCollectionPermissionDto> getPermissionInfo(String username) {
		List<UserCollectionPermissionDto> infos = new ArrayList<>();

		SystemWideUserInfos userCredentials = userServices.getUserInfos(username);
		List<String> collections = userCredentials.getCollections();
		for (String collection : collections) {
			Set<String> codes = new HashSet<>();

			User user = getUserByUsername(username, collection);
			for (String userRoleCode : user.getAllRoles()) {
				Role role = user.getRolesDetails().getRole(userRoleCode);
				codes.addAll(role.getOperationPermissions());
			}

			List<String> permissionCodes = new ArrayList<>(codes);
			infos.add(UserCollectionPermissionDto.builder().collection(collection)
					.permissionCodes(permissionCodes)
					.build());
		}

		return infos;
	}

	private List<UserCollectionAccessDto> getAccessInfo(String username) {
		List<UserCollectionAccessDto> infos = new ArrayList<>();

		SystemWideUserInfos userCredentials = userServices.getUserInfos(username);
		List<String> collections = userCredentials.getCollections();
		for (String collection : collections) {
			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
			User user = getUserByUsername(username, collection);

			List<AdministrativeUnit> unitsWithReadAccess = rm.administrativeUnitStream()
					.filter(unit -> user.hasReadAccess().on(unit)).collect(Collectors.toList());

			List<AdministrativeUnit> unitsWithWriteAccess = rm.administrativeUnitStream()
					.filter(unit -> user.hasWriteAccess().on(unit)).collect(Collectors.toList());

			infos.add(UserCollectionAccessDto.builder().collection(collection)
					.hasReadAccess(user.hasCollectionReadAccess())
					.hasWriteAccess(user.hasCollectionWriteAccess())
					.unitsWithReadAccess(unitsWithReadAccess.stream().map(AdministrativeUnit::getId).collect(Collectors.toList()))
					.unitsWithWriteAccess(unitsWithWriteAccess.stream().map(AdministrativeUnit::getId).collect(Collectors.toList()))
					.build());
		}

		return infos;
	}
}

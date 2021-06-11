package com.constellio.app.modules.restapi.apis.v1.ace;

import com.constellio.app.modules.restapi.apis.v1.ace.dao.AceDao;
import com.constellio.app.modules.restapi.apis.v1.core.BaseDao;
import com.constellio.app.modules.restapi.apis.v1.core.BaseService;
import com.constellio.app.modules.restapi.apis.v1.resource.dto.AceDto;
import com.constellio.app.modules.restapi.apis.v1.resource.dto.AceListDto;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.restlet.engine.util.StringUtils;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.modules.restapi.core.util.ListUtils.nullToEmpty;
import static com.constellio.app.modules.restapi.core.util.Permissions.DELETE;
import static com.constellio.app.modules.restapi.core.util.Permissions.READ;
import static com.constellio.app.modules.restapi.core.util.Permissions.WRITE;
import static java.util.Arrays.asList;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;

public class AceService extends BaseService {

	private final static Map<String, List<Set<String>>> permissionGroups = ImmutableMap.<String, List<Set<String>>>builder()
			.put("R", Collections.singletonList(Collections.singleton(READ)))
			.put("RW", asList(asSet(READ, WRITE), Collections.singleton(WRITE)))
			.put("RD", asList(asSet(READ, DELETE), Collections.singleton(DELETE)))
			.put("RWD", asList(asSet(READ, WRITE, DELETE), asSet(WRITE, DELETE)))
			.build();

	@Inject
	private AceDao aceDao;

	public AceListDto getAces(Record record) {
		return aceDao.getAces(record);
	}

	public void addAces(User user, Record record, List<AceDto> aces) {
		aceDao.addAces(user, record, mergeByPermissionGroup(aces));
	}

	public boolean updateAces(User user, Record record, List<AceDto> aces) {
		List<AceDto> mergedAces = Lists.newLinkedList(mergeByPermissionGroup(aces));

		List<AceDto> acesToAdd = Lists.newArrayList();
		List<AceDto> acesToModify = Lists.newArrayList();
		List<AceDto> acesToRemove = Lists.newArrayList();

		List<AceDto> existingAces = Lists.newLinkedList(aceDao.getAces(record).getDirectAces());

		// filter identical aces
		Iterator<AceDto> iterator = mergedAces.iterator();
		while (iterator.hasNext()) {
			AceDto currentAce = iterator.next();
			boolean removed = existingAces.remove(currentAce);
			if (removed) {
				iterator.remove();
			}
		}

		for (AceDto ace : mergedAces) {
			if (existingAces.size() == 0) {
				acesToAdd.add(ace);
				continue;
			}
			ace.setAuthorizationId(existingAces.remove(0).getAuthorizationId());
			acesToModify.add(ace);
		}
		acesToRemove.addAll(existingAces);

		if (!acesToAdd.isEmpty()) {
			aceDao.addAces(user, record, acesToAdd);
		}
		if (!acesToModify.isEmpty()) {
			aceDao.updateAces(user, record, acesToModify);
		}
		if (!acesToRemove.isEmpty()) {
			aceDao.removeAces(user, record, acesToRemove);
		}

		return !acesToAdd.isEmpty() || !acesToModify.isEmpty() || !acesToRemove.isEmpty();
	}

	@Override
	protected BaseDao getDao() {
		return aceDao;
	}

	private List<AceDto> mergeByPermissionGroup(List<AceDto> aces) {
		Map<String, List<AceDto>> acesByPermissionGroup = Maps.newHashMap();

		for (AceDto ace : aces) {
			String group = getPermissionGroup(ace);
			boolean merged = false;
			for (AceDto groupedAce : nullToEmpty(acesByPermissionGroup.get(group))) {
				if (StringUtils.nullToEmpty(groupedAce.getStartDate()).equals(StringUtils.nullToEmpty(ace.getStartDate())) &&
					StringUtils.nullToEmpty(groupedAce.getEndDate()).equals(StringUtils.nullToEmpty(ace.getEndDate()))) {
					groupedAce.getPrincipals().addAll(ace.getPrincipals());
					merged = true;
					break;
				}
			}

			if (!merged) {
				if (acesByPermissionGroup.get(group) != null) {
					acesByPermissionGroup.get(group).add(ace);
				} else {
					acesByPermissionGroup.put(group, Lists.newArrayList(ace));
				}
			}
		}

		List<AceDto> mergedAces = Lists.newArrayList();
		for (List<AceDto> groupedAces : acesByPermissionGroup.values()) {
			mergedAces.addAll(groupedAces);
		}
		return mergedAces;
	}

	private String getPermissionGroup(AceDto ace) {
		for (Map.Entry<String, List<Set<String>>> entry : permissionGroups.entrySet()) {
			for (Set<String> set : entry.getValue()) {
				if (set.equals(ace.getPermissions())) {
					return entry.getKey();
				}
			}
		}

		throw new IllegalArgumentException(String.format("Invalid permission list : %s", ace.getPermissions()));
	}
}

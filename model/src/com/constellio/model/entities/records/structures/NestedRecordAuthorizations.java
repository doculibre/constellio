package com.constellio.model.entities.records.structures;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.AbstractMapBasedSeparatedStructureFactory;
import com.constellio.model.entities.schemas.StructureInstanciationParams;
import com.constellio.model.entities.security.Role;
import lombok.Builder;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.ignite.internal.util.lang.GridFunc.asList;

public class NestedRecordAuthorizations extends AbstractMapBasedSeparatedStructureFactory.MapBasedStructure {

	RecordId targetId;

	String collection;

	String targetSchemaType;

	interface NestedRecordAuthorizationsAlteration {
		void alter(List<NestedRecordAuthorization> authorizations);
	}

	public enum NestedRecordAccessType {
		//DON'T CHANGE ORDER OR ADD ELEMENTS BEFORE RWD!!
		R(asList(Role.READ)),
		RW(asList(Role.READ, Role.WRITE)),
		RD(asList(Role.READ, Role.DELETE)),
		RWD(asList(Role.READ, Role.WRITE, Role.DELETE));

		List<String> roles;

		NestedRecordAccessType(List<String> roles) {
			this.roles = roles;
		}
	}

	public NestedRecordAuthorizations(StructureInstanciationParams params) {
		this.targetId = params.getId();
		this.collection = params.getCollection();
		this.targetSchemaType = params.getSchemaType();
	}

	public NestedRecordAuthorizations() {
	}

	public List<NestedRecordAuthorization> getAuthorizations() {
		List<Integer> principals = getList("principals");
		List<Integer> principalsCount = getList("principalsCount");
		List<Integer> accessTypes = getList("accessType");
		List<Integer> sources = getList("source");
		List<Boolean> negatives = getList("negative");
		List<Boolean> cascadings = getList("cascading");

		List<NestedRecordAuthorization> authorizations = new ArrayList<>();

		if (principalsCount != null) {
		int currentPrincipalsIndex = 0;
		for (int i = 0; i < principalsCount.size(); i++) {
			List<RecordId> authPrincipalIds = new ArrayList<>();
			for (int j = 0; j < principalsCount.get(i); j++) {
				authPrincipalIds.add(RecordId.id(principals.get(currentPrincipalsIndex++)));
			}

				NestedRecordAccessType accessType = NestedRecordAccessType.values()[accessTypes.get(i)];
				boolean negative = negatives.get(i);
				boolean cascading = cascadings.get(i);

				RecordId source = sources.get(i) == -1 ? null : RecordId.id(sources.get(i));

				authorizations.add(NestedRecordAuthorization.builder().principals(authPrincipalIds).accessType(accessType)
						.nonPersistedTargetId(targetId)
						.nonPersistedCollection(collection)
						.nonPersistedTargetSchemaType(targetSchemaType)
						.nonPersistedAuthId(targetId + "-" + i )
						.negative(negative).cascading(cascading).source(source)
						.build());

			}
		}

		return authorizations;


	}

	public void alter(NestedRecordAuthorizationsAlteration alteration) {
		List<NestedRecordAuthorization> authorizations = new ArrayList<>(getAuthorizations());
		alteration.alter(authorizations);
		write(authorizations);
	}

	public NestedRecordAuthorizations addAll(List<NestedRecordAuthorization> newAuths) {
		List<NestedRecordAuthorization> authorizations = new ArrayList<>(getAuthorizations());
		authorizations.addAll(newAuths);
		write(authorizations);

		return this;
	}
	public void add(NestedRecordAuthorization newAuth) {
		List<NestedRecordAuthorization> authorizations = new ArrayList<>(getAuthorizations());
		authorizations.add(newAuth);
		write(authorizations);
	}

	public void add(NestedRecordAuthorization.NestedRecordAuthorizationBuilder newAuthBuilder) {
		List<NestedRecordAuthorization> authorizations = new ArrayList<>(getAuthorizations());
		authorizations.add(newAuthBuilder.build());
		write(authorizations);
	}

	public static NestedRecordAuthorization.NestedRecordAuthorizationBuilder nonCascadingAuthToPrincipals(List<?> principals) {
		List<RecordId> recordIds = RecordId.toIds(principals);
		return NestedRecordAuthorization.builder().principals(recordIds).cascading(false);
	}

	public static NestedRecordAuthorization.NestedRecordAuthorizationBuilder nonCascadingAuthToPrincipals(Record... principals) {
		List<RecordId> recordIds = RecordId.toIds(asList(principals));
		return NestedRecordAuthorization.builder().principals(recordIds).cascading(false);
	}

	public static NestedRecordAuthorization.NestedRecordAuthorizationBuilder nonCascadingAuthToPrincipals(
			RecordWrapper... principals) {
		List<RecordId> recordIds = asList(principals).stream().map(RecordWrapper::getWrappedRecordId).collect(Collectors.toList());
		return NestedRecordAuthorization.builder().principals(recordIds).cascading(false);
	}

	public static NestedRecordAuthorization.NestedRecordAuthorizationBuilder nonCascadingAuthToPrincipals(
			RecordId... principals) {
		List<RecordId> recordIds = RecordId.toIds(asList(principals));
		return NestedRecordAuthorization.builder().principals(recordIds).cascading(false);
	}

	public static NestedRecordAuthorization.NestedRecordAuthorizationBuilder nonCascadingAuthToPrincipals(
			String... principals) {
		List<RecordId> recordIds = RecordId.toIds(asList(principals));
		return NestedRecordAuthorization.builder().principals(recordIds).cascading(false);
	}

	private void write(List<NestedRecordAuthorization> authorizations) {
		List<Integer> principals = new ArrayList<>();
		List<Integer> principalsCount = new ArrayList<>();
		List<Integer> accessTypes = new ArrayList<>();
		List<Integer> sources = new ArrayList<>();
		List<Boolean> negatives = new ArrayList<>();
		List<Boolean> cascadings = new ArrayList<>();

		for (NestedRecordAuthorization auth : authorizations) {
			auth.principals.forEach((id) -> {
				principals.add(id.intValue());
			});
			principalsCount.add(auth.principals.size());
			accessTypes.add(auth.accessType.ordinal());
			sources.add(auth.source == null ? -1 : auth.source.intValue());
			negatives.add(auth.negative);
			cascadings.add(auth.cascading);
		}

		set("principals", principals);
		set("principalsCount", principalsCount);
		set("accessType", accessTypes);
		set("source", sources);
		set("negative", negatives);
		set("cascading", cascadings);

	}

	@Builder
	public static class NestedRecordAuthorization implements Authorization {

		List<RecordId> principals;
		NestedRecordAccessType accessType;
		RecordId source;
		boolean negative;
		boolean cascading;

		String nonPersistedCollection;
		String nonPersistedTargetSchemaType;
		RecordId nonPersistedTargetId;
		String nonPersistedAuthId;


		@Override
		public String getId() {
			return nonPersistedAuthId;
		}

		@Override
		public String getCollection() {
			return nonPersistedCollection;
		}

		@Override
		public List<String> getPrincipals() {
			return principals.stream().map(RecordId::stringValue).collect(Collectors.toList());
		}

		@Override
		public List<RecordId> getPrincipalsIds() {
			return principals;
		}

		@Override
		public String getSharedBy() {
			return null;
		}

		@Override
		public List<String> getRoles() {
			return accessType.roles;
		}

		@Override
		public LocalDate getStartDate() {
			return null;
		}

		@Override
		public LocalDate getLastTokenRecalculate() {
			return null;
		}

		@Override
		public LocalDate getEndDate() {
			return null;
		}

		@Override
		public String getTargetSchemaType() {
			return nonPersistedTargetSchemaType;
		}

		@Override
		public boolean isOverrideInherited() {
			return false;
		}

		@Override
		public String getTarget() {
			return nonPersistedTargetId == null ? null : nonPersistedTargetId.stringValue();
		}

		@Override
		public int getTargetRecordIntId() {
			return nonPersistedTargetId == null ? null : nonPersistedTargetId.intValue();
		}

		@Override
		public RecordId getTargetRecordId() {
			return nonPersistedTargetId;
		}

		@Override
		public boolean isSynced() {
			return false;
		}

		@Override
		public boolean isFutureAuthorization() {
			return false;
		}

		@Override
		public boolean isNegative() {
			return negative;
		}

		@Override
		public boolean isActiveAuthorization() {
			return true;
		}

		@Override
		public RecordId getSource() {
			return source;
		}

		@Override
		public boolean isCascading() {
			return cascading;
		}
	}

	public static final NestedRecordAuthorizations UNMODIFIABLE_EMPTY = new NestedRecordAuthorizations(
			new StructureInstanciationParams(null,null,null));
}

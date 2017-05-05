package com.constellio.model.services.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;

public class SecurityTokenManager implements StatefulService {
	List<TokenProvider> providers = new ArrayList<>();
	List<String> schemaTypesWithoutSecurity = new ArrayList<>();
	List<PublicTypeWithCondition> globalPermissionSecurizedSchemaTypes = new ArrayList<>();
	ModelLayerFactory modelLayerFactory;

	public SecurityTokenManager(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	@Override
	public void initialize() {

	}

	@Override
	public void close() {

	}

	public UserTokens getTokens(final User user) {

		UserTokens tokens = new UserTokens();
		for (TokenProvider provider : providers) {
			tokens.add(provider.getTokensFor(user.getUsername(), user.getCollection()));
		}
		return tokens;
	}

	public void registerProvider(TokenProvider provider) {
		providers.add(provider);
	}

	public void registerPublicTypeWithCondition(String schemaType, GlobalSecurizedTypeCondition condition) {

		PublicTypeWithCondition publicTypeWithCondition = new PublicTypeWithCondition();
		publicTypeWithCondition.condition = condition;
		publicTypeWithCondition.schemaType = schemaType;

		this.globalPermissionSecurizedSchemaTypes.add(publicTypeWithCondition);
	}

	public void registerPublicType(String publicType) {
		schemaTypesWithoutSecurity.add(publicType);
	}

	public void unregisterPublicType(String publicType) {
		schemaTypesWithoutSecurity.remove(publicType);
	}

	public List<String> getSchemaTypesWithoutSecurity() {
		return schemaTypesWithoutSecurity;
	}

	public Set<String> getGlobalPermissionSecurizedSchemaTypesVisibleBy(User user, String access) {
		Set<String> types = new HashSet<>();
		for (PublicTypeWithCondition publicTypeWithCondition : globalPermissionSecurizedSchemaTypes) {
			if (publicTypeWithCondition.condition.hasGlobalAccess(user, access)) {
				types.add(publicTypeWithCondition.schemaType);
			}
		}
		return types;
	}

	public boolean hasGlobalTypeAccess(User user, String typeCode, String access) {
		for (PublicTypeWithCondition publicTypeWithCondition : globalPermissionSecurizedSchemaTypes) {
			if (publicTypeWithCondition.schemaType.equals(typeCode)
					&& publicTypeWithCondition.condition.hasGlobalAccess(user, access)) {
				return true;
			}
		}
		return false;
	}

	public static class UserTokens {
		private final List<String> allowTokens;
		private final List<String> denyTokens;
		private final List<String> shareAllowTokens;
		private final List<String> shareDenyTokens;

		public UserTokens() {
			this(new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
		}

		public UserTokens(List<String> allowTokens) {
			this(allowTokens, new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
		}

		public UserTokens(
				List<String> allowTokens, List<String> denyTokens, List<String> shareAllowTokens, List<String> shareDenyTokens) {
			this.allowTokens = allowTokens;
			this.denyTokens = denyTokens;
			this.shareAllowTokens = shareAllowTokens;
			this.shareDenyTokens = shareDenyTokens;
		}

		public List<String> getAllowTokens() {
			return allowTokens;
		}

		public List<String> getDenyTokens() {
			return denyTokens;
		}

		public List<String> getShareAllowTokens() {
			return shareAllowTokens;
		}

		public List<String> getShareDenyTokens() {
			return shareDenyTokens;
		}

		public void add(UserTokens tokens) {
			allowTokens.addAll(tokens.allowTokens);
			denyTokens.addAll(tokens.denyTokens);
			shareAllowTokens.addAll(tokens.shareAllowTokens);
			shareDenyTokens.addAll(tokens.shareDenyTokens);
		}
	}

	public interface TokenProvider {
		UserTokens getTokensFor(String username, String collection);
	}

	private static class PublicTypeWithCondition {
		String schemaType;
		GlobalSecurizedTypeCondition condition;
	}
}

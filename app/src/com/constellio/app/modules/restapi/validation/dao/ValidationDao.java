package com.constellio.app.modules.restapi.validation.dao;

import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.core.util.MapUtils;
import com.constellio.model.entities.security.global.UserCredential;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ValidationDao extends BaseDao {

    public List<String> getUserTokens(String serviceKey) {
        return getUserTokens(serviceKey, false);
    }

    public List<String> getUserTokens(String serviceKey, boolean sortByDescDate) {
        String username = getUsernameByServiceKey(serviceKey);
        if (username == null) return Collections.emptyList();

        UserCredential userCredential = userServices.getUser(username);
        if (userCredential == null) return Collections.emptyList();

        if (sortByDescDate) {
            Map<String, LocalDateTime> sortedTokens = MapUtils.sortByReverseValue(userCredential.getAccessTokens());
            return new ArrayList<>(sortedTokens.keySet());
        }
        return new ArrayList<>(userCredential.getAccessTokens().keySet());
    }

    public boolean isUserAuthenticated(String token, String serviceKey) {
        return getUserTokens(serviceKey).contains(token);
    }

}

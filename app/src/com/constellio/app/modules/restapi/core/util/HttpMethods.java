package com.constellio.app.modules.restapi.core.util;

import com.google.common.collect.Sets;
import lombok.experimental.UtilityClass;

import java.util.Set;

@UtilityClass
public final class HttpMethods {
    public static final String DELETE = "DELETE";
    public static final String GET = "GET";
    public static final String PATCH = "PATCH";
    public static final String POST = "POST";
    public static final String PUT = "PUT";

    private static final Set<String> ALLOWED_METHODS = Sets.newHashSet(DELETE, GET, PATCH, POST, PUT);

    public static boolean contains(String method) {
        return ALLOWED_METHODS.contains(method);
    }
}

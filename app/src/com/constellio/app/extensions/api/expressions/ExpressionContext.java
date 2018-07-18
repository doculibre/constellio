package com.constellio.app.extensions.api.expressions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExpressionContext {

    public static Map<String, Object> WORKFLOW_CONTEXT = new ConcurrentHashMap<String, Object>();
}

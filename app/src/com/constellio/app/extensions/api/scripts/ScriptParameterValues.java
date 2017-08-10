package com.constellio.app.extensions.api.scripts;

import java.util.Map;

public class ScriptParameterValues {

    Map<ScriptParameter, Object> values;

    public ScriptParameterValues(Map<ScriptParameter, Object> values) {
        this.values = values;
    }

    public <T> T get(ScriptParameter parameter) {
        return (T) values.get(parameter);
    }

}

package com.constellio.app.extensions.api.scripts;

public class ScriptParameter {

    ScriptParameterType type;

    String label;

    boolean required;

    public ScriptParameter(ScriptParameterType type, String label, boolean required) {
        this.type = type;
        this.label = label;
        this.required = required;
    }

    public ScriptParameterType getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScriptParameter that = (ScriptParameter) o;

        if (type != that.type) return false;
        return label != null ? label.equals(that.label) : that.label == null;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        return result;
    }
}

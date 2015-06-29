package org.humbird.soa.common.utils;

/**
 * A helper class for <a href="http://json-schema.org/">JSON schema</a>.
 */
public final class JsonSchemaHelper {

    private JsonSchemaHelper() {
    }

    /**
     * Gets the JSon schema type.
     *
     * @param   type the java type
     * @return  the json schema type, is never null, but returns <tt>object</tt> as the generic type
     */
    public static String getType(Class<?> type) {
        if (type.isEnum()) {
            return "enum";
        } else if (type.isArray()) {
            return "array";
        }

        String primitive = getPrimitiveType(type);
        if (primitive != null) {
            return primitive;
        }

        return "object";
    }

    /**
     * Gets the JSon schema primitive type.
     *
     * @param   type the java type
     * @return  the json schema primitive type, or <tt>null</tt> if not a primitive
     */
    public static String getPrimitiveType(Class<?> type) {
        String name = type.getCanonicalName();

        // special for byte[] or Object[] as its common to use
        if ("java.lang.byte[]".equals(name) || "byte[]".equals(name)) {
            return "string";
        } else if ("java.lang.Byte[]".equals(name) || "Byte[]".equals(name)) {
            return "array";
        } else if ("java.lang.Object[]".equals(name) || "Object[]".equals(name)) {
            return "array";
        } else if ("java.lang.String[]".equals(name) || "String[]".equals(name)) {
            return "array";
            // and these is common as well
        } else if ("java.lang.String".equals(name) || "String".equals(name)) {
            return "string";
        } else if ("java.lang.Boolean".equals(name) || "Boolean".equals(name)) {
            return "boolean";
        } else if ("boolean".equals(name)) {
            return "boolean";
        } else if ("java.lang.Integer".equals(name) || "Integer".equals(name)) {
            return "integer";
        } else if ("int".equals(name)) {
            return "integer";
        } else if ("java.lang.Long".equals(name) || "Long".equals(name)) {
            return "integer";
        } else if ("long".equals(name)) {
            return "integer";
        } else if ("java.lang.Short".equals(name) || "Short".equals(name)) {
            return "integer";
        } else if ("short".equals(name)) {
            return "integer";
        } else if ("java.lang.Byte".equals(name) || "Byte".equals(name)) {
            return "integer";
        } else if ("byte".equals(name)) {
            return "integer";
        } else if ("java.lang.Float".equals(name) || "Float".equals(name)) {
            return "number";
        } else if ("float".equals(name)) {
            return "number";
        } else if ("java.lang.Double".equals(name) || "Double".equals(name)) {
            return "number";
        } else if ("double".equals(name)) {
            return "number";
        }

        return null;
    }

}

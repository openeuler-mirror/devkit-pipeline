package com.huawei.devkit.pipeline.utils;

import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JfrMethodSignatureParser {

    private static final Map<Character, String> TYPE_MAPPINGS;

    static {
        TYPE_MAPPINGS = new HashMap<>();
        TYPE_MAPPINGS.put('V', "void");
        TYPE_MAPPINGS.put('Z', "boolean");
        TYPE_MAPPINGS.put('B', "byte");
        TYPE_MAPPINGS.put('C', "char");
        TYPE_MAPPINGS.put('D', "double");
        TYPE_MAPPINGS.put('F', "float");
        TYPE_MAPPINGS.put('I', "int");
        TYPE_MAPPINGS.put('J', "long");
        TYPE_MAPPINGS.put('S', "short");
    }

    public static String convertMethodSignature(String signature, String methodName) {
        List<String> params = new ArrayList<>();
        int end = parseParams(signature, 1, params);
        String paramDesc = "(" + Strings.join(params.iterator(), ',') + ")";
        String returnDesc = parseReturn(signature, end);
        return returnDesc + " " + methodName + paramDesc;
    }

    public static String convertMethodSignatureWithoutReturnType(String signature, String methodName) {
        List<String> params = new ArrayList<>();
        parseParams(signature, 1, params);
        String paramDesc = "(" + Strings.join(params.iterator(), ',') + ")";
        return methodName + paramDesc;
    }

    private static String parseReturn(String signature, int indexForRetrun) {
        if (indexForRetrun < signature.length()) {
            char returnType = signature.charAt(indexForRetrun);
            if (returnType == 'L') {
                int endIndex = signature.indexOf(';', indexForRetrun);
                if (endIndex == -1) {
                    throw new IllegalArgumentException("Invalid method signature: " + signature);
                }
                return signature.substring(indexForRetrun + 1, endIndex).replace('/', '.');
            } else if (returnType == '[') {
                // Skip over array dimensions
                int arrayDimension = 0;
                while (indexForRetrun < signature.length() && signature.charAt(indexForRetrun) == '[') {
                    indexForRetrun++;
                    arrayDimension++;
                }
                if (indexForRetrun == signature.length()) {
                    throw new IllegalArgumentException("Invalid method signature: " + signature);
                }
                StringBuilder builder = new StringBuilder();
                setNameToClassParamDesc(signature, indexForRetrun, builder);
                return builder.append("[]".repeat(arrayDimension)).toString();
            } else if (TYPE_MAPPINGS.containsKey(returnType)) {
                return TYPE_MAPPINGS.get(returnType);
            }
        }
        throw new IllegalArgumentException("Invalid type character in signature: " + signature);
    }

    private static int parseParams(String signature, int i, List<String> params) {
        while (i < signature.length()) {
            char c = signature.charAt(i);
            if (c == ')') {
                i++;
                break; // End of parameters, start of return type
            } else if (c == 'L') {
                int endIndex = signature.indexOf(';', i);
                if (endIndex == -1) {
                    throw new IllegalArgumentException("Invalid method signature: " + signature);
                }
                String className = signature.substring(i + 1, endIndex).replace('/', '.');
                params.add(className);
                i = endIndex + 1;
            } else if (c == '[') {
                // Skip over array dimensions
                int arrayDimension = 0;
                while (i < signature.length() && signature.charAt(i) == '[') {
                    i++;
                    arrayDimension++;
                }
                if (i == signature.length()) {
                    throw new IllegalArgumentException("Invalid method signature: " + signature);
                }
                StringBuilder builder = new StringBuilder();
                i = setNameToClassParamDesc(signature, i, builder);
                params.add(builder.append("[]".repeat(arrayDimension)).toString());
            } else if (TYPE_MAPPINGS.containsKey(c)) {
                params.add(TYPE_MAPPINGS.get(c));
                i++;
            } else {
                throw new IllegalArgumentException("Invalid type character in signature: " + c);
            }
        }
        return i;
    }

    private static int setNameToClassParamDesc(String signature, int index, StringBuilder builder) {
        char c = signature.charAt(index);
        if (c == 'L') {
            int endIndex = signature.indexOf(';', index);
            if (endIndex == -1) {
                throw new IllegalArgumentException("Invalid method signature: " + signature);
            }
            String className = signature.substring(index + 1, endIndex).replace('/', '.');
            builder.append(className);
            return endIndex + 1;
        } else if (TYPE_MAPPINGS.containsKey(c)) {
            builder.append(TYPE_MAPPINGS.get(c));
            return index + 1;
        }
        throw new IllegalArgumentException("Invalid method signature: " + signature);
    }
}
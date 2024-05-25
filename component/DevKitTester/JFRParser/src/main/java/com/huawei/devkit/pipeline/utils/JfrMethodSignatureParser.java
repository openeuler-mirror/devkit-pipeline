package com.huawei.devkit.pipeline.utils;

import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JfrMethodSignatureParser {

    private static final Map<Character, String> TYPE_MAPPINGS;
    private static final int PARAMS_START_INDEX = 1;

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

    /**
     * 转换成正常方法签名
     *
     * @param signature  方法签名
     * @param methodName 方法名
     * @return 正常的方法签名
     */
    public static String convertMethodSignature(String signature, String methodName) {
        List<String> params = new ArrayList<>();
        int end = parseParams(signature, params);
        String returnDesc = parseReturn(signature, end);
        return returnDesc + " " + methodName + "(" + Strings.join(params.iterator(), ',') + ")";
    }

    /**
     * 转换成没有返回值正常方法签名
     *
     * @param signature  方法签名
     * @param methodName 方法名
     * @return 正常的方法签名
     */
    public static String convertMethodSignatureWithoutReturnType(String signature, String methodName) {
        List<String> params = new ArrayList<>();
        parseParams(signature, params);
        return methodName + "(" + Strings.join(params.iterator(), ',') + ")";
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

    private static int parseParams(String signature, List<String> params) {
        int index = PARAMS_START_INDEX;
        while (index < signature.length()) {
            char c = signature.charAt(index);
            if (c == ')') {
                index++;
                break; // End of parameters, start of return type
            } else if (c == 'L') {
                int endIndex = signature.indexOf(';', index);
                if (endIndex == -1) {
                    throw new IllegalArgumentException("Invalid method signature: " + signature);
                }
                String className = signature.substring(index + 1, endIndex).replace('/', '.');
                params.add(className);
                index = endIndex + 1;
            } else if (c == '[') {
                // Skip over array dimensions
                int arrayDimension = 0;
                while (index < signature.length() && signature.charAt(index) == '[') {
                    index++;
                    arrayDimension++;
                }
                if (index == signature.length()) {
                    throw new IllegalArgumentException("Invalid method signature: " + signature);
                }
                StringBuilder builder = new StringBuilder();
                index = setNameToClassParamDesc(signature, index, builder);
                params.add(builder.append("[]".repeat(arrayDimension)).toString());
            } else if (TYPE_MAPPINGS.containsKey(c)) {
                params.add(TYPE_MAPPINGS.get(c));
                index++;
            } else {
                throw new IllegalArgumentException("Invalid type character in signature: " + c);
            }
        }
        return index;
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
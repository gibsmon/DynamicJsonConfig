package com.poliymorf.dynamicjsonconfig.mapper;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class JsonToConfig {


    public static String parseJson(JsonNode node, boolean setter) {
        StringBuilder sb = new StringBuilder();
        Set<String> printedPaths = new HashSet<>();
        parse(node, "", "", sb, printedPaths, setter);
        return sb.toString();
    }

    private static void parse(JsonNode node, String path, String prefix, StringBuilder sb, Set<String> printedPaths, boolean setter) {
        String prx = setter ? "=\n" : "\n";
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String currentPath = path.isEmpty() ? entry.getKey() : path + "{}, " + entry.getKey();

                if (entry.getValue().isValueNode()) {
                    if (printedPaths.add(currentPath)) {
                        sb.append("& ")
                                .append(currentPath)
                                .append(prx);
                    }
                } else {
                    parse(entry.getValue(), currentPath, "&", sb, printedPaths, setter);
                }
            }
        } else if (node.isArray()) {
            String currentPath = path + "[]";

            if (node.size() > 0) {
                JsonNode firstItem = node.get(0);

                if (firstItem.isValueNode()) {
                    if (printedPaths.add(currentPath)) {
                        sb.append("& ")
                                .append(currentPath)
                                .append(prx);
                    }
                } else {
                    parse(firstItem, currentPath, "&", sb, printedPaths, setter);
                }
            } else {
                if (printedPaths.add(currentPath)) {
                    sb.append("& ")
                            .append(currentPath)
                            .append(prx);
                }
            }
        } else if (node.isValueNode()) {
            if (printedPaths.add(path)) {
                sb.append("& ")
                        .append(path)
                        .append(prx);
            }
        }
    }

}

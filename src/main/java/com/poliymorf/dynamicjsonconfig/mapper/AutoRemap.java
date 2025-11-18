package com.poliymorf.dynamicjsonconfig.mapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

public class AutoRemap {

    private static final ObjectMapper mapper = new ObjectMapper();



    // Get values from path
    public static List<JsonNode> getValuesByPath(JsonNode node, String path) {
        String[] tokens = path.replace("&", "").split(",\\s*");
        return traversePath(node, tokens, 0);
    }

    private static List<JsonNode> traversePath(JsonNode current, String[] tokens, int index) {
        if (index >= tokens.length) {
            return Collections.singletonList(current);
        }

        String token = tokens[index];
        String key = extractKey(token);

        if (token.endsWith("{}[]") || token.endsWith("{[]}")) {
            JsonNode objNode = current.path(key);
            if (!objNode.isArray()) return Collections.emptyList();

            List<JsonNode> result = new ArrayList<>();
            for (JsonNode item : objNode) {
                result.addAll(traversePath(item, tokens, index + 1));
            }
            return result;

        } else if (token.endsWith("{}")) {
            JsonNode next = current.path(key);
            if (next.isMissingNode()) return Collections.emptyList();
            return traversePath(next, tokens, index + 1);

        } else if (token.endsWith("[]")) {
            JsonNode arrayNode = current.path(key);
            if (!arrayNode.isArray()) return Collections.emptyList();

            List<JsonNode> result = new ArrayList<>();
            for (JsonNode item : arrayNode) {
                result.addAll(traversePath(item, tokens, index + 1));
            }
            return result;

        } else {
            JsonNode next = current.path(key);
            if (next.isMissingNode()) return Collections.emptyList();
            return traversePath(next, tokens, index + 1);
        }
    }

    // Set values into JSON
    public static void setValuesByPath(ObjectNode node, String path, List<JsonNode> values) {
        String[] tokens = path.replace("&", "").split(",\\s*");
        traverseAndSet(node, tokens, 0, values);
    }

    private static void traverseAndSet(JsonNode current, String[] tokens, int index, List<JsonNode> values) {
        if (index >= tokens.length) return;

        String token = tokens[index];
        String key = extractKey(token);
        boolean isLast = (index == tokens.length - 1);

        if (token.endsWith("{}[]") || token.endsWith("{[]}")) {
            if (!current.has(key) || current.get(key).isNull()) {
                ((ObjectNode) current).set(key, mapper.createArrayNode());
            }

            ArrayNode arrayNode = (ArrayNode) current.get(key);

            // Expand array size if needed
            while (arrayNode.size() < values.size()) {
                arrayNode.add(mapper.createObjectNode());
            }

            for (int i = 0; i < values.size(); i++) {
                JsonNode itemNode = arrayNode.get(i);
                if (!(itemNode instanceof ObjectNode)) {
                    itemNode = mapper.createObjectNode();
                    arrayNode.set(i, itemNode);
                }

                if (isLast) {
                    String field = extractLastField(tokens);
                    ((ObjectNode) itemNode).set(field, values.get(i));
                } else {
                    traverseAndSet(itemNode, tokens, index + 1, Collections.singletonList(values.get(i)));
                }
            }

        } else if (token.endsWith("{}")) {
            if (!current.has(key) || current.get(key).isNull()) {
                ((ObjectNode) current).set(key, mapper.createObjectNode());
            }
            traverseAndSet(current.get(key), tokens, index + 1, values);

        } else if (token.endsWith("[]")) {
            if (!current.has(key) || current.get(key).isNull()) {
                ((ObjectNode) current).set(key, mapper.createArrayNode());
            }

            ArrayNode arrayNode = (ArrayNode) current.get(key);

            while (arrayNode.size() < values.size()) {
                arrayNode.add(mapper.createObjectNode());
            }

            for (int i = 0; i < values.size(); i++) {
                JsonNode itemNode = arrayNode.get(i);
                if (!(itemNode instanceof ObjectNode)) {
                    itemNode = mapper.createObjectNode();
                    arrayNode.set(i, itemNode);
                }

                if (isLast) {
                    String field = extractLastField(tokens);
                    ((ObjectNode) itemNode).set(field, values.get(i));
                } else {
                    traverseAndSet(itemNode, tokens, index + 1, Collections.singletonList(values.get(i)));
                }
            }

        } else {
            if (isLast) {
                ((ObjectNode) current).set(key, values.get(0));
            } else {
                if (!current.has(key) || current.get(key).isNull()) {
                    ((ObjectNode) current).set(key, mapper.createObjectNode());
                }
                traverseAndSet(current.get(key), tokens, index + 1, values);
            }
        }
    }

    //  Extract field name
    private static String extractKey(String token) {
        return token.replace("{}", "")
                .replace("[]", "")
                .replace("{[]}", "")
                .replace("{}[]", "")
                .trim();
    }

    // Extract last field (for final assignment)
    private static String extractLastField(String[] tokens) {
        String last = tokens[tokens.length - 1];
        return last.replace("{}", "")
                .replace("[]", "")
                .replace("{[]}", "")
                .replace("{}[]", "")
                .trim();
    }
}


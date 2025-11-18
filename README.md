# DynamicJsonConfig

A Java library for dynamic JSON configuration parsing and mapping. This library provides utilities to generate configuration paths from JSON structures and map values between different JSON objects.

## Features

- **JSON Path Generation**: Generate configuration paths from JSON structures
- **Dynamic JSON Mapping**: Map values between different JSON objects using path expressions
- **Array Support**: Handle arrays and nested objects seamlessly
- **Flexible Configuration**: Support for both setter-style and simple path configurations

## Dependencies

- Jackson Databind (included with Spring Boot)
- Java 21+

## Usage

### 1. Generating Configuration Paths

Use `JsonToConfig.parseJson()` to generate configuration paths from JSON:

```java
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static com.poliymorf.dynamicjsonconfig.mapper.JsonToConfig.*;

public class Example {
    public static void main(String[] args) throws IOException {
        String json = """
            {
              "menu": {
                "id": "file",
                "value": "File",
                "popup": {
                  "menuitem": [
                    {"value": "New", "onclick": "CreateNewDoc()"},
                    {"value": "Open", "onclick": "OpenDoc()"}
                  ]
                }
              }
            }
            """;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        // Generate setter-style paths
        String setterPaths = parseJson(root, true);
        System.out.println(setterPaths);
        
        // Generate simple paths
        String simplePaths = parseJson(root, false);
        System.out.println(simplePaths);
    }
}
```

**Output (setter-style):**
```
& menu{}, id=
& menu{}, value=
& menu{}, popup{}, menuitem{}[], value=
& menu{}, popup{}, menuitem{}[], onclick=
```

**Output (simple paths):**
```
& menu{}, id
& menu{}, value
& menu{}, popup{}, menuitem{}[], value
& menu{}, popup{}, menuitem{}[], onclick
```

### 2. Dynamic JSON Mapping

Use `AutoRemap` to map values between different JSON structures:

```java
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import static com.poliymorf.dynamicjsonconfig.mapper.AutoRemap.*;

public class MappingExample {
    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        
        // Source JSON
        String sourceJson = """
            {
              "id": "0001",
              "type": "donut",
              "name": "Cake",
              "batters": {
                "batter": [
                  {"id": "1001", "type": "Regular"},
                  {"id": "1002", "type": "Chocolate"}
                ]
              }
            }
            """;

        // Target JSON template
        String targetJson = """
            {
              "menu": {
                "id": "file",
                "value": "File",
                "popup": {
                  "menuitem": [
                    {"value": "New", "onclick": "CreateNewDoc()"}
                  ]
                }
              }
            }
            """;

        JsonNode sourceNode = mapper.readTree(sourceJson);
        ObjectNode targetNode = (ObjectNode) mapper.readTree(targetJson);

        // Define mappings: target_path = source_path
        String[] mappings = {
            "& menu{}, id = & id",
            "& menu{}, value = & type",
            "& menu{}, popup{}, menuitem{}[], value = & batters{}, batter{}[], id",
            "& menu{}, popup{}, menuitem{}[], onclick = & batters{}, batter{}[], type"
        };

        // Apply mappings
        for (String mapping : mappings) {
            String[] parts = mapping.split("=");
            String targetPath = parts[0].trim();
            String sourcePath = parts[1].trim();

            List<JsonNode> values = getValuesByPath(sourceNode, sourcePath);
            if (!values.isEmpty()) {
                setValuesByPath(targetNode, targetPath, values);
            }
        }

        // Print result
        System.out.println(mapper.writerWithDefaultPrettyPrinter()
                                .writeValueAsString(targetNode));
    }
}
```

**Output:**
```json
{
  "menu": {
    "id": "0001",
    "value": "donut",
    "popup": {
      "menuitem": [
        {
          "value": "1001",
          "onclick": "Regular"
        },
        {
          "value": "1002",
          "onclick": "Chocolate"
        }
      ]
    }
  }
}
```

## Path Syntax

The library uses a specific path syntax to navigate JSON structures:

- `{}` - Navigate into an object
- `[]` - Navigate into an array
- `{}[]` - Navigate into an array within an object
- `,` - Separate path segments

### Examples:

- `& menu{}, id` - Access `id` field within `menu` object
- `& items[]` - Access array `items`
- `& menu{}, items{}[], name` - Access `name` field in array items within menu object

## API Reference

### JsonToConfig

- `parseJson(JsonNode node, boolean setter)` - Generate configuration paths from JSON
  - `node`: The JSON node to parse
  - `setter`: If true, generates setter-style paths with `=`; if false, generates simple paths

### AutoRemap

- `getValuesByPath(JsonNode node, String path)` - Extract values from JSON using path
- `setValuesByPath(ObjectNode node, String path, List<JsonNode> values)` - Set values in JSON using path

## Building

```bash
mvn clean compile
```

## Running Examples

```bash
mvn exec:java -Dexec.mainClass="com.poliymorf.dynamicjsonconfig.example.ExampleCreateConfig"
mvn exec:java -Dexec.mainClass="com.poliymorf.dynamicjsonconfig.example.ExampleMapping"
```
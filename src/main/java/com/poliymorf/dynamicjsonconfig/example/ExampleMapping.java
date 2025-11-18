package com.poliymorf.dynamicjsonconfig.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.List;

import static com.poliymorf.dynamicjsonconfig.mapper.AutoRemap.*;


public class ExampleMapping {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        String json1 = "{\n" +
                "  \"menu\": {\n" +
                "    \"id\": \"file\",\n" +
                "    \"value\": \"File\",\n" +
                "    \"popup\": {\n" +
                "      \"menuitem\": [\n" +
                "        {\"value\": \"New\", \"onclick\": \"CreateNewDoc()\"}\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";

        String json2 = "{\n" +
                "  \"id\": \"0001\",\n" +
                "  \"type\": \"donut\",\n" +
                "  \"name\": \"Cake\",\n" +
                "  \"ppu\": 0.55,\n" +
                "  \"batters\": {\n" +
                "    \"batter\": [\n" +
                "      { \"id\": \"1001\", \"type\": \"Regular\" },\n" +
                "      { \"id\": \"1002\", \"type\": \"Chocolate\" },\n" +
                "      { \"id\": \"1003\", \"type\": \"Blueberry\" },\n" +
                "      { \"id\": \"1004\", \"type\": \"Devil's Food\" }\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        ObjectNode json1Node = (ObjectNode) mapper.readTree(json1);
        JsonNode json2Node = mapper.readTree(json2);

        String[] mappings = {
                "& menu{}, id= & id",
                "& menu{}, value= & type",
                "& menu{}, popup{}, menuitem{}[], value= & batters{}, batter{}[], id",
                "& menu{}, popup{}, menuitem{}[], onclick= & batters{}, batter{}[], type"
        };

        for (String map : mappings) {
            String[] parts = map.split("=");
            String targetPath = parts[0].trim();
            String sourcePath = parts[1].trim();

            List<JsonNode> values = getValuesByPath(json2Node, sourcePath);
            if (values.isEmpty()) {
                System.out.println("No value found for source path: " + sourcePath);
                continue;
            }
            setValuesByPath(json1Node, targetPath, values);
        }

        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json1Node));
    }

}

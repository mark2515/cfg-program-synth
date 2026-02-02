package synth.util;

import synth.core.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {

    /**
     * Parse one example from a string.
     * @param text example text of the form x=a, y=b, z=c -> d, where a, b, c, d are integers.
     *             Note that the equal sign "=", comma ",", and right arrow "->" are hard coded.
     *             Also note that the variable names are case-sensitive, and we use lower case x, y, z.
     * @return the example
     */
    public static Example parseAnExample(String text) {
        String[] tokens = text.split("->");
        assert tokens.length == 2 : "Parsing error in line " + text;
        Map<String, Integer> input = new HashMap<>();
        String[] pairs = tokens[0].trim().split(",");
        for (String pair : pairs) {
            Map<String, Integer> map = parseVarValuePair(pair);
            for (String varName : map.keySet()) {
                input.put(varName, map.get(varName));
            }
        }
        int output = Integer.parseInt(tokens[1].trim());
        return new Example(input, output);
    }

    /**
     * Parse a list of examples from a list of strings, ignoring empty lines.
     * @param lines a list of example strings
     * @return a list of examples
     */
    public static List<Example> parseAllExamples(List<String> lines) {
        List<Example> examples = new ArrayList<>();
        for (String line : lines) {
            if (!line.isEmpty()) {
                examples.add(parseAnExample(line));
            }
        }
        return examples;
    }

    /**
     * Parse a pair of variable name and value.
     * @param text pair of the form x=a
     * @return a singleton map from the variable name to the value
     */
    private static Map<String, Integer> parseVarValuePair(String text) {
        String[] tokens = text.split("=");
        assert tokens.length == 2: "Parsing error in pair " + text;
        Map<String, Integer> map = new HashMap<>();
        String varName = tokens[0].trim();
        String valueText = tokens[1].trim();
        map.put(varName, Integer.parseInt(valueText));
        return map;
    }
}

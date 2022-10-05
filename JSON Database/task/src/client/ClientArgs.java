package client;

import com.beust.jcommander.Parameter;

public class ClientArgs {

    @Parameter(names = {"--commandType", "-t"}, description = "get, set or delete", arity = 1)
    private String type;
    @Parameter(names = {"--key", "-k"}, description = "key", arity = 1)
    private String key;

    @Parameter(names = {"--value", "-v"}, description = "value", arity = 1, required = false)
    private String value;

    @Parameter(names = {"--input", "-in"}, description = "input", arity = 1)
    private String input;

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getInput() {
        return input;
    }
}

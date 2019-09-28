package provider.response;

import lombok.Data;

import java.util.Set;

@Data
public class ParseResult {
    public Set<ContextVariable> contextVariables;
    public Set<String> imports;

    public ParseResult() {
    }

    public ParseResult(Set<ContextVariable> contextVariables, Set<String> imports) {
        this.contextVariables = contextVariables;
        this.imports = imports;
    }

}

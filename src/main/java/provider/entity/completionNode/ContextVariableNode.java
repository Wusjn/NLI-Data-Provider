package provider.entity.completionNode;

import lombok.Data;
import provider.entity.UsageCounter;
import provider.response.ContextVariable;

import java.util.List;


@Data
public class ContextVariableNode implements CompletionNode{
    private ContextVariable contextVariable;

    public String toCode(){
        return contextVariable.getName();
    }

    public ContextVariableNode(ContextVariable contextVariable){
        this.contextVariable = contextVariable;
    }

    @Override
    public List<String> getTypes() {
        return null;
    }

    @Override
    public double getSocre(UsageCounter usageCounter, String targetType) {
        return 1;
    }
}

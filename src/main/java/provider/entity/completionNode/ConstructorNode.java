package provider.entity.completionNode;

import lombok.Data;
import provider.entity.Constructor;
import provider.entity.UsageCounter;

import java.util.ArrayList;
import java.util.List;

@Data
public class ConstructorNode implements CompletionNode{
    private Constructor constructor;
    private List<CompletionNode> args = new ArrayList<>();
    private double score;

    public ConstructorNode(Constructor constructor, List<CompletionNode> args){
        this.constructor = constructor;
        this.args = args;
    }

    public String toCode(){
        StringBuilder sb = new StringBuilder();
        sb.append("new " + constructor.getClassName() + "(");
        for (CompletionNode cn : args){
            sb.append(cn.toCode() + ",");
        }
        if (args.size() > 0){
            sb.deleteCharAt(sb.lastIndexOf(","));
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public List<String> getTypes() {
        List<String> types = new ArrayList<>();
        for (CompletionNode arg : args){
            List<String> subTypes = arg.getTypes();
            if (subTypes != null){
                types.addAll(subTypes);
            }
        }
        types.add(constructor.getPackageName() + "." + constructor.getClassName());

        return types;
    }

    @Override
    public double getSocre(UsageCounter usageCounter, String targetType) {
        int total = usageCounter.getCounter().getOrDefault(targetType,-1);
        assert total != -1;
        double score = constructor.getUsageCount()/(double)total;
        for (int i=0; i<args.size();i++){
            score *= args.get(i).getSocre(usageCounter,constructor.getArgsType().get(i));
        }
        return score;
    }
}

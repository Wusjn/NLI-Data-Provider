package provider.entity.completionNode;

import lombok.Data;
import provider.entity.Method;
import provider.entity.UsageCounter;

import java.util.ArrayList;
import java.util.List;

@Data
public class MethodNode implements CompletionNode{
    private Method method;
    private CompletionNode receiver;
    private List<CompletionNode> args = new ArrayList<>();

    public MethodNode(Method method, CompletionNode receiver, List<CompletionNode> args){
        this.method = method;
        this.receiver = receiver;
        this.args = args;
    }
    public MethodNode(Method method, List<CompletionNode> args){
        this.method = method;
        this.receiver = null;
        this.args = args;
    }


    public String toCode(){
        StringBuilder sb = new StringBuilder();
        if (method.isStatic()){
            sb.append(method.getClassName() + ".");
        }else {
            sb.append(receiver.toCode() + ".");
        }
        sb.append(method.getShortName() + "(");
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

        if (method.isStatic()){
            types.add(method.getPackageName() + "." + method.getClassName());
        }else {
            List<String> subTypes = receiver.getTypes();
            if (subTypes != null){
                types.addAll(subTypes);
            }
        }

        return types;
    }

    @Override
    public double getSocre(UsageCounter usageCounter, String targetType) {
        int total = usageCounter.getCounter().getOrDefault(targetType,-1);
        assert total != -1;
        double score = method.getUsageCount()/(double)total;
        for (int i=0; i<args.size();i++){
            score *= args.get(i).getSocre(usageCounter,method.getArgsType().get(i));
        }
        if (!method.isStatic()){
            score *= receiver.getSocre(usageCounter,method.getReturnType());
        }
        return score;
    }
}

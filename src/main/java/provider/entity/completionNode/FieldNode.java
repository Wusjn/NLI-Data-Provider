package provider.entity.completionNode;

import lombok.Data;
import provider.entity.Field;
import provider.entity.UsageCounter;

import java.util.ArrayList;
import java.util.List;


@Data
public class FieldNode implements CompletionNode {
    private Field field;
    private CompletionNode receiver;

    public FieldNode(Field field){
        this.field = field;
        receiver = null;
    }

    public FieldNode(Field field, CompletionNode receiver){
        this.field = field;
        this.receiver = receiver;
    }

    public String toCode(){
        if (field.isStatic()){
            return field.getClassName() + "." + field.getFieldName();
        }else{
            return receiver.toCode() + "." + field.getFieldName();
        }
    }

    @Override
    public List<String> getTypes() {
        if (field.isStatic()){
            List<String> types = new ArrayList<>();
            types.add(field.getPackageName() + "." + field.getClassName());
            return types;
        }else{
            return receiver.getTypes();
        }
    }

    @Override
    public double getSocre(UsageCounter usageCounter, String targetType) {
        int total = usageCounter.getCounter().getOrDefault(targetType,-1);
        assert total != -1;
        double score = field.getUsageCount()/(double)total;
        if (!field.isStatic()){
            return receiver.getSocre(usageCounter,field.getPackageName() + "." + field.getClassName()) * score;
        }
        return score;
    }
}

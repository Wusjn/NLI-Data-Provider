package provider.entity.completionNode;

import lombok.Data;
import provider.entity.UsageCounter;

import java.util.ArrayList;
import java.util.List;

@Data
public class HoleNode implements CompletionNode {
    public String qualifiedTypeName;
    public boolean isPrimitiveType;
    public String shortName;

    public String toCode() {
        return "<" + shortName + ">";
    }

    public HoleNode(String qualifiedTypeName) {
        this.qualifiedTypeName = qualifiedTypeName;

        int lastDot = qualifiedTypeName.lastIndexOf(".");
        if (lastDot == -1) {
            this.isPrimitiveType = true;
            this.shortName = qualifiedTypeName;
        } else {
            this.isPrimitiveType = false;
            this.shortName = qualifiedTypeName.substring(lastDot + 1);
        }
    }
    //public HoleNode(){}

    @Override
    public List<String> getTypes() {
        List<String> types = new ArrayList<>();
        if (!isPrimitiveType && !qualifiedTypeName.startsWith("java.lang")) {
            types.add(qualifiedTypeName);
        }

        return types;
    }

    @Override
    public double getSocre(UsageCounter usageCounter, String targetType) {
        return 0;
    }
}

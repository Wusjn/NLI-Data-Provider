package provider.entity.completionNode;

import provider.entity.UsageCounter;

import java.util.List;

public interface CompletionNode {
    String toCode();
    List<String> getTypes();
    double getSocre(UsageCounter counter, String targetType);
}

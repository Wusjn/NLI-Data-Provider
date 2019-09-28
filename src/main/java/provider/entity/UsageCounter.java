package provider.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class UsageCounter {
    private Map<String, Integer> exactCounter = new HashMap<>();
    private Map<String, Integer> counter = new HashMap<>();

    //TODO not a type, it should be a method/field/constructor
    public void incrementByOne(String type) {
        Integer counts = exactCounter.get(type);
        if (counts == null) {
            exactCounter.put(type, 1);
        } else {
            exactCounter.put(type, counts + 1);
        }
    }

    public void recomputeCounter(Map<String, List<String>> extendedInheritMap) {
        for (String type : exactCounter.keySet()) {
            int counts = 0;

            List<String> extendTypes = extendedInheritMap.get(type);
            if (extendTypes == null) {
                counter.put(type, exactCounter.get(type));
                continue;
            }

            for (String extendType : extendTypes) {
                counts += exactCounter.getOrDefault(extendType, 0);
            }
            counter.put(type, counts);
        }
        return;
    }

    public UsageCounter() {
    }
}

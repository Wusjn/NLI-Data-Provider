package provider.response;

import java.util.ArrayList;
import java.util.List;

public class Recommendation {
    private final List<String> recommendations = new ArrayList<>();

    public Recommendation(){}

    public void addItem(String recommendation){
        recommendations.add(recommendation);
    }

    public List<String> getRecommendations() {
        return recommendations;
    }
}

package provider.response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Recommendation {
    private final List<String> recommendations = new ArrayList<>();
    private final List<List<String>> typesList = new ArrayList<>();


    public Recommendation(){}

    public void addItem(String recommendation){
        recommendations.add(recommendation);
    }
    public void addTypes(List<String> types){
        typesList.add(types);
    }


    public List<String> getRecommendations() {
        return recommendations;
    }

    public List<List<String>> getTypesList() {
        return typesList;
    }
}

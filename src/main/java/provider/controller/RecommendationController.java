package provider.controller;

import provider.repository.entity.MethodEntity;
import provider.repository.entity.TypeEntity;
import provider.repository.Repository;
import provider.response.Recommendation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class RecommendationController {
    private Repository repository;

    public RecommendationController(){
        repository = new Repository();
    }


    @RequestMapping("/recommendation")
    public Recommendation getRecommendation(@RequestParam(name = "type", defaultValue = "null") String type){
        if (type.equals("null")){
            return null;
        }
        TypeEntity typeEntity = repository.getTyoe(type);
        if (typeEntity == null){
            return null;
        }

        Recommendation recommendation = new Recommendation();
        Set<MethodEntity> producers = typeEntity.getProducers();
        for (MethodEntity methodEntity : producers){
            recommendation.addItem(methodEntity.getSignature());
        }
        return recommendation;
    }
}

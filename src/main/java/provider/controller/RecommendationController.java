package provider.controller;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.RequestBody;
import provider.entity.Constructor;
import provider.entity.Field;
import provider.entity.Method;
import provider.entity.Type;
import provider.entity.completionNode.*;
import provider.repository.Repository;
import provider.response.ContextVariable;
import provider.response.Recommendation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;


@RestController
public class RecommendationController {

    private Repository repository;
    private List<ContextVariable> variableContext = new ArrayList<>();

    public RecommendationController(){
        repository = new Repository();
    }


    @RequestMapping("/recommendation")
    public Recommendation getRecommendation(@RequestParam(name = "type", defaultValue = "null") String type, @RequestBody String context){
        if (type.equals("null")){
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            variableContext = (List<ContextVariable>)objectMapper.readValue(
                    context,
                    objectMapper.getTypeFactory().constructParametricType(List.class,ContextVariable.class)
            );
        } catch (IOException e) {
            variableContext = new ArrayList<>();
            e.printStackTrace();
        }

        Recommendation recommendation = new Recommendation();

        Type typeEntity = repository.typeMap.get(type);
        if (typeEntity == null){
            for (ContextVariable cv : variableContext){
                if (cv.getQualifiedType().equals(type)){
                    recommendation.addItem(new ContextVariableNode(cv).toCode());
                    recommendation.addTypes(null);
                    Collections.sort(recommendation.getRecommendations(),Comparator.comparingInt(String::length));
                }
            }
        }else {
            recommendation = getRecommendation(typeEntity);
        }

        /*Collections.sort(recommendationList, Comparator.comparingInt(String::length));
        for (String recommendationItem : recommendationList){
            recommendation.addItem(recommendationItem);
        }*/

        return recommendation;
    }

    private Recommendation getRecommendation(Type typeEntity){
        Recommendation recommendation = new Recommendation();
        List<CompletionNode> matchedExpression = getMatchedExpression(typeEntity,2);
        Collections.sort(matchedExpression, Comparator.comparingDouble(a -> a.getSocre(repository.usageCounter,typeEntity.getFullQualifiedName())));
        for (CompletionNode cn : matchedExpression){
            recommendation.addItem(cn.toCode());
            recommendation.addTypes(cn.getTypes());
        }
        return recommendation;
    }

    //at least contains a 'HoleNode'
    private List<CompletionNode> getMatchedExpression(Type typeEntity, int maxDeepth) {
        List<CompletionNode> matchedExpression = new ArrayList<>();

        Set<Type> ancestors = new HashSet<>();
        Queue<Type> queue = new LinkedList<>();
        queue.add(typeEntity);
        while (!queue.isEmpty()){
            Type type = queue.remove();
            if (ancestors.contains(type)){
                continue;
            }
            ancestors.add(type);
            for (String subClass : type.getSubClasses()){
                Type subType = repository.typeMap.get(subClass);
                if (subType != null){
                    queue.add(subType);
                }
            }
        }

        for (ContextVariable cv : variableContext){
            Type cvType = repository.typeMap.get(cv.getQualifiedType());
            if (cvType != null && ancestors.contains(cvType)){
                matchedExpression.add(new ContextVariableNode(cv));
            }
        }

        if (maxDeepth <= 0){
            if (matchedExpression.size() == 0){
                matchedExpression.add(new HoleNode(typeEntity.getFullQualifiedName()));
            }
            return matchedExpression;
        }

        for (Type ancestor : ancestors){
            matchedExpression.addAll(getExactMatchedExpression(ancestor,maxDeepth));
        }

        if (matchedExpression.size() == 0){
            matchedExpression.add(new HoleNode(typeEntity.getFullQualifiedName()));
        }
        return matchedExpression;
    }

    private List<CompletionNode> getExactMatchedExpression(Type typeEntity, int maxDeepth){
        List<CompletionNode> matchedExpression = new ArrayList<>();
        if (maxDeepth <= 0){
            return matchedExpression;
        }

        for (Field field : typeEntity.getFieldProducers()){
            if (field.isStatic()){
                matchedExpression.add(new FieldNode(field));
            }else{
                Type receiverType = repository.typeMap.get(field.getPackageName() + "." + field.getClassName());
                List<CompletionNode> matchedReceiver = getMatchedExpression(receiverType,maxDeepth-1);
                for (CompletionNode receiver : matchedReceiver){
                    matchedExpression.add(new FieldNode(field,receiver));
                }
            }
        }

        for (Method method : typeEntity.getMethodProducers()){
            List<List<CompletionNode>> argsCombinations = getArgsCombinations(maxDeepth-1, method.getArgsType());

            if (method.isStatic()){
                for (List<CompletionNode> argCombination : argsCombinations){
                    matchedExpression.add(new MethodNode(method,argCombination));
                }
            }else{
                Type receiverType = repository.typeMap.get(method.getPackageName() + "." + method.getClassName());
                List<CompletionNode> receivers = getMatchedExpression(receiverType,maxDeepth-1);
                for (CompletionNode receiver : receivers){
                    for (List<CompletionNode> argCombination : argsCombinations){
                        matchedExpression.add(new MethodNode(method,receiver,argCombination));
                    }
                }
            }
        }

        for (Constructor constructor : typeEntity.getConstructorProducers()){
            List<List<CompletionNode>> argsCombinations = getArgsCombinations(maxDeepth-1, constructor.getArgsType());

            for (List<CompletionNode> argCombination : argsCombinations){
                matchedExpression.add(new ConstructorNode(constructor,argCombination));
            }
        }

        return matchedExpression;
    }

    private List<List<CompletionNode>> getArgsCombinations(int maxDeepth, List<String> argsType) {
        List<List<CompletionNode>> args = new ArrayList<>();
        for (String argTypeString : argsType){
            List<CompletionNode> arg = new ArrayList<>();
            Type argType = repository.typeMap.get(argTypeString);
            if (argType == null){
                for (ContextVariable cv : variableContext){
                    if (cv.getQualifiedType().equals(argTypeString)){
                        arg.add(new ContextVariableNode(cv));
                    }
                    //Collections.sort(arg,(a,b)->{});
                }
                if (arg.size() == 0){
                    arg.add(new HoleNode(argTypeString));
                }
            }else {
                arg = getMatchedExpression(argType,maxDeepth);
            }
            args.add(arg);
        }
        List<List<CompletionNode>> argsCombinations = getCartesianProduct(args);
        return argsCombinations;
    }

    private List<List<CompletionNode>> getCartesianProduct(List<List<CompletionNode>> src){
        List<List<CompletionNode>> result = new ArrayList<>();
        //System.out.println(src.size());
        if (src.size() == 0){
            result.add(new ArrayList<>());
            return result;
        }

        List<List<CompletionNode>> subSrc = new ArrayList<>();
        for (int i=0;i<src.size()-1;i++){
            subSrc.add(src.get(i));
        }
        List<List<CompletionNode>> subResult = getCartesianProduct(subSrc);

        List<CompletionNode> lastNodes = src.get(src.size()-1);
        for (CompletionNode lastNode : lastNodes){
            List<List<CompletionNode>> extendedSubResult = copy(subResult);
            for (List<CompletionNode> extendedSubResultRecord : extendedSubResult){
                extendedSubResultRecord.add(lastNode);
            }
            result.addAll(extendedSubResult);
        }

        return result;
    }

    public List<List<CompletionNode>> copy(List<List<CompletionNode>> src){
        List<List<CompletionNode>> result = new ArrayList<>();
        for (List<CompletionNode> subsrc : src){
            List<CompletionNode> subResult = new ArrayList<>();
            for (CompletionNode node : subsrc){
                subResult.add(node);
            }
            result.add(subResult);
        }
        return result;
    }


}

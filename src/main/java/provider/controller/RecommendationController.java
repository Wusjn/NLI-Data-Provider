package provider.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.nlp.util.EditDistance;
import org.springframework.web.bind.annotation.*;
import provider.entity.Constructor;
import provider.entity.Field;
import provider.entity.Method;
import provider.entity.Type;
import provider.entity.completionNode.*;
import provider.repository.Repository;
import provider.response.ContextVariable;
import provider.response.Entry;
import provider.response.Recommendation;

import java.io.IOException;
import java.util.*;


@RestController
public class RecommendationController {

    private Repository repository;
    private Set<ContextVariable> variableContext = new HashSet<>();

    public RecommendationController() {
        repository = new Repository();
    }


    @RequestMapping("/recommendation")
    @CrossOrigin
    public Recommendation getRecommendation(
            @RequestParam(name = "type", defaultValue = "null") String type,
            @RequestParam(name = "info", defaultValue = "null") String info,
            @RequestBody String context) {
        if (type.equals("null")) {
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            variableContext = (Set<ContextVariable>) objectMapper.readValue(
                    context,
                    objectMapper.getTypeFactory().constructParametricType(Set.class, ContextVariable.class)
            );
            /*for(ContextVariable cv : variableContext){
                System.out.println(cv.getQualifiedType() + " : " + cv.getName());
            }*/
        } catch (IOException e) {
            variableContext = new HashSet<>();
            e.printStackTrace();
        }

        Recommendation recommendation = new Recommendation();

        Type typeEntity = repository.typeMap.get(type);
        if (typeEntity == null) {
            EditDistance editDistance = new EditDistance();
            for (ContextVariable cv : variableContext) {
                if (cv.getQualifiedType().equals(type)) {
                    recommendation.addEntry(
                            new Entry(
                                    new ContextVariableNode(cv).toCode(),
                                    null,
                                    1 / (1 + editDistance.score(info, cv.getName()))
                            )
                    );
                }
            }
            if (type.equals("boolean")) {
                recommendation.addEntry(
                        new Entry(
                                "true",
                                null,
                                1
                        )
                );
                recommendation.addEntry(
                        new Entry(
                                "false",
                                null,
                                1
                        )
                );
            }
            for (String basicTypeName : repository.basicTypeToFieldMap.keySet()) {
                if (basicTypeName.equals(type)) {
                    for (String field : repository.basicTypeToFieldMap.get(basicTypeName)) {
                        /*recommendation.addEntry(
                                new Entry(
                                        field,
                                        null,
                                        1 / (1 + editDistance.score(info, field))
                                )
                        );*/
                    }
                }
            }
            //Collections.sort(recommendation.getRecommendations(),Comparator.comparingInt());
        } else {
            recommendation = getRecommendation(typeEntity);
            recommendation.trim(100);
        }

        /*Collections.sort(recommendationList, Comparator.comparingInt(String::length));
        for (String recommendationItem : recommendationList){
            recommendation.addItem(recommendationItem);
        }*/
        if (recommendation.getEntries().size() == 0) {
            if (type.equals("name_property")) {
                recommendation.addEntry(new Entry(info.replace(" ", ""), null, 1));
            } else if (type.equals("java.lang.String")) {
                recommendation.addEntry(new Entry("\"" + info + "\"", null, 1));
            } else if (type.equals("int") || type.equals("double")) {
                recommendation.addEntry(new Entry("0", null, 1));
                recommendation.addEntry(new Entry("1", null, 1));
            } else if (type.equals("short")) {
                recommendation.addEntry(new Entry("(short)0", null, 1));
                recommendation.addEntry(new Entry("(short)1", null, 1));
            }
        }

        return recommendation;
    }

    private Recommendation getRecommendation(Type typeEntity) {
        Recommendation recommendation = new Recommendation();
        List<CompletionNode> matchedExpression = getMatchedExpression(typeEntity, 2);
        Collections.sort(matchedExpression, Comparator.comparingDouble(a -> -a.getSocre(repository.usageCounter, typeEntity.getFullQualifiedName())));
        for (CompletionNode cn : matchedExpression) {
            recommendation.addEntry(new Entry(
                    cn.toCode(),
                    cn.getTypes(),
                    cn.getSocre(repository.usageCounter, typeEntity.getFullQualifiedName())
            ));
        }
        return recommendation;
    }

    //at least contains a 'HoleNode'
    private List<CompletionNode> getMatchedExpression(Type typeEntity, int maxDeepth) {
        List<CompletionNode> matchedExpression = new ArrayList<>();

        Set<Type> ancestors = new HashSet<>();
        Queue<Type> queue = new LinkedList<>();
        queue.add(typeEntity);
        while (!queue.isEmpty()) {
            Type type = queue.remove();
            if (ancestors.contains(type)) {
                continue;
            }
            ancestors.add(type);
            for (String subClass : type.getSubClasses()) {
                Type subType = repository.typeMap.get(subClass);
                if (subType != null) {
                    queue.add(subType);
                }
            }
        }

        for (ContextVariable cv : variableContext) {
            Type cvType = repository.typeMap.get(cv.getQualifiedType());
            if (cvType != null && ancestors.contains(cvType)) {
                matchedExpression.add(new ContextVariableNode(cv));
            }
        }

        if (maxDeepth <= 0) {
            if (matchedExpression.size() == 0) {
                matchedExpression.add(new HoleNode(typeEntity.getFullQualifiedName()));
            }
            return matchedExpression;
        }

        for (Type ancestor : ancestors) {
            matchedExpression.addAll(getExactMatchedExpression(ancestor, maxDeepth));
        }

        if (matchedExpression.size() == 0) {
            matchedExpression.add(new HoleNode(typeEntity.getFullQualifiedName()));
        }
        return matchedExpression;
    }

    private List<CompletionNode> getExactMatchedExpression(Type typeEntity, int maxDeepth) {
        List<CompletionNode> matchedExpression = new ArrayList<>();
        if (maxDeepth <= 0) {
            return matchedExpression;
        }

        for (Field field : typeEntity.getFieldProducers()) {
            if (field.isStatic()) {
                matchedExpression.add(new FieldNode(field));
            } else {
                Type receiverType = repository.typeMap.get(field.getPackageName() + "." + field.getClassName());
                List<CompletionNode> matchedReceiver = getMatchedExpression(receiverType, maxDeepth - 1);
                for (CompletionNode receiver : matchedReceiver) {
                    matchedExpression.add(new FieldNode(field, receiver));
                }
            }
        }

        for (Method method : typeEntity.getMethodProducers()) {
            List<List<CompletionNode>> argsCombinations = getArgsCombinations(maxDeepth - 1, method.getArgsType());

            if (method.isStatic()) {
                for (List<CompletionNode> argCombination : argsCombinations) {
                    matchedExpression.add(new MethodNode(method, argCombination));
                }
            } else {
                Type receiverType = repository.typeMap.get(method.getPackageName() + "." + method.getClassName());
                List<CompletionNode> receivers = getMatchedExpression(receiverType, maxDeepth - 1);
                for (CompletionNode receiver : receivers) {
                    for (List<CompletionNode> argCombination : argsCombinations) {
                        matchedExpression.add(new MethodNode(method, receiver, argCombination));
                    }
                }
            }
        }

        for (Constructor constructor : typeEntity.getConstructorProducers()) {
            List<List<CompletionNode>> argsCombinations = getArgsCombinations(maxDeepth - 1, constructor.getArgsType());

            for (List<CompletionNode> argCombination : argsCombinations) {
                matchedExpression.add(new ConstructorNode(constructor, argCombination));
            }
        }

        return matchedExpression;
    }

    private List<List<CompletionNode>> getArgsCombinations(int maxDeepth, List<String> argsType) {
        List<List<CompletionNode>> args = new ArrayList<>();
        for (String argTypeString : argsType) {
            List<CompletionNode> arg = new ArrayList<>();
            Type argType = repository.typeMap.get(argTypeString);
            if (argType == null) {
                for (ContextVariable cv : variableContext) {
                    if (cv.getQualifiedType().equals(argTypeString)) {
                        arg.add(new ContextVariableNode(cv));
                    }
                    //Collections.sort(arg,(a,b)->{});
                }
                if (arg.size() == 0) {
                    arg.add(new HoleNode(argTypeString));
                }
            } else {
                arg = getMatchedExpression(argType, maxDeepth);
            }
            args.add(arg);
        }
        List<List<CompletionNode>> argsCombinations = getCartesianProduct(args);
        return argsCombinations;
    }

    private List<List<CompletionNode>> getCartesianProduct(List<List<CompletionNode>> src) {
        List<List<CompletionNode>> result = new ArrayList<>();
        //System.out.println(src.size());
        if (src.size() == 0) {
            result.add(new ArrayList<>());
            return result;
        }

        List<List<CompletionNode>> subSrc = new ArrayList<>();
        for (int i = 0; i < src.size() - 1; i++) {
            subSrc.add(src.get(i));
        }
        List<List<CompletionNode>> subResult = getCartesianProduct(subSrc);

        List<CompletionNode> lastNodes = src.get(src.size() - 1);
        for (CompletionNode lastNode : lastNodes) {
            List<List<CompletionNode>> extendedSubResult = copy(subResult);
            for (List<CompletionNode> extendedSubResultRecord : extendedSubResult) {
                extendedSubResultRecord.add(lastNode);
            }
            result.addAll(extendedSubResult);
        }

        return result;
    }

    public List<List<CompletionNode>> copy(List<List<CompletionNode>> src) {
        List<List<CompletionNode>> result = new ArrayList<>();
        for (List<CompletionNode> subsrc : src) {
            List<CompletionNode> subResult = new ArrayList<>();
            for (CompletionNode node : subsrc) {
                subResult.add(node);
            }
            result.add(subResult);
        }
        return result;
    }


}

package provider.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import provider.entity.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Repository {
    public Map<String, Type> typeMap;
    public Map<String, Method> methodMap;
    public Map<String, Constructor> constructorMap;
    public Map<String, Field> fieldMap;
    public Map<String, List<String>> basicTypeToFieldMap;
    public UsageCounter usageCounter;


    public Repository() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            typeMap = (Map<String, Type>) objectMapper.readValue(this.getClass().getResourceAsStream("/data/types"), objectMapper.getTypeFactory().constructParametricType(HashMap.class, String.class, Type.class));
            methodMap = (Map<String, Method>) objectMapper.readValue(this.getClass().getResourceAsStream("/data/methods"), objectMapper.getTypeFactory().constructParametricType(HashMap.class, String.class, Method.class));
            constructorMap = (Map<String, Constructor>) objectMapper.readValue(this.getClass().getResourceAsStream("/data/constructors"), objectMapper.getTypeFactory().constructParametricType(HashMap.class, String.class, Constructor.class));
            fieldMap = (Map<String, Field>) objectMapper.readValue(this.getClass().getResourceAsStream("/data/fields"), objectMapper.getTypeFactory().constructParametricType(HashMap.class, String.class, Field.class));
            usageCounter = (UsageCounter) objectMapper.readValue(this.getClass().getResourceAsStream("/data/counter"), UsageCounter.class);
            basicTypeToFieldMap = (Map<String, List<String>>) objectMapper.readValue(this.getClass().getResourceAsStream("/data/basicTypeToFieldMap"), objectMapper.getTypeFactory().constructParametricType(HashMap.class, String.class, objectMapper.getTypeFactory().constructParametricType(List.class, String.class).getRawClass()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Method method : methodMap.values()) {
            if (typeMap.containsKey(method.getReturnType())) {
                typeMap.get(method.getReturnType()).getMethodProducers().add(method);
            }
        }
        for (Constructor constructor : constructorMap.values()) {
            if (typeMap.containsKey(constructor.getReturnType())) {
                typeMap.get(constructor.getReturnType()).getConstructorProducers().add(constructor);
            }
        }
        for (Field field : fieldMap.values()) {
            if (typeMap.containsKey(field.getFieldType())) {
                typeMap.get(field.getFieldType()).getFieldProducers().add(field);
            }
        }

        /*for (String key : basicTypeToFieldMap.keySet()) {
            System.out.println(key + " : " + basicTypeToFieldMap.get(key).size());
        }*/
    }

    public static void main(String[] args) {
        new Repository();
    }
}

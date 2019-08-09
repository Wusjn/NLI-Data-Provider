package provider.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Type {
    private String fullQualifiedName;
    private String packageName;
    private String className;


    private List<String> subClasses = new ArrayList<>();

    private List<Method> methodProducers = new ArrayList<>();
    private List<Constructor> constructorProducers = new ArrayList<>();
    private List<Field> fieldProducers = new ArrayList<>();

    public Type(String fullQualifiedName) {
        this.fullQualifiedName = fullQualifiedName;

        int lastDot = fullQualifiedName.lastIndexOf(".");
        this.packageName = fullQualifiedName.substring(0, lastDot);
        this.className = fullQualifiedName.substring(lastDot + 1);
    }

    public Type(){};
}

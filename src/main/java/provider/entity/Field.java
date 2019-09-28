package provider.entity;

import com.github.javaparser.resolution.declarations.ResolvedEnumConstantDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import lombok.Data;

@Data
public class Field {
    private boolean isStatic;
    private String packageName;
    private String className;
    private String fieldType;
    private String fieldName;
    private int usageCount;


    public Field(ResolvedFieldDeclaration r, String classFullName) {
        isStatic = r.isStatic();
        fieldType = r.declaringType().getQualifiedName();
        fieldName = r.getName();

        int lastDotIndex = classFullName.lastIndexOf(".");
        packageName = classFullName.substring(0, lastDotIndex);
        className = classFullName.substring(lastDotIndex + 1);
    }

    public Field(ResolvedEnumConstantDeclaration r, String classFullName) {
        isStatic = true;
        fieldType = classFullName;
        fieldName = r.getName();

        int lastDotIndex = classFullName.lastIndexOf(".");
        packageName = classFullName.substring(0, lastDotIndex);
        className = classFullName.substring(lastDotIndex + 1);
    }

    public Field() {
    }

    ;
}

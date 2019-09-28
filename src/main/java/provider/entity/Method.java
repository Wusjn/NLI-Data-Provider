package provider.entity;

import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Method {
    private boolean isStatic;
    private String qualifiedSignature;
    private String packageName;
    private String className;
    private String shortName;
    private List<String> argsType = new ArrayList<>();
    private List<String> argsName = new ArrayList<>();
    private String returnType;
    private int usageCount;


    public Method(ResolvedMethodDeclaration r) {
        isStatic = r.isStatic();
        qualifiedSignature = r.getQualifiedSignature();
        packageName = r.getPackageName();
        className = r.getClassName();
        shortName = r.getName();
        returnType = r.getReturnType().describe();

        for (int i = 0; i < r.getNumberOfParams(); i++) {
            argsType.add(r.getParam(i).describeType());
            argsName.add(r.getParam(i).getName());
        }
    }

    public Method() {
    }

    ;
}

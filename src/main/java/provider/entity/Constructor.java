package provider.entity;

import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Constructor {
    private String qualifiedSignature;
    private String packageName;
    private String className;
    private List<String> argsType = new ArrayList<>();
    private List<String> argsName = new ArrayList<>();
    private String returnType;
    private int usageCount;


    public Constructor(ResolvedConstructorDeclaration r) {
        qualifiedSignature = r.getQualifiedSignature();
        packageName = r.getPackageName();
        className = r.getClassName();
        returnType = packageName + "." + className;

        for (int i = 0; i < r.getNumberOfParams(); i++) {
            argsType.add(r.getParam(i).describeType());
            argsName.add(r.getParam(i).getName());
        }
    }

    public Constructor() {
    }

    ;
}

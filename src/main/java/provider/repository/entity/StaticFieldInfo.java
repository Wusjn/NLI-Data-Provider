package provider.repository.entity;

import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

public class StaticFieldInfo {
    private Map<String, List<String>> fieldsInfo;

    public StaticFieldInfo() {

    }

    private StaticFieldInfo(ResolvedReferenceTypeDeclaration decl) {
        this.fieldsInfo = decl.getDeclaredFields().stream()
            .filter(ResolvedFieldDeclaration::isStatic)
            .collect(
                groupingBy(
                    (ResolvedFieldDeclaration f) -> f.getType().describe(),
                    mapping(ResolvedFieldDeclaration::getName, toList())
                )
            );
    }

    public static StaticFieldInfo from(ResolvedReferenceTypeDeclaration decl) {
        try {
            return new StaticFieldInfo(decl);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, List<String>> getFieldsInfo() {
        return fieldsInfo;
    }
}

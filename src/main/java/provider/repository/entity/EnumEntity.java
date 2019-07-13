package provider.repository.entity;

import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.resolution.declarations.ResolvedEnumConstantDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedEnumDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserEnumDeclaration;
import org.neo4j.ogm.annotation.NodeEntity;

import java.util.stream.Collectors;

@NodeEntity
public class EnumEntity extends TypeEntity {
    transient private ResolvedReferenceTypeDeclaration resolved;

    private String constants;

    public static EnumEntity fromDeclaration(JavaParserEnumDeclaration decl) {
        return new EnumEntity(decl, decl.getWrappedNode().getJavadocComment().orElse(null));
    }

    protected EnumEntity() {
    }

    protected EnumEntity(ResolvedEnumDeclaration resolved, JavadocComment javadocComment) {
        super(resolved, false, false, javadocComment);
        this.constants = resolved.getEnumConstants().stream().map(ResolvedEnumConstantDeclaration::getName).collect(Collectors.joining(","));
    }

    public String getConstants() {
        return constants;
    }
}

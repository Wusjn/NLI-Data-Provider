package provider.repository.entity;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserInterfaceDeclaration;
import provider.repository.StaticFieldInfoConverter;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.Convert;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class TypeEntity {
    @Id
    private String qualifiedName;
    private String simpleName;
    private boolean isInterface;
    private boolean isAbstract;
    private String javadoc;
    @Convert(StaticFieldInfoConverter.class)
    private StaticFieldInfo staticFields;

    @Relationship(type = "HAS_METHOD")
    private Set<MethodEntity> hasMethods = new HashSet<>();

    @Relationship(type = "EXTENDS")
    private Set<TypeEntity> extendedTypes = new HashSet<>();

    @Relationship(type = "ITERABLE")
    private TypeEntity iterableType;

    @Relationship(type = "ITERABLE", direction = Relationship.INCOMING)
    private Set<TypeEntity> iterables = new HashSet<>();

    @Relationship(type = "EXTENDS", direction = Relationship.INCOMING)
    private Set<TypeEntity> subTypes = new HashSet<>();

    @Relationship(type = "PRODUCES", direction = Relationship.INCOMING)
    private Set<MethodEntity> producers = new HashSet<>();

    @Relationship(type = "PRODUCES_MULTIPLE", direction = Relationship.INCOMING)
    private Set<MethodEntity> multipleProducers = new HashSet<>();

    public static TypeEntity fromDeclaration(JavaParserClassDeclaration resolved) {
        ClassOrInterfaceDeclaration decl = resolved.getWrappedNode();
        return new TypeEntity(resolved, decl.isInterface(), decl.isAbstract(), decl.getJavadocComment().orElse(null));
    }

    public static TypeEntity fromDeclaration(JavaParserInterfaceDeclaration resolved) {
        ClassOrInterfaceDeclaration decl = resolved.getWrappedNode();
        return new TypeEntity(resolved, decl.isInterface(), decl.isAbstract(), decl.getJavadocComment().orElse(null));
    }

    public static TypeEntity fake(String qualifiedName) {
        TypeEntity typeEntity = new TypeEntity();
        typeEntity.qualifiedName = qualifiedName;
        return typeEntity;
    }

    protected TypeEntity() {
    }

    protected TypeEntity(ResolvedReferenceTypeDeclaration resolved, boolean isInterface, boolean isAbstract, JavadocComment javadocComment) {
        this.qualifiedName = resolved.getQualifiedName();
        this.simpleName = resolved.getName();
        this.isInterface = isInterface;
        this.isAbstract = isAbstract;
        this.staticFields = StaticFieldInfo.from(resolved);
        this.javadoc = javadocComment == null ? "" : javadocComment.getContent();
    }

    public void addHasMethod(MethodEntity methodEntity) {
        this.hasMethods.add(methodEntity);
    }

    public void addExtendedTypes(Set<TypeEntity> extendedTypes) {
        this.extendedTypes.addAll(extendedTypes);
        this.extendedTypes.forEach(t -> t.addSubType(this));
    }

    public void addSubType(TypeEntity subType) {
        this.subTypes.add(subType);
    }

    public void addProducer(MethodEntity producer) {
        this.producers.add(producer);
    }

    public void addMultipleProducer(MethodEntity producer) {
        this.multipleProducers.add(producer);
    }

    public void setIterableType(TypeEntity iterableType) {
        this.iterableType = iterableType;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public Set<MethodEntity> getHasMethods() {
        return hasMethods;
    }

    public Set<TypeEntity> getExtendedTypes() {
        return Collections.unmodifiableSet(extendedTypes);
    }

    public Set<TypeEntity> getSubTypes() {
        return Collections.unmodifiableSet(subTypes);
    }

    public Set<MethodEntity> getProducers() {
        return Collections.unmodifiableSet(producers);
    }

    public Set<MethodEntity> getMultipleProducers() {
        return Collections.unmodifiableSet(multipleProducers);
    }

    public Set<TypeEntity> getIterables() {
        return Collections.unmodifiableSet(iterables);
    }

    public StaticFieldInfo getStaticFields() {
        return staticFields;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public boolean isInterface() {
        return isInterface;
    }
}

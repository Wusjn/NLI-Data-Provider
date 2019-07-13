package provider.repository.entity;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedParameterDeclaration;
import com.google.common.collect.ImmutableMap;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Transient;

import java.util.*;
import java.util.stream.Collectors;

@NodeEntity
public class MethodEntity {
    @Id
    private String qualifiedSignature;
    private String signature;
    private String simpleName;
    private boolean isStatic;
    private boolean isConstructor;
    private boolean isDeprecated;
    private AccessSpecifier accessSpecifier;
    private String paramNames;
    private String returnType;

    @Relationship(type = "HAS_METHOD", direction = Relationship.INCOMING)
    private TypeEntity declareType;

    @Relationship(type = "EXTENDS")
    private Set<MethodEntity> extendedMethods = new HashSet<>();

    @Relationship(type = "RETURNS")
    private TypeEntity returns;

    @Relationship(type = "RETURNS_MULTIPLE")
    private TypeEntity returnsMultiple;

    @Relationship(type = "JAVADOC")
    private MethodJavadocEntity javadoc;

    @Transient
    private Map<String, String> paramJavadocs;

    public MethodEntity() {
    }

    public MethodEntity(ResolvedConstructorDeclaration resolved, boolean isDeprecated, TypeEntity declareType, MethodJavadocEntity javadoc) {
        this.qualifiedSignature = resolved.getQualifiedSignature();
        this.signature = resolved.getSignature();
        this.simpleName = resolved.getName();
        this.isStatic = true;
        this.isConstructor = true;
        this.isDeprecated = isDeprecated;
        this.accessSpecifier = resolved.accessSpecifier();
        this.declareType = declareType;
        this.javadoc = javadoc;
        this.returnType = resolved.getClassName();
        List<String> paramNames = new ArrayList<>();
        int paramNum = resolved.getNumberOfParams();
        for (int i = 0; i < paramNum; ++i) {
            ResolvedParameterDeclaration param = resolved.getParam(i);
            paramNames.add(param.getName());
        }
        this.paramNames = String.join(",", paramNames);
        declareType.addHasMethod(this);
    }

    public MethodEntity(ResolvedMethodDeclaration resolved, boolean isDeprecated, TypeEntity declareType, MethodJavadocEntity javadoc) {
        this.qualifiedSignature = resolved.getQualifiedSignature();
        this.signature = resolved.getSignature();
        this.simpleName = resolved.getName();
        this.isStatic = resolved.isStatic();
        this.isConstructor = false;
        this.isDeprecated = isDeprecated;
        this.accessSpecifier = resolved.accessSpecifier();
        this.declareType = declareType;
        this.javadoc = javadoc;
        this.returnType = resolved.getReturnType().describe();
        List<String> paramNames = new ArrayList<>();
        int paramNum = resolved.getNumberOfParams();
        for (int i = 0; i < paramNum; ++i) {
            ResolvedParameterDeclaration param = resolved.getParam(i);
            paramNames.add(param.getName());
        }
        this.paramNames = String.join(",", paramNames);
        declareType.addHasMethod(this);
    }

    public void lazySetupParamJavadocs() {
        this.paramJavadocs = javadoc == null ?
            ImmutableMap.of() :
            javadoc.getParams().stream().collect(Collectors.toMap(MethodParamJavadocEntity::getName, MethodParamJavadocEntity::getDescription));
    }

    public void addExtendedMethods(Set<MethodEntity> extendedMethods) {
        this.extendedMethods.addAll(extendedMethods);
    }

    public void setReturns(TypeEntity returns) {
        assert this.returns == null;
        this.returns = returns;
        returns.addProducer(this);
    }

    public void setReturnsMultiple(TypeEntity returnsMultiple) {
        assert this.returnsMultiple == null;
        this.returnsMultiple = returnsMultiple;
        returnsMultiple.addMultipleProducer(this);
    }

    public String getSignature() {
        return signature;
    }

    public String getQualifiedSignature() {
        return qualifiedSignature;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public TypeEntity getDeclareType() {
        return declareType;
    }

    public TypeEntity getReturns() {
        return returns;
    }

    public String getReturnType() {
        return returnType;
    }

    public AccessSpecifier getAccessSpecifier() {
        return accessSpecifier;
    }

    public String getParamNames() {
        return paramNames;
    }

    public MethodJavadocEntity getJavadoc() {
        return javadoc;
    }

    public String getParamJavadoc(String param) {
        if (this.paramJavadocs == null) {
            lazySetupParamJavadocs();
        }
        return this.paramJavadocs.get(param);
    }

    public Set<MethodEntity> getExtendedMethods() {
        return Collections.unmodifiableSet(extendedMethods);
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isDeprecated() {
        return isDeprecated;
    }
}

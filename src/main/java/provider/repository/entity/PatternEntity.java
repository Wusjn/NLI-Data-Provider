package provider.repository.entity;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
@NodeEntity
public class PatternEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String pattern;

    @Relationship(type = "HAS_TYPE")
    private Set<TypeEntity> hasTypes = new HashSet<>();

    @Relationship(type = "HAS_METHOD")
    private Set<MethodEntity> hasMethods = new HashSet<>();

    public static PatternEntity create(String pattern) {
        return new PatternEntity(pattern);
    }

    protected PatternEntity() {
    }

    protected PatternEntity(String pattern) {
        this.pattern = pattern;
    }

    public void addHasType(TypeEntity typeEntity) {
        hasTypes.add(typeEntity);
    }

    public void addHasTypes(Collection<TypeEntity> typeEntitys) {
        hasTypes.addAll(typeEntitys);
    }

    public void addHasMethod(MethodEntity methodEntity) {
        hasMethods.add(methodEntity);
    }

    public void addHasMethods(Collection<MethodEntity> methodEntitys) {
        hasMethods.addAll(methodEntitys);
    }

    public String getPattern() {
        return pattern;
    }

    public Set<TypeEntity> getHasTypes() {
        return Collections.unmodifiableSet(hasTypes);
    }

    public Set<MethodEntity> getHasMethods() {
        return Collections.unmodifiableSet(hasMethods);
    }

}

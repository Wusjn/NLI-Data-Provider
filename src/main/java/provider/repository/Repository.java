package provider.repository;

import provider.repository.entity.EnumEntity;
import provider.repository.entity.MethodEntity;
import provider.repository.entity.TypeEntity;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.SessionFactory;

import java.io.FileInputStream;
import java.util.*;

public class Repository {
    Map<String,TypeEntity> types = new HashMap<>();
    Map<String,EnumEntity> enums = new HashMap<>();
    Map<String,MethodEntity> methods = new HashMap<>();
    SessionFactory sessionFactory;

    public Repository(){
        Properties properties = new Properties();
        try{
            properties.load(new FileInputStream("src/main/resources/application.properties"));
        }catch (Exception e){
            e.printStackTrace();
        }
        String neo4jServerUri = properties.getProperty("neo4jServerUri");
        String neo4jServerUserName = properties.getProperty("neo4jServerUserName");
        String neo4jServerPassword = properties.getProperty("neo4jServerPassword");

        Configuration configuration = new Configuration.Builder().uri(neo4jServerUri).credentials(neo4jServerUserName,neo4jServerPassword).build();
        sessionFactory = new SessionFactory(configuration,"provider.repository.entity");
        Neo4jSession session = (Neo4jSession) sessionFactory.openSession();

        ArrayList<TypeEntity> typesList = new ArrayList<>(session.loadAll(TypeEntity.class));
        ArrayList<EnumEntity> enumsList = new ArrayList<>(session.loadAll(EnumEntity.class));
        ArrayList<MethodEntity> methodsList = new ArrayList<>(session.loadAll(MethodEntity.class));
        for (TypeEntity typeEntity : typesList){
            types.put(typeEntity.getQualifiedName(),typeEntity);
        }
        for (EnumEntity enumEntity : enumsList){
            enums.put(enumEntity.getQualifiedName(),enumEntity);
        }
        for (MethodEntity methodEntity : methodsList){
            methods.put(methodEntity.getQualifiedSignature(),methodEntity);
        }

        sessionFactory.close();
    }

    public TypeEntity getTyoe(String qualifiedName){
       TypeEntity type = enums.get(qualifiedName);
       if (type == null){
           type = types.get(qualifiedName);
       }
       return type;
    }

    public EnumEntity getEnum(String qualifiedName){
        return enums.get(qualifiedName);
    }

    public MethodEntity getMethod(String qualifiedSignature){
        return methods.get(qualifiedSignature);
    }


    public static void main(String[] args) {
        Repository repository = new Repository();
        TypeEntity typeEntity = repository.getTyoe("org.apache.poi.ss.usermodel.Workbook");
        Set<MethodEntity> methodEntities = typeEntity.getProducers();
        for (MethodEntity methodEntity : methodEntities){
            System.out.println(methodEntity.getSignature());
        }
    }
}

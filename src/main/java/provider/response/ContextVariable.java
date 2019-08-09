package provider.response;


import lombok.Data;

@Data
public class ContextVariable {
    private String qualifiedType;
    private String name;
    public ContextVariable(){}
    public ContextVariable(String qualifiedType, String name){
        this.qualifiedType = qualifiedType;
        this.name = name;
    }
}

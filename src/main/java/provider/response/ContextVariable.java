package provider.response;

import lombok.Data;

@Data
public class ContextVariable implements Comparable<ContextVariable> {
    private String qualifiedType;
    private String name;
    private int depthToCaret;
    private int distanceToCaret;
    private int nthToTheCaret;

    public ContextVariable() {
    }

    public ContextVariable(String qualifiedType, String name, int depthToCaret, int distanceToCaret) {
        this.qualifiedType = qualifiedType;
        this.name = name;
        this.depthToCaret = depthToCaret;
        this.distanceToCaret = distanceToCaret;
    }

    @Override
    public int compareTo(ContextVariable cv) {
        if (depthToCaret != cv.depthToCaret) {
            return depthToCaret - cv.depthToCaret;
        } else {
            return distanceToCaret - cv.distanceToCaret;
        }
    }


    /*@Override
    public boolean equals(Object obj){
        if (this == obj){
            return true;
        }
        if (!(obj instanceof ContextVariable)){
            return false;
        }
        ContextVariable cv = (ContextVariable) obj;
        return name.equals(cv.name);
    }*/
}

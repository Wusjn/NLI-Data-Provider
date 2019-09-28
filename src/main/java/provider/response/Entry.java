package provider.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Entry {
    private String text;
    private List<String> typeList = new ArrayList<>();
    private double score;

    public Entry(String text, List<String> typeList, double score) {
        this.text = text;
        this.typeList = typeList;
        this.score = score;
    }

    public Entry() {
    }
}

package provider.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class NLI {
    private String functionalFeature;
    private List<String> text = new ArrayList<>();
    private List<String> type = new ArrayList<>();
    private List<String> info = new ArrayList<>();
    private Map<String, String> symbol = new HashMap<>();


}

package provider.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import provider.response.NLI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RestController
public class NLIController {

    List<NLI> NLIList = new ArrayList<>();
    NLI[] NLIs;
    String NLILocation = "/Users/apple/Downloads/Wusj/NLI-Data-Provider/NLIs";

    public NLIController(){
        File NLIDirectory = new File(NLILocation);
        ObjectMapper objectMapper = new ObjectMapper();
        for (File NLIFile : NLIDirectory.listFiles()){
            NLI nli = new NLI();
            try {
                JsonNode jsonNode = objectMapper.readTree(NLIFile);

                nli.setFunctionalFeature(jsonNode.get("functionalFeature").asText());
                for (JsonNode textFrag : jsonNode.withArray("text")){
                    nli.getText().add(textFrag.asText());
                }
                for (JsonNode typeFrag : jsonNode.withArray("type")){
                    nli.getType().add(typeFrag.asText());
                }
                for (JsonNode infoFrag : jsonNode.withArray("info")){
                    nli.getInfo().add(infoFrag.asText());
                }
                for (Iterator<Map.Entry<String, JsonNode>> it = jsonNode.with("symbol").fields(); it.hasNext(); ) {
                    Map.Entry<String, JsonNode> symbolFrag = it.next();
                    nli.getSymbol().put(symbolFrag.getKey(),symbolFrag.getValue().asText());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            NLIList.add(nli);
        }
        NLIs = new NLI[NLIList.size()];
        for (int i=0;i<NLIList.size();i++){
            NLIs[i] = NLIList.get(i);
        }
    }

    @RequestMapping("/NLI")
    public NLI[] getNLI(){
        return NLIs;
    }

}

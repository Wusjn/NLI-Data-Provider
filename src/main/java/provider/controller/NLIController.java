package provider.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import provider.response.NLI;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@RestController
public class NLIController {

    List<NLI> NLIList = new ArrayList<>();
    NLI[] NLIs;
    URL NLILocation = this.getClass().getResource("/NLIs");

    public NLIController() {
        ObjectMapper objectMapper = new ObjectMapper();
        JarFile jarFile = null;
        try {
            jarFile = ((JarURLConnection) NLILocation.openConnection()).getJarFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
        while (jarEntryEnumeration.hasMoreElements()) {
            JarEntry jarEntry = jarEntryEnumeration.nextElement();

            if (jarEntry.getName().startsWith("NLIs/") && !jarEntry.isDirectory()) {
                InputStream inputStream = this.getClass().getResourceAsStream("/" + jarEntry.getName());
                NLI nli = new NLI();

                try {
                    JsonNode jsonNode = objectMapper.readTree(inputStream);

                    nli.setFunctionalFeature(jsonNode.get("functionalFeature").asText());
                    for (JsonNode textFrag : jsonNode.withArray("text")) {
                        nli.getText().add(textFrag.asText());
                    }
                    for (JsonNode typeFrag : jsonNode.withArray("type")) {
                        nli.getType().add(typeFrag.asText());
                    }
                    for (JsonNode infoFrag : jsonNode.withArray("info")) {
                        nli.getInfo().add(infoFrag.asText());
                    }
                    for (Iterator<Map.Entry<String, JsonNode>> it = jsonNode.with("symbol").fields(); it.hasNext(); ) {
                        Map.Entry<String, JsonNode> symbolFrag = it.next();
                        nli.getSymbol().put(symbolFrag.getKey(), symbolFrag.getValue().asText());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                NLIList.add(nli);
            }
        }

        NLIs = new NLI[NLIList.size()];
        for (int i = 0; i < NLIList.size(); i++) {
            NLIs[i] = NLIList.get(i);
        }
    }

    @RequestMapping("/NLI")
    @CrossOrigin
    public NLI[] getNLI() {
        System.out.println("sended some NLIs");
        return NLIs;
    }

}

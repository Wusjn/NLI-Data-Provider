package provider.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class Recommendation {

    private List<Entry> entries = new ArrayList<>();

    public void addEntry(Entry entry) {
        entries.add(entry);
    }

    public void trim(int maxNum) {
        for (int i = entries.size() - 1; i >= maxNum; i--) {
            entries.remove(i);
        }
    }
}

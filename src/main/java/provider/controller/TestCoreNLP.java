package provider.controller;

import edu.stanford.nlp.util.EditDistance;

public class TestCoreNLP {
    public static void main(String[] args) {
        EditDistance editDistance = new EditDistance();
        System.out.println(editDistance.score("Background Color", "IndexedColor.RED"));
        System.out.println(editDistance.score("Background Color", "BackgroundColor"));
    }
}

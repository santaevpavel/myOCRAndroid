package ru.myocr.model.align;

import java.util.List;

public class DataBaseFinder {
    private final List<String> products;

    public DataBaseFinder(List<String> products) {
        this.products = products;
    }

    public String find(String ocrProduct) {
        int bestScore = Integer.MIN_VALUE;
        String bestScoreProduct = "";
        for (String product : products) {
            Aligner aligner = new SimpleAligner();
            int alignScore = aligner.align(ocrProduct, product);
            if (alignScore > bestScore) {
                bestScore = alignScore;
                bestScoreProduct = product;
            }
        }

        return bestScoreProduct;
    }
}

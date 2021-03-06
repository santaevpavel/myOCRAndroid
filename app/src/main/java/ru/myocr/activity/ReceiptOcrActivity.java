package ru.myocr.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import ru.myocr.activity.adapter.ReceiptDataViewAdapter;
import ru.myocr.align.DataBaseFinder;
import ru.myocr.db.DbStub;
import ru.myocr.model.OcrParser;
import ru.myocr.model.R;
import ru.myocr.model.ReceiptData;
import ru.myocr.model.ReceiptDataImpl;
import ru.myocr.model.databinding.ActivityReceiptOcrBinding;

public class ReceiptOcrActivity extends AppCompatActivity implements ReceiptDataViewAdapter.OnItemClickListener {

    private ActivityReceiptOcrBinding binding;

    private boolean hasProducts;
    private ReceiptData receiptData;
    private OcrParser parser;

    private ReceiptDataViewAdapter receiptViewAdapter;
    private List<Pair<String, String>> productPricePairs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_ocr);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_receipt_ocr);
        binding.buttonScanPrices.setOnClickListener(v -> runOcrTextScanner());

        handleIncomingText(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIncomingText(intent);
    }

    private void removeProduct(int position) {
        final List<String> products = receiptData.getProducts();
        products.remove(position);
        updateProductsView();
    }

    private void removePrice(int position) {
        final List<String> prices = receiptData.getPrices();
        prices.remove(position);
        updateProductsView();
    }

    private void handleIncomingText(Intent intent) {
        if (intent != null) {
            handleText(intent);
        }
    }

    void handleText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            if (!hasProducts) {
                updateProducts(sharedText);
                hasProducts = true;
            } else {
                updatePrices(sharedText);
            }
        }
    }

    private void updateProducts(String sharedText) {
        parser = new OcrParser(sharedText);
        final List<String> products = parser.parseProductList();
        final List<String> matchesProducts = replaceMatchesInDB(products);
        receiptData = new ReceiptDataImpl(matchesProducts);
        updateProductsView();
    }

    private List<String> replaceMatchesInDB(List<String> ocrProducts) {
        final DbStub db = new DbStub();
        final List<String> allProducts = db.getAllProducts(this);

        final DataBaseFinder finder = new DataBaseFinder(allProducts);
        final List<String> matchProducts = finder.findAll(ocrProducts);

        return matchProducts;
    }

    private void updatePrices(String sharedText) {
        parser.setPricesText(sharedText);
        final List<String> products = receiptData.getProducts();
        final List<String> prices = parser.parsePriceList();
        receiptData = new ReceiptDataImpl(products, prices);
        updateProductsView();
    }

    private void updateProductsView() {
        productPricePairs.clear();
        productPricePairs.addAll(receiptData.getProductsPricesPairs());
        if (receiptViewAdapter == null) {
            receiptViewAdapter = new ReceiptDataViewAdapter(this, productPricePairs, this);
            binding.listReceiptData.setAdapter(receiptViewAdapter);
        } else {
            receiptViewAdapter.notifyDataSetChanged();
        }

    }

    private void runOcrTextScanner() {
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.offline.ocr.english.image.to.text");
        startActivity(intent);
    }

    @Override
    public void onClickProduct(int pos) {
        removeProduct(pos);
    }

    @Override
    public void onClickPrice(int pos) {
        removePrice(pos);
    }
}

package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import it.jaschke.alexandria.services.BookService;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class Scanner extends ActionBarActivity implements ZBarScannerView.ResultHandler {
    private ZBarScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZBarScannerView(this);
        setContentView(mScannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        if(rawResult.getBarcodeFormat().getName().equals("ISBN13")||rawResult.getBarcodeFormat().getName().equals("ISBN10")) {
            mScannerView.stopCamera();
            Intent intent = this.getIntent();
            intent.putExtra(getString(R.string.scan_isbn_key), "978"+rawResult.getContents());
            if (getParent() == null) {
                setResult(Activity.RESULT_OK, intent);
            } else {
                getParent().setResult(Activity.RESULT_OK, intent);
            }
            finish();
        }
        else{
            Toast.makeText(this, "Please scan a barcode of ISBN10 or ISBN13",Toast.LENGTH_SHORT).show();
            mScannerView.startCamera();
        }
    }
}
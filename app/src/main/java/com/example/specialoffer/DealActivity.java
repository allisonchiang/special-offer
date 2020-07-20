package com.example.specialoffer;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import com.squareup.picasso.Picasso;

public class DealActivity extends AppCompatActivity {

    Typeface textFont;
    ImageView logo, qrCode;
    TextView name, address, website, offerDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);

        textFont = Typeface.createFromAsset(getAssets(), "fonts/Acme-Regular.ttf");

        logo = findViewById(R.id.logo);
        name = findViewById(R.id.name);
        address = findViewById(R.id.address);
        website = findViewById(R.id.website);
        offerDetails = findViewById(R.id.offerDetails);
        qrCode = findViewById(R.id.qrCode);

        name.setTypeface(textFont);
        address.setTypeface(textFont);
        website.setTypeface(textFont);
        offerDetails.setTypeface(textFont);

        String id = getIntent().getStringExtra("FENCE");
        FenceData fenceData = FenceMgr.getFenceData(id);

        // change background color
        String backgroundColor = fenceData.getFenceColor();
        getWindow().getDecorView().setBackgroundColor(Color.parseColor(backgroundColor));

        // load business logo using Picasso
        Picasso picasso = new Picasso.Builder(this).build();
        picasso.setLoggingEnabled(true);
        picasso.load(fenceData.getLogo()).into(logo);

        name.setText(fenceData.getId());
        address.setText(fenceData.getAddress());
        website.setText(fenceData.getWebsite());
        offerDetails.setText(fenceData.getMessage());

        // make business address and website clickable
        Linkify.addLinks(address, Linkify.ALL);
        Linkify.addLinks(website, Linkify.ALL);

        // generate offer QR code
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(fenceData.getCode(), BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            // The below Bitmap is what will be displayed in an ImageView in the Activity
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            // The below line uses the Bitmap just created as the ImageView’s image bitmap
            ((ImageView) findViewById(R.id.qrCode)).setImageBitmap(bmp);
        } catch (WriterException e) {
            e.printStackTrace();
        }

    }
}
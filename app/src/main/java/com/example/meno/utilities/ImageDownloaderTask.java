package com.example.meno.utilities;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
    private RoundedImageView imageView;

    public ImageDownloaderTask(RoundedImageView imageView) {
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        String imageUrl = urls[0];
        try {
            // Download image using Picasso
            return Picasso.get().load(imageUrl).get();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if (result != null) {
            imageView.setImageBitmap(result);
        }
    }
}

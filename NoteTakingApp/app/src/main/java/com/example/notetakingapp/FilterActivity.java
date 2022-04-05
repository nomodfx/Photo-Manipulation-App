package com.example.notetakingapp;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.view.Window;
import android.net.Uri;
import android.widget.ImageView;
import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FilterActivity extends AppCompatActivity {
    private static final int pickImage = 100;
    Button loadImg, filter_1, filter_2, filter_3, filter_4, filter_5, filter_6, filter_7, filter_8, filter_9, saveImg;
    ImageView imageView;
    Uri imageUri;

    RadioGroup radioGroup;
    RadioButton pngButton, jpegButton;
    int saveFormat;

    TextInputEditText saveTitle;
    String imgName = "Image";

    Bitmap previewImage;//This will get the bitmap from the selected image
    Bitmap operation;

    //Gallery stored in phone emulator
    private void openGallery()
    {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, pickImage);
    }

    private void getBitmap()//get bits from ImageView.\
    {
        if (imageView != null) {
            imageView.invalidate();
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            if (drawable != null)
                previewImage = drawable.getBitmap();
        }
    }

    private void saveBitmap()//Inject bits into image and save
    {
        if (operation == null) // If an image isnt loaded
        {
            Toast.makeText(getApplicationContext(), //Context
                    "Please apply  a filter", // Message to display
                    Toast.LENGTH_SHORT // Duration of the message, another possible value is Toast.LENGTH_LONG
            ).show(); //Finally Show the toast
        }
        else if(radioGroup.getCheckedRadioButtonId() == -1)//no image format specified
        {
            Toast.makeText(getApplicationContext(), //Context
                    "Please specify an image format ", // Message to display
                    Toast.LENGTH_SHORT // Duration of the message, another possible value is Toast.LENGTH_LONG
            ).show(); //Finally Show the toast
        }
        else//We're good to go for saving
        {//We're good to go for saving
            if (android.os.Build.VERSION.SDK_INT >= 29)
            {
                imgName = saveTitle.getEditableText().toString().trim();//get name, if empty, a placeholder name will automatically be used
                ContentValues values = contentValues();
                values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + getString(R.string.app_name));
                values.put(MediaStore.Images.Media.DISPLAY_NAME, imgName);
                values.put(MediaStore.Images.Media.IS_PENDING, true);

                Uri uri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    try {
                        saveImageToStream(operation, this.getContentResolver().openOutputStream(uri));
                        values.put(MediaStore.Images.Media.IS_PENDING, false);
                        this.getContentResolver().update(uri, values, null, null);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }

            }
        }

    }

    private ContentValues contentValues() {//Content values are used when we manipulate data in Android, in this case is to add details such as Date to the created image
        ContentValues values = new ContentValues();
        switch(radioGroup.getCheckedRadioButtonId())
        {
            case(R.id.pngRadioButton):
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                break;
            case(R.id.jpegRadioButton):
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                break;
            default:
        }
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        }
        return values;
    }

    private void saveImageToStream(Bitmap bitmap, OutputStream outputStream) { //Here we save the image based on the format specified by user
        if (outputStream != null) {
            try {
                saveFormat = radioGroup.getCheckedRadioButtonId();// get which option the user picked
                switch (saveFormat) {
                    case R.id.pngRadioButton:
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        //Inform User
                        Toast.makeText(getApplicationContext(), //Context
                                "Image saved as PNG", // Message to display
                                Toast.LENGTH_SHORT // Duration of the message, another possible value is Toast.LENGTH_LONG
                        ).show(); //Finally Show the toast
                        break;
                    case R.id.jpegRadioButton:
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        //Inform user
                        Toast.makeText(getApplicationContext(), //Context
                                "Image saved as JPEG", // Message to display
                                Toast.LENGTH_SHORT // Duration of the message, another possible value is Toast.LENGTH_LONG
                        ).show(); //Finally Show the toast
                        break;
                    default://redundant as we check before creating an image, but here it is just in case
                        Toast.makeText(getApplicationContext(), //Context
                                "Please Select an Image Format", // Message to display
                                Toast.LENGTH_SHORT // Duration of the message, another possible value is Toast.LENGTH_LONG
                        ).show(); //Finally Show the toast
                }
                //Send notification to user!
                NotificationCompat.Builder builder = new NotificationCompat.Builder(FilterActivity.this, "My Notification");
                builder.setContentTitle("Image Saved");
                builder.setContentText("Filtered Image Saved to Gallery");
                builder.setSmallIcon(R.drawable.ic_launcher_background);
                builder.setAutoCancel(true);

                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(FilterActivity.this);
                managerCompat.notify(1, builder.build());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == pickImage){
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_filter);

        imageView = findViewById(R.id.imageView);
        loadImg = findViewById(R.id.loadImage);
        saveImg = findViewById(R.id.saveFilterButton);
        saveTitle =findViewById(R.id.saveTitle);

        //Filter Button ID
        filter_1 = findViewById(R.id.filter1);
        filter_2 = findViewById(R.id.filter2);
        filter_3 = findViewById(R.id.filter3);
        filter_4 = findViewById(R.id.filter4);
        filter_5 = findViewById(R.id.filter5);
        filter_6 = findViewById(R.id.filter6);
        filter_7 = findViewById(R.id.filter7);
        filter_8 = findViewById(R.id.filter8);
        filter_9 = findViewById(R.id.filter9);
        //Radio Buttons
        pngButton = findViewById(R.id.pngRadioButton);
        jpegButton = findViewById(R.id.jpegRadioButton);
        radioGroup = findViewById(R.id.saveFormat);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("My Notification", "My Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        //Opens gallery for picture input and later photo manipulation
        loadImg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                openGallery();
            }
        });
        saveImg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if(imageView != null) {
                    saveBitmap();
                }

            }
        });

        //Invert Filter
        filter_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBitmap();
                if (previewImage != null) {
                    System.out.println("Starting Filter 1");
                    operation = Bitmap.createBitmap(previewImage.getWidth(), previewImage.getHeight(), previewImage.getConfig());

                    for (int i = 0; i < previewImage.getWidth(); i++) {
                        for (int j = 0; j < previewImage.getHeight(); j++) {
                            int p = previewImage.getPixel(i, j);
                            int oldR = Color.red(p);
                            int oldG = Color.green(p);
                            int oldB = Color.blue(p);
                            int alpha = Color.alpha(p);
                            // invert colors by subtracting current values from the max
                            int newR = 255 - oldR;
                            int newG = 255 - oldG;
                            int newB = 255 - oldB;
                            operation.setPixel(i, j, Color.argb(alpha, newR, newG, newB));
                        }
                    }
                    imageView.setImageBitmap(operation);
                    System.out.println("Done!");

                }
                else {
                    Toast.makeText(getApplicationContext(), //Context
                            "Please load image first ", // Message to display
                            Toast.LENGTH_SHORT // Duration of the message, another possible value is Toast.LENGTH_LONG
                    ).show(); //Finally Show the toast
                }

            }

        });

        //Saturation Filter
        filter_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBitmap();
                if (previewImage != null) {
                    System.out.println("Starting Filter 2");
                    operation = Bitmap.createBitmap(previewImage.getWidth(), previewImage.getHeight(), previewImage.getConfig());

                    for (int i = 0; i < previewImage.getWidth(); i++) {
                        for (int j = 0; j < previewImage.getHeight(); j++) {
                            int p = previewImage.getPixel(i, j);
                            int a = Color.alpha(p);
                            float[] hsv = new float[3];

                            // Increase saturation
                            Color.colorToHSV(p, hsv);
                            hsv[1] = (float) (hsv[1] + 0.2f);
                            // Ensure numbers don't exceed bounds
                            if (hsv[1] < 0.0f) {
                                hsv[1] = 0.0f;
                            }
                            else if (hsv[1] > 1.0f) {
                                hsv[1] = 1.0f;
                            }

                            int saturated = Color.HSVToColor(a, hsv);
                            operation.setPixel(i, j, saturated);
                        }
                    }
                    imageView.setImageBitmap(operation);
                    System.out.println("Done!");
                }else {
                    Toast.makeText(getApplicationContext(), //Context
                            "Please load image first ", // Message to display
                            Toast.LENGTH_SHORT // Duration of the message, another possible value is Toast.LENGTH_LONG
                    ).show(); //Finally Show the toast
                }
            }
        });

        //Horizontal Flip Filter
        filter_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBitmap();
                if (previewImage != null) {
                    System.out.println("Starting horizontal flip");
                    operation = Bitmap.createBitmap(previewImage.getWidth(), previewImage.getHeight(), previewImage.getConfig());
                    int width = previewImage.getWidth();
                    int height = previewImage.getHeight();
                    // flip image horizontally by copying pixels on one side to the other.
                    for (int i = 0; i < height; i++){
                        for (int j = 0; j < width / 2; j++){
                            int temp = previewImage.getPixel(j,i);
                            int p = previewImage.getPixel((width - j - 1),i);
                            operation.setPixel(j,i, Color.argb(Color.alpha(p), Color.red(p), Color.green(p), Color.blue(p)));
                            operation.setPixel((width - j - 1), i, Color.argb(Color.alpha(temp), Color.red(temp), Color.green(temp), Color.blue(temp)));
                        }
                        if (width % 2 != 0){
                            int last = previewImage.getPixel((width/2),i);
                            operation.setPixel((width/2),i,Color.argb(Color.alpha(last), Color.red(last), Color.green(last), Color.blue(last)));
                        }
                    }
                    imageView.setImageBitmap(operation);
                    System.out.println("Done!");
                }else {
                    Toast.makeText(getApplicationContext(), //Context
                            "Please load image first ", // Message to display
                            Toast.LENGTH_SHORT // Duration of the message, another possible value is Toast.LENGTH_LONG
                    ).show(); //Finally Show the toast
                }
            }
        });

        //Vertical Flip Filter
        filter_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBitmap();
                if (previewImage != null) {
                    System.out.println("Starting horizontal flip");
                    operation = Bitmap.createBitmap(previewImage.getWidth(), previewImage.getHeight(), previewImage.getConfig());
                    int width = previewImage.getWidth();
                    int height = previewImage.getHeight();
                    // flip image vertically by copying pixels on one side to the other.
                    for (int i = 0; i < width; i++){
                        for (int j = 0; j < height / 2; j++){
                            int temp = previewImage.getPixel(i,j);
                            int p = previewImage.getPixel(i,(height - j - 1) );
                            operation.setPixel(i,j, Color.argb(Color.alpha(p), Color.red(p), Color.green(p), Color.blue(p)));
                            operation.setPixel(i,(height - j - 1), Color.argb(Color.alpha(temp), Color.red(temp), Color.green(temp), Color.blue(temp)));
                        }
                        if (height % 2 != 0){
                            int last = previewImage.getPixel(i,(height/2));
                            operation.setPixel(i,(height/2),Color.argb(Color.alpha(last), Color.red(last), Color.green(last), Color.blue(last)));
                        }
                    }
                    imageView.setImageBitmap(operation);
                    System.out.println("Done!");
                }else {
                    Toast.makeText(getApplicationContext(), //Context
                            "Please load image first ", // Message to display
                            Toast.LENGTH_SHORT // Duration of the message, another possible value is Toast.LENGTH_LONG
                    ).show(); //Finally Show the toast
                }
            }
        });

        //Sepia Filter
        filter_5.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                getBitmap();
                if (previewImage != null) {

                    operation = Bitmap.createBitmap(previewImage.getWidth(), previewImage.getHeight(), previewImage.getConfig());

                    for (int i = 0; i < previewImage.getWidth(); i++) {
                        for (int j = 0; j < previewImage.getHeight(); j++) {
                            int p = previewImage.getPixel(i, j);
                            int oldR = Color.red(p);
                            int oldG = Color.green(p);
                            int oldB = Color.blue(p);
                            int alpha = Color.alpha(p);

                            int newR = (int) (0.39 * oldR+ 0.77 * oldG + 0.19 * oldB);
                            int newG = (int) (0.35 * oldR + 0.68 * oldG + 0.17 * oldB);
                            int newB = (int) (0.27 * oldR + 0.53 * oldG + 0.13 * oldB);

                            if(newR > 255) {
                                newR = 255;
                            }
                            if (newG > 255) {
                                newG =255;
                            }
                            if (newB > 255) {
                                newB = 255;
                            }

                            operation.setPixel(i, j, Color.argb(Color.alpha(p), newR, newG, newB));
                        }
                    }
                    imageView.setImageBitmap(operation);
                    System.out.println("Done!");
                }else {
                    Toast.makeText(getApplicationContext(), //Context
                            "Please load image first ", // Message to display
                            Toast.LENGTH_SHORT // Duration of the message, another possible value is Toast.LENGTH_LONG
                    ).show(); //Finally Show the toast
                }
            }
        });

        //Blue Tint Filter
        filter_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBitmap();
                if (previewImage != null) {
                    System.out.println("Starting Filter 1");
                    operation = Bitmap.createBitmap(previewImage.getWidth(), previewImage.getHeight(), previewImage.getConfig());
                    // tint the image blue
                    for (int i = 0; i < previewImage.getWidth(); i++) {
                        for (int j = 0; j < previewImage.getHeight(); j++) {
                            int p = previewImage.getPixel(i, j);
                            int oldR = Color.red(p);
                            int oldG = Color.green(p);
                            int oldB = Color.blue(p);
                            int alpha = Color.alpha(p);
                            int newR = oldR;
                            int newG = oldG;
                            int newB = (int)(oldB + (255 - oldB) * 0.5);

                            operation.setPixel(i, j, Color.argb(alpha, newR, newG, newB));
                        }
                    }
                    imageView.setImageBitmap(operation);
                    System.out.println("Done!");

                }
                else {
                    Toast.makeText(getApplicationContext(), //Context
                            "Please load image first ", // Message to display
                            Toast.LENGTH_SHORT // Duration of the message, another possible value is Toast.LENGTH_LONG
                    ).show(); //Finally Show the toast
                }

            }
        });

        //Contrast Filter
        filter_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBitmap();
                if (previewImage != null) {
                    System.out.println("Starting contrast flip");
                    operation = Bitmap.createBitmap(previewImage.getWidth(), previewImage.getHeight(), previewImage.getConfig());
                    int width = previewImage.getWidth();
                    int height = previewImage.getHeight();
                    // calculate contrast and apply to each pixel using formula to up contrast between colors
                    double contrast = Math.pow((100.0 + 50) / 100.0, 2);
                    for (int i = 0; i < previewImage.getWidth(); i++) {
                        for (int j = 0; j < previewImage.getHeight(); j++) {
                            int p = previewImage.getPixel(i, j);
                            int oldR = Color.red(p);
                            int oldG = Color.green(p);
                            int oldB = Color.blue(p);
                            int alpha = Color.alpha(p);
                            int newR = (int)(((((oldR / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                            int newG = (int)(((((oldG / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                            int newB = (int)(((((oldB / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                            // check to make sure no pixel color goes under or over the limit
                            if(newR > 255){
                                newR = 255;
                            }
                            else if (newR < 0){
                                newR = 0;
                            }
                            if(newG > 255){
                                newG = 255;
                            }
                            else if (newG < 0){
                                newG = 0;
                            }
                            if(newB > 255){
                                newB = 255;
                            }
                            else if (newB < 0){
                                newB = 0;
                            }
                            operation.setPixel(i, j, Color.argb(alpha, newR, newG, newB));

                        }

                    }
                    imageView.setImageBitmap(operation);
                    System.out.println("Done!");
                }else {
                    Toast.makeText(getApplicationContext(), //Context
                            "Please load image first ", // Message to display
                            Toast.LENGTH_SHORT // Duration of the message, another possible value is Toast.LENGTH_LONG
                    ).show(); //Finally Show the toast
                }
            }
        });

        // Grayscale
        filter_8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBitmap();
                if (previewImage != null) {
                    System.out.println("Starting Filter 1");
                    operation = Bitmap.createBitmap(previewImage.getWidth(), previewImage.getHeight(), previewImage.getConfig());

                    for (int i = 0; i < previewImage.getWidth(); i++) {
                        for (int j = 0; j < previewImage.getHeight(); j++) {
                            int p = previewImage.getPixel(i, j);
                            int oldR = Color.red(p);
                            int oldG = Color.green(p);
                            int oldB = Color.blue(p);
                            int alpha = Color.alpha(p);

                            oldR = oldG = oldB = (int)(0.299 * oldR + 0.587 * oldG + 0.114 * oldB); //average of old pixel colors

                            operation.setPixel(i, j, Color.argb(alpha, oldR, oldG, oldB));
                        }
                    }
                    imageView.setImageBitmap(operation);
                    System.out.println("Done!");
                }
                else {
                    Toast.makeText(getApplicationContext(), //Context
                            "Please load image first ", // Message to display
                            Toast.LENGTH_SHORT // Duration of the message, another possible value is Toast.LENGTH_LONG
                    ).show(); //Finally Show the toast
                }
            }
        });

        // SMOOTHING
        filter_9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                getBitmap();
                if (previewImage != null)
                {
                    //System.out.println("Starting Filter 1");
                    operation = Bitmap.createBitmap(previewImage.getWidth(), previewImage.getHeight(), previewImage.getConfig());

                    double[][] gaussianBlurMatrix = new double[][] {
                            { 1, 2, 1 },
                            { 2, 4, 2 },
                            { 1, 2, 1 }
                    };
                    int factor = 16;

                    for (int x = 0; x < previewImage.getWidth() - 2; x++) {
                        for (int y = 0; y < previewImage.getHeight() - 2; y++) {
                            int a, r, g, b;
                            int sumR, sumG, sumB;
                            int[][] pixels = new int[3][3];

                            sumR = sumG = sumB = 0;

                            // get pixel matrix
                            for (int i = 0; i < 3; i++) {
                                for (int j = 0; j < 3; j++) {
                                    pixels[i][j] = previewImage.getPixel(x + i, y + j);
                                    sumR += (Color.red(pixels[i][j]) * gaussianBlurMatrix[i][j]);
                                    sumG += (Color.green(pixels[i][j]) * gaussianBlurMatrix[i][j]);
                                    sumB += (Color.blue(pixels[i][j]) * gaussianBlurMatrix[i][j]);
                                }
                            }

                            // calculate pixel argb values
                            a = Color.alpha(pixels[1][1]);
                            r = (int) (sumR / factor);
                            g = (int) (sumG / factor);
                            b = (int) (sumB / factor);

                            // keep values within bounds
                            if (r < 0) {
                                r = 0;
                            }
                            else if (r > 255) {
                                r = 255;
                            }
                            if (g < 0) {
                                g = 0;
                            }
                            else if (g > 255) {
                                g = 255;
                            }
                            if (b < 0) {
                                b = 0;
                            }
                            else if (b > 255) {
                                b = 255;
                            }

                            // set new pixel
                            operation.setPixel(x + 1, y + 1, Color.argb(a, r, g, b));
                        }
                    }
                    imageView.setImageBitmap(operation);
                    System.out.println("Done!");
                } else {
                    Toast.makeText(getApplicationContext(), //Context
                            "Please load image first ", // Message to display
                            Toast.LENGTH_SHORT // Duration of the message, another possible value is Toast.LENGTH_LONG
                    ).show(); //Finally Show the toast
                }
            }
        });
    }
}
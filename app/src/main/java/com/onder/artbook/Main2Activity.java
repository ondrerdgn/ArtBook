package com.onder.artbook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLData;

public class Main2Activity extends AppCompatActivity {
    ImageView imageView;
    EditText editText;
    Button button;
    static SQLiteDatabase database;
    Bitmap selectedImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        imageView = findViewById(R.id.imageView);
        editText = findViewById(R.id.editText);
        button = findViewById(R.id.button);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if (info.equalsIgnoreCase("new")){

            Bitmap background = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.background);
            imageView.setImageBitmap(background);
            button.setVisibility(View.VISIBLE);
            editText.setText("");

        }else{

            String name = intent.getStringExtra("name");
            editText.setText(name);
            int position = intent.getIntExtra("position", 0);
            imageView.setImageBitmap(MainActivity.artImage.get(position));
            button.setVisibility(View.INVISIBLE);
        }
    }

    public void select(View view){

        // intenti başlatmadan önce kullanıcının izni var mı yok mu kontrol etmemiz lazım.

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 2);

        }else{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,1);   //  neden For Result?, bu intent bir sonuç için yapılıyor.yani media ya gidip bir resim alıcam mesela.

        }

    }

    @Override   // Kullanıcının iznini alıyoruz(yani kullanıcı bize ilk izni verdiğinde ne olacak onu yazıyoruz.)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 2){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,1);   // yukarıdan alıp yapıştırdık.
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override   //  For result u bu şekilde alıyoruz.
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 1 && resultCode == RESULT_OK && data != null);

        Uri image = data.getData();

        try {
            selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image);
            imageView.setImageBitmap(selectedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    public void save(View view){

        String artName = editText.getText().toString();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        selectedImage.compress(Bitmap.CompressFormat.PNG,50, outputStream);
        byte[] byteArray = outputStream.toByteArray();

        Toast.makeText(this, "selected image saved", Toast.LENGTH_SHORT).show();

        try{
            database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null);
            database.execSQL("CREATE TABLE IF NOT EXISTS arts (name VARCHAR, image BLOB)");

            String sqlString = "INSERT INTO arts (name, image)  VALUES (?, ?)";
            SQLiteStatement statement = database.compileStatement(sqlString);
            statement.bindString(1, artName);
            statement.bindBlob(2, byteArray);
            statement.execute();




        }catch (Exception e){
            e.printStackTrace();
        }
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }



}

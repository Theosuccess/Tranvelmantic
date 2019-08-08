package com.theo.travelmantics;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class DealActivity extends AppCompatActivity {
    private static final int PICTURE_RESULT = 123;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private EditText txtDeals;
    private EditText txtDealPrice;
    private EditText txtDescription;
    private TravelDeal travelDeal;
    private String downloadUrl;
    private ImageView imageView;
    private Button btnUpload;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);

        firebaseDatabase=FirebaseUtil.firebaseDatabase;
        databaseReference=FirebaseUtil.databaseReference;

        txtDeals = findViewById(R.id.edtDeals);
        txtDealPrice = findViewById(R.id.edtDealPrice);
        txtDescription = findViewById(R.id.edtDescription);
        btnUpload = findViewById(R.id.btnUploadImage);
        imageView = findViewById(R.id.imgDeal);
        Intent intent = getIntent();
        travelDeal = (TravelDeal)intent.getSerializableExtra("Deal");
        if (travelDeal ==null){
            travelDeal =new TravelDeal();
        }
        this.travelDeal= travelDeal;
        txtDeals.setText(travelDeal.getTitle());
        txtDescription.setText(travelDeal.getDescription());
        txtDealPrice.setText(travelDeal.getPrice());
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent, "Insert Picture"), PICTURE_RESULT);
            }
        });
        showImage(travelDeal.getImageUrl());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            StorageReference reference = FirebaseUtil.storageReference.child(imageUri.getLastPathSegment());
            reference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getBaseContext(), "Image Uploaded", Toast.LENGTH_LONG).show();
                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful()) ;
                    String imageUrl = urlTask.getResult().toString();
                    String pictureName = taskSnapshot.getStorage().getPath();
                    travelDeal.setImageUrl(imageUrl);
                    travelDeal.setImageName(pictureName);
                    showImage(imageUrl);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        if (FirebaseUtil.isAdmin) {
            menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.save_menu).setVisible(true);
            enableEditTexts(true);
            btnUpload.setEnabled(true);
        } else {
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.save_menu).setVisible(false);
            enableEditTexts(false);
            btnUpload.setEnabled(false);
        }
        return true;
    }
    private void enableEditTexts(boolean isEnabled) {
        txtDeals.setEnabled(isEnabled);
        txtDescription.setEnabled(isEnabled);
        txtDealPrice.setEnabled(isEnabled);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_menu:
                saveDeal();
                clean();
                backToList();
                return true;
            case R.id.delete_menu:
                deleteDeal();
                Toast.makeText(this, "Deal Deleted", Toast.LENGTH_SHORT).show();
                backToList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveDeal() {
        travelDeal.setTitle( txtDeals.getText().toString());
        travelDeal.setDescription(txtDescription.getText().toString());
        travelDeal.setPrice(txtDealPrice.getText().toString());
        if(travelDeal.getId()==null){
            databaseReference.push().setValue(travelDeal);
        }
        else{
            databaseReference.child(travelDeal.getId()).setValue(travelDeal);
        }
    }
    private void deleteDeal() {
        if(travelDeal==null){
            Toast.makeText(this, "Please save the deal before deleting", Toast.LENGTH_SHORT).show();
            return;
        }
        databaseReference.child(travelDeal.getId()).removeValue();
    }

    private void backToList(){
        startActivity(new Intent(this, TravelActivity.class));
    }

    private void clean() {
        txtDeals.setText("");
        txtDealPrice.setText("");
        txtDescription.setText("");
        txtDeals.requestFocus();
    }

    private void showImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.with(this)
                    .load(imageUrl)
                    .resize(width, width * 2 / 3)
                    .centerCrop()
                    .into(imageView);


        }
    }
}

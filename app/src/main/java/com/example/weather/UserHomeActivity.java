package com.example.weather;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weather.databinding.ActivityUserHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

public class UserHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {




    private AppBarConfiguration mAppBarConfiguration;
    TextView usernameTv, useremailTv;
    CircleImageView uploadImage, userProfile;
    private Uri imageUrl = null;
    private static final int PICK_IMAGE =1000 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Paper.init(this);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,R.id.nav_Flood,R.id.nav_gallery,R.id.nav_slideshow,R.id.nav_setting)
                .setDrawerLayout(drawer)
                .build();


        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_user_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        usernameTv = headerView.findViewById(R.id.userNameNav);
        useremailTv = headerView.findViewById(R.id.userEmailNav);
        userProfile = headerView.findViewById(R.id.userProfile);

        if(Utils.currentUser != null){
            Glide.with(getApplicationContext()).load(Utils.currentUser.getImageurl()).placeholder(R.drawable.avatar).into(userProfile);
            usernameTv.setText(Utils.currentUser.getName());
            useremailTv.setText(Utils.currentUser.getEmail());
        }


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_user_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }



    private ProgressDialog progressDialog;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Bitmap bitmap = null;
                Uri resultUri = result.getUri();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver() , Uri.parse(resultUri.toString()));
                    uploadImage.setImageBitmap(bitmap);
                    uploadFile(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }


        /*if (requestCode == PICK_IMAGE) {
            imageUrl = data.getData();



                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver() , Uri.parse(imageUrl.toString()));
                    uploadImage.setImageBitmap(bitmap);
                    uploadFile(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }*/

        // Glide.with(this).load(imageUrl).into(uploadImage);
    }




    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.nav_Rain){

            Intent intent = new Intent(getApplicationContext(),RainActivity.class);
            startActivity(intent);
        }

        if (item.getItemId() == R.id.nav_Flood){


            AlertDialog.Builder builder =
                    new AlertDialog.Builder(UserHomeActivity.this);
            View view = LayoutInflater.from(UserHomeActivity.this).inflate(R.layout.dialogue_box, (LinearLayout)
                    findViewById(R.id.layoutDialogContainer)
            );
            builder.setView(view);
            final AlertDialog alertDialog = builder.create();

            alertDialog.setCancelable(true);
            EditText lvl = view.findViewById(R.id.lvlMeasurement);
            Button enterLvl = view.findViewById(R.id.enterBtn);
            enterLvl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String level = "1500";
                    if (TextUtils.isEmpty(lvl.toString())){
                        lvl.setError("Enter Level...");
                    }

                    if (lvl.getText().toString().equalsIgnoreCase(level)){
                        Toast.makeText(UserHomeActivity.this, "Flood is predicted...", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(UserHomeActivity.this, "No Flood is predicted...", Toast.LENGTH_SHORT).show();
                    }
                }
            });




            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

            alertDialog.show();

        }


        if(item.getItemId() == R.id.nav_update){
            //custom alert dialog

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.update_user_info, null);
            builder.setView(dialogView);
            builder.setTitle("Update Profile");
            builder.setMessage("Please Type New Name here");

            EditText nameEt = dialogView.findViewById(R.id.updateEt);
            uploadImage = (CircleImageView) dialogView.findViewById(R.id.uploadImage);

            uploadImage.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View v) {
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(UserHomeActivity.this);
                }
            });

            builder.setPositiveButton("update", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(!TextUtils.isEmpty(nameEt.getText().toString())){
                        String newName = nameEt.getText().toString();

                        Map<String, Object> update = new HashMap<>();
                        update.put("name", newName);

                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(Utils.currentUser.getUid())
                                .updateChildren(update);
                        Utils.currentUser.setName(newName);
                        usernameTv.setText(Utils.currentUser.getName());
                        Paper.book().write("user_info", Utils.currentUser);
                        Toast.makeText(UserHomeActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }

                    if(imageUrl != null){

                        Utils.currentUser.setImageurl(imageUrl.toString());
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(Utils.currentUser.getUid())
                                .setValue(Utils.currentUser);

                        Glide.with(getApplicationContext()).load(imageUrl).into(userProfile);
                        Paper.book().write("user_info", Utils.currentUser);
                        Toast.makeText(UserHomeActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.show();
        }

        if(item.getItemId() == R.id.nav_logout){
            Paper.init(this);
            Paper.book().delete("user_info");
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }
        return false;
    }

    public Bitmap loadBitmap(String url)
    {
        Bitmap bm = null;
        InputStream is = null;
        BufferedInputStream bis = null;
        try
        {
            URLConnection conn = new URL(url).openConnection();
            conn.connect();
            is = conn.getInputStream();
            bis = new BufferedInputStream(is, 8192);
            bm = BitmapFactory.decodeStream(bis);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if (bis != null)
            {
                try
                {
                    bis.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return bm;
    }


    private void uploadFile(Bitmap bitmap) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait ...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profile_images/" + Utils.currentUser.getUid() + ".jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte[] data = baos.toByteArray();

        final UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return storageRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            imageUrl = task.getResult();
                            progressDialog.dismiss();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(UserHomeActivity.this, "failed", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                progressDialog.setMessage("Uploading " + progress + "%");
            }
        });

    }
}
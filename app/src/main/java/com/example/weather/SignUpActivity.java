package com.example.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends AppCompatActivity {
    private EditText NameEt,emailEt, passwordEt;
    private FirebaseAuth mAuth;

    Button CreateUser;
    private Uri imageUrl = null;
    CircleImageView uploadImage;
    private ProgressDialog progressDialog;
    Bitmap bitmap = null;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver() , Uri.parse(resultUri.toString()));
                    uploadImage.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().setTitle("Sign Up");

            NameEt = findViewById(R.id.userNameET);
            emailEt = findViewById(R.id.signUpEmailEt);
            passwordEt = findViewById(R.id.signUpPassEt);
            CreateUser = findViewById(R.id.createUserBtn);

        mAuth = FirebaseAuth.getInstance();

        CreateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
            }
        });

        uploadImage = findViewById(R.id.uploadImage);

        uploadImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SignUpActivity.this);
            }
        });


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


    private void uploadFile(Bitmap bitmap, String name, String email, String UserId) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait ...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final String random = UUID.randomUUID().toString();

        if (bitmap == null){
            Log.d("Bitmap", "uploadFile: bitmap null");
            HashMap map = new HashMap();
            map.put("name",name);
            map.put("email",email);
            map.put("imageUrl","https://firebasestorage.googleapis.com/v0/b/weathernew-7699c.appspot.com/o/profile_images%2F5ff035a8-1dfa-44b2-b027-8f5f0e921e6c.jpg?alt=media&token=1e27df86-5475-4356-ac55-7a9245490a22");
            map.put("uid",UserId);
            FirebaseDatabase.getInstance().getReference("Users").child(UserId).setValue(map);
            Toast.makeText(this, "User Created...", Toast.LENGTH_SHORT).show();
        }else{
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profile_images/" + random + ".jpg");
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
                                if (imageUrl!=null){
                                    User user = new User(name,email,UserId,imageUrl.toString());
                                    FirebaseDatabase.getInstance().getReference("Users")
                                            .child(UserId)
                                            .setValue(user);
                                    Toast.makeText(SignUpActivity.this, "User Created", Toast.LENGTH_SHORT).show();
                                    NameEt.setText(null);
                                    emailEt.setText(null);
                                    passwordEt.setText(null);
                                    Glide.with(getApplicationContext()).load(R.drawable.avatar).into(uploadImage);
                                    progressDialog.dismiss();
                                }else{
                                    String emptyUrl = "https://firebasestorage.googleapis.com/v0/b/weatherforecast-e2eed.appspot.com/o/profile_images%2Fpp.jpg?alt=media&token=b67c590c-24ac-42c8-8f58-2830aeeca31f";
                                    User user = new User(name,email,UserId,emptyUrl);
                                    FirebaseDatabase.getInstance().getReference("Users")
                                            .child(UserId)
                                            .setValue(user);
                                    Toast.makeText(SignUpActivity.this, "User Created", Toast.LENGTH_SHORT).show();
                                    NameEt.setText(null);
                                    emailEt.setText(null);
                                    passwordEt.setText(null);
                                    Glide.with(getApplicationContext()).load(R.drawable.avatar).into(uploadImage);
                                    progressDialog.dismiss();
                                }

                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(SignUpActivity.this, "failed"+e, Toast.LENGTH_SHORT).show();
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



    private void createAccount() {

        String Name = NameEt.getText().toString();
        String email = emailEt.getText().toString();
        String password = passwordEt.getText().toString();

        if (TextUtils.isEmpty(Name)) {
            NameEt.setError("Enter Name!");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailEt.setError("Enter Email!");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEt.setError("Enter Password!");
            return;
        }


        //user will be created with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            final FirebaseUser user = mAuth.getCurrentUser();
                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        String UserId = user.getUid();
                                        //saving details for user info
                                        uploadFile(bitmap, Name, email, UserId);
                                        Toast.makeText(SignUpActivity.this, "Verification Email send to "+user.getEmail(), Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                        startActivity(intent);
                                    }else {
                                        Toast.makeText(getApplicationContext(), "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignUpActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void gotoLogIn(View view) {
        finish();
    }
}
package site.josh.alc40phase.sec;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.josh.alc40phase.sec.R;

import site.josh.alc40phase.sec.model.Deal;

public class AdminActivity extends AppCompatActivity {

    public final static int RC_GALLERY = 889;

    public final static String TAG = AdminActivity.class.getSimpleName();

    private ImageView preview;
    private EditText resortName, amount, description;
    private FirebaseStorage fbStorage;
    private Uri selectedImageUri;
    private Toast validationToast;

    AlertDialog busyDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preview = findViewById(R.id.preview);
        resortName = findViewById(R.id.edit_name);
        amount = findViewById(R.id.edit_price);
        description = findViewById(R.id.edit_description);
        this.fbStorage = FirebaseStorage.getInstance();
        this.validationToast = Toast.makeText(this, "All Input fields are required", Toast.LENGTH_LONG);

        busyDialog = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog)
                .setView(R.layout.layout_progress)
                .create();

        busyDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            doSignOut();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.item_save_option).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                AdminActivity.this.onSaveOptionClicked();
                return true;
            }
        });
        menu.findItem(R.id.item_signout_option).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                AdminActivity.this.onSignOutOptionClicked();
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    public void onSaveOptionClicked() {
        Log.d(TAG, "onSaveOptionClicked");
//        validationToast.cancel();
        if (validateInputs()) {
            // TODO: 2019-08-05 upload resort record
            try {
                busyDialog.show();
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                final String resortName = this.resortName.getText().toString();
                final Bitmap bitmap;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), this.selectedImageUri);
                } else {
                    bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.getContentResolver(), this.selectedImageUri));
                }

                final StorageReference ref = fbStorage.getReference();
                final StorageReference imageRef = ref.child("resorts/"+this.selectedImageUri.getLastPathSegment());
                final UploadTask uploadTask = imageRef.putFile(this.selectedImageUri);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AdminActivity.this, "Error uploading image", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Error uploading your image");
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "Hurray! You uploaded your image");
                    }
                });

                final Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return imageRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            final Uri downloadUri = task.getResult();
                            // FIXME: 2019-08-05 Create a function to do this
                            final Deal deal = new Deal(resortName
                                    , amount.getText().toString()
                                    , description.getText().toString()
                                    , downloadUri.toString()
                                    , user.getUid());
                            final DatabaseReference dref = FirebaseDatabase.getInstance().getReference();
                            dref.child(Deal.COLLECTION).child(deal.getUuid()).setValue(deal)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(AdminActivity.this, "Hurray! Deal uploaded", Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(AdminActivity.this, "Error uploading Deal", Toast.LENGTH_LONG).show();
                                        Log.w(TAG, "Error saving record", e);
                                    }
                                });
                            // Done
                            AdminActivity.this.finish();
                        } else {
                            Toast.makeText(AdminActivity.this, "Couldn't get image url", Toast.LENGTH_LONG).show();
                        }
                    }
                });




            } catch (Exception e) {
                busyDialog.dismiss();
            }
        } else {
            validationToast.show();
        }
    }

    @Override
    protected void onStop() {
        busyDialog.dismiss();
        super.onStop();
    }

    public void onSignOutOptionClicked() {
        Log.d(TAG, "onSaveOptionClicked");
        doSignOut();
    }

    public void onSelectImageButtonClicked(@NonNull final View view) {
        final Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        startActivityForResult(intent,RC_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GALLERY) {
            if (resultCode == RESULT_OK) {
                this.selectedImageUri = data.getData();
                preview.setImageURI(this.selectedImageUri);
                // TODO: 2019-08-04 Remember we're going to save to cloud

            }
        }
    }

    private void updateImage(@NonNull final Uri imageUri) {

    }

    private void doSignOut() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, AuthActivity.class));
        finish();
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(resortName.getText().toString())
            || TextUtils.isEmpty(amount.getText().toString())
            || TextUtils.isEmpty(description.getText())) {
            return false;
        } else {
            return true;
        }
    }
}

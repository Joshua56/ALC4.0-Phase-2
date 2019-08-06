package site.josh.alc40phase.sec;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.josh.alc40phase.sec.R;

import java.util.Arrays;
import java.util.List;


public class AuthActivity extends AppCompatActivity {

    public static int REQ_CODE_SIGNIN = 888;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkCurrentUser();
    }

    public void checkCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // TODO: 2019-08-04 Goto AdminActivity
            navigateToMainActivity(user);
        } else {
            createSignInIntent();
        }
    }

    public void createSignInIntent() {
        final List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        startActivityForResult(AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(providers)
        .build(), REQ_CODE_SIGNIN);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SIGNIN) {
//            final IdpResponse idpResponse = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                navigateToMainActivity(user);
            } else {
                // TODO: 2019-08-04 Handle sign in error (or cancellation)
                finish();
            }
        }
    }

    private void navigateToMainActivity(@NonNull final FirebaseUser user) {
        final Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}

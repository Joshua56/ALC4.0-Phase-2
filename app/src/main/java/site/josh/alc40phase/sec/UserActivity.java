package site.josh.alc40phase.sec;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.josh.alc40phase.sec.R;

import site.josh.alc40phase.sec.adapter.DealsRecyclerAdapter;
import site.josh.alc40phase.sec.model.Deal;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity {

    public static String TAG = UserActivity.class.getSimpleName();

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChange key:"+dataSnapshot.getKey());
            final List<Deal> deals = new ArrayList<>();
            final Iterable<DataSnapshot> it = dataSnapshot.getChildren();
            for (DataSnapshot child : it) {
                deals.add(child.getValue(Deal.class));
            }
            UserActivity.this.adapter.apply(deals);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.d(TAG, "onCancelled err:"+databaseError.getMessage());
        }
    };

    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Log.d(TAG, "onChildAdded key:"+dataSnapshot.getKey());
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Log.d(TAG, "onChildChanged key:"+dataSnapshot.getKey());
            final List<Deal> deals = new ArrayList<>();
            final Iterable<DataSnapshot> it = dataSnapshot.getChildren();
            for (DataSnapshot child : it) {
                deals.add(child.getValue(Deal.class));
            }
            UserActivity.this.adapter.apply(deals);
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            Log.d(TAG, "onChildRemoved key:"+dataSnapshot.getKey());
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Log.d(TAG, "onChildMoved key:"+dataSnapshot.getKey());
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.d(TAG, "onCancelled err:"+databaseError.getMessage());
        }
    };

    RecyclerView recyclerView;
    DatabaseReference fbDatabaseRef;
    DealsRecyclerAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        recyclerView = findViewById(R.id.recycler_deals);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        this.adapter = new DealsRecyclerAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);

        fbDatabaseRef = FirebaseDatabase.getInstance().getReference();

        fbDatabaseRef.child(Deal.COLLECTION).addValueEventListener(valueEventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fbDatabaseRef.addChildEventListener(this.childEventListener);
    }

    @Override
    protected void onPause() {
        fbDatabaseRef.removeEventListener(this.childEventListener);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        fbDatabaseRef.removeEventListener(valueEventListener);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finishAndRemoveTask();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user, menu);
        final MenuItem item = menu.findItem(R.id.item_add);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                onAddMenuItemOptionClicked();
                return true;
            }
        });
        menu.findItem(R.id.item_signout).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                UserActivity.this.onSignOutOptionClicked();
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void onAddMenuItemOptionClicked() {
        startActivity(new Intent(this, AdminActivity.class));
    }

    public void onSignOutOptionClicked() {
        Log.d(TAG, "onSaveOptionClicked");
        doSignOut();
    }

    private void doSignOut() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, AuthActivity.class));
        finish();
    }


}

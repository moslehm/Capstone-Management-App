package ca.macewan.capstone.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

import ca.macewan.capstone.ListFragment;


public class ViewPagerAdapter extends FragmentStateAdapter implements ListFragment.OnListListener {
    private final String email;


    public ViewPagerAdapter(@NonNull Fragment fragment, String email) {
        super(fragment);
        this.email = email;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        ListFragment listFragment = new ListFragment();
        listFragment.setListener(this);
        Bundle bundle = new Bundle();
        bundle.putString("email", email);
        bundle.putBoolean("isSupervisor", true);
        if (position == 1) {
            bundle.putString("emptyListText", "No invites received");
            bundle.putString("screenType", "invited");
            listFragment.setArguments(bundle);
            return listFragment;
        }
        bundle.putString("emptyListText", "There are no existing projects.\nCreate one with the plus sign above");
        bundle.putString("screenType", "all");
        listFragment.setArguments(bundle);
        return listFragment;
    }


    @Override
    public int getItemCount() {
        return 2;
    }

    @Override
    public void onListUpdate(String fragmentName, ListFragment.OnUpdateListener onUpdateListener) {
        ArrayList<String> projectIds = new ArrayList<String>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (Objects.equals(fragmentName, "all")) {
            db.collection("Projects")
                    .whereEqualTo("isComplete", false)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    projectIds.add(document.getId());
                                }
                                onUpdateListener.onUpdateComplete(projectIds);
                            }
                        }
                    });
        } else if (Objects.equals(fragmentName, "invited")) {
            db.collection("Users")
                    .document(email)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                ArrayList<String> projectIds = (ArrayList<String>) task.getResult().get("invited");
                                if (projectIds == null) {
                                    onUpdateListener.onUpdateComplete(new ArrayList<String>());
                                } else {
                                    onUpdateListener.onUpdateComplete(projectIds);
                                }
                            }
                        }
                    });
        }
    }
}

package volosyuk.easybizcard.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import volosyuk.easybizcard.R;
import volosyuk.easybizcard.UserDetailActivity;
import volosyuk.easybizcard.adapters.UsersAdapter;
import volosyuk.easybizcard.models.User;

public class UsersFragment extends Fragment {


    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private FirebaseFirestore db;
    private List<User> userList = new ArrayList<>();

    public UsersFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        adapter = new UsersAdapter(userList, user -> openUserDetails(user));
        recyclerView.setAdapter(adapter);

        loadUsers();
        return view;
    }

    private void loadUsers() {
        db.collection("users").get().addOnSuccessListener(querySnapshot -> {
            userList.clear();
            for (DocumentSnapshot document : querySnapshot) {
                String userId = document.getString("userId");
                String email = document.getString("email");
                List<String> bookmarkedCards = (List<String>) document.get("bookmarkedCards");
                boolean admin = Boolean.TRUE.equals(document.getBoolean("admin"));

                userList.add(new User(userId, email, bookmarkedCards, admin));
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> Log.e("Firestore", "Ошибка загрузки пользователей", e));
    }

    private void openUserDetails(User user) {
        Intent intent = new Intent(getContext(), UserDetailActivity.class);
        intent.putExtra("userId", user.userId);
        startActivity(intent);
    }
}
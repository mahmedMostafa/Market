package com.example.market.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.market.Activities.DetailsActivity;
import com.example.market.Activities.LoginActivity;
import com.example.market.Adapters.FavouritesAdapter;
import com.example.market.Models.FavouriteItem;
import com.example.market.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.market.Activities.MainActivity.KEY_GRID_ID;
import static com.example.market.Activities.MainActivity.KEY_LABEL_ID;

public class FavoritesFragment extends Fragment {


    private LinearLayout loginLayout;
    private Button loginButton;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener listener;
    private RecyclerView recyclerView;
    private FavouritesAdapter adapter;
    private List<FavouriteItem> favouriteItems;
    private LinearLayout noLikesLayout;
    private FirebaseFirestore firebaseFirestore;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        // Inflate the layout for this fragment
        //Don't forget to write the code here before the return statement
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        loginLayout = view.findViewById(R.id.login_layout);
        loginButton = view.findViewById(R.id.login_button);
        noLikesLayout = view.findViewById(R.id.no_likes_layout);
        recyclerView = view.findViewById(R.id.favourites_recycler_view);
        progressBar = view.findViewById(R.id.favourites_progress_bar);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        favouriteItems = new ArrayList<>();
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        });
        checkUser();
        getAllFavourites();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(listener);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        getFragmentManager().beginTransaction().detach(this).attach(this).commit();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuth != null) {
            mAuth.removeAuthStateListener(listener);
        }
    }

    private void checkUser() {
        listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    loginLayout.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    //noLikesLayout.setVisibility(View.VISIBLE);
                }
            }
        };
    }

    private void getAllFavourites() {
        favouriteItems.clear();
        if (mAuth.getCurrentUser() != null) {
            noLikesLayout.setVisibility(View.GONE);
            firebaseFirestore.collection("users/").document(mAuth.getCurrentUser().getUid())
                    .collection("likes/").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        for (DocumentSnapshot document : documents) {
                            FavouriteItem item = new FavouriteItem();

                            item.setItem(document.getId());
                            item.setBrand(document.get("brand").toString());
                            item.setCategory(document.get("category").toString());
                            item.setImageUrl(document.get("imageUrl").toString());
                            item.setPrice(document.get("price").toString());
                            item.setOldPrice(document.get("oldPrice").toString());
                            item.setTitle(document.get("title").toString());
                            item.setDiscount(document.get("discount").toString());

                            favouriteItems.add(item);
                        }
                        Collections.reverse(favouriteItems);
                        adapter = new FavouritesAdapter(getActivity(), favouriteItems);
                        recyclerView.setAdapter(adapter);
                        adapter.setOnCloseButtonClickListener(new FavouritesAdapter.onCloseButtonClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                adapter.notifyItemRemoved(position);
                                firebaseFirestore.collection("users/").document(mAuth.getCurrentUser().getUid())
                                        .collection("likes/").document(favouriteItems.get(position).getItem())
                                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getActivity(), "item deleted ", Toast.LENGTH_LONG).show();
                                    }
                                });
                                favouriteItems.remove(favouriteItems.get(position));
                                if (favouriteItems.size() == 0) {
                                    noLikesLayout.setVisibility(View.VISIBLE);
                                } else {
                                    noLikesLayout.setVisibility(View.GONE);
                                }
                            }
                        });

                        adapter.setOnItemClickListener(new FavouritesAdapter.onItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                                intent.putExtra(KEY_LABEL_ID,favouriteItems.get(position).getCategory());
                                intent.putExtra(KEY_GRID_ID,favouriteItems.get(position).getItem());
                                startActivity(intent);
                            }
                        });
                        if (favouriteItems.size() == 0) {
                            noLikesLayout.setVisibility(View.VISIBLE);
                        } else {
                            noLikesLayout.setVisibility(View.GONE);
                        }
                        Log.e("size favouriteItems :", String.valueOf(favouriteItems.size()));
                    } else {
                        Toast.makeText(getActivity(), "task isn't successful", Toast.LENGTH_SHORT).show();
                        Log.e("", "task isn't successful");
                    }
                }
            });
        }
    }
}

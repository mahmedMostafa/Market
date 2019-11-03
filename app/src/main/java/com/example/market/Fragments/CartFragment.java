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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.market.Activities.DetailsActivity;
import com.example.market.Activities.LoginActivity;
import com.example.market.Activities.MainActivity;
import com.example.market.Adapters.CartAdapter;
import com.example.market.Models.CartItem;
import com.example.market.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.example.market.Activities.MainActivity.KEY_GRID_ID;
import static com.example.market.Activities.MainActivity.KEY_LABEL_ID;


public class CartFragment extends Fragment {

    private LinearLayout loginLayout;
    private LinearLayout noItemsLayout;
    private Button loginButton;
    private Button continueShoppingButton;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener listener;
    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private List<CartItem> cartItems;
    private String currentItemId;
    private int cartPosition;
    private RelativeLayout completeOrderLayout;
    private String currentPrice;
    private TextView totalPriceTextView;
    private int totalPrice =0;
    private ProgressBar progressBar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        // Inflate the layout for this fragment
        //Don't forget to write the code here before the return statement
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        loginLayout = view.findViewById(R.id.login_layout);
        progressBar = view.findViewById(R.id.cart_progress_bar);
        noItemsLayout = view.findViewById(R.id.no_items_layout);
        totalPriceTextView = view.findViewById(R.id.total_price_text_view);
        completeOrderLayout = view.findViewById(R.id.complere_order_layout);
        continueShoppingButton = view.findViewById(R.id.continue_shopping_button);
        final TabLayout tabLayout = (TabLayout) ((MainActivity) getActivity()).findViewById(R.id.sliding_tabs);
        loginButton = view.findViewById(R.id.cart_login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("click removeListener :", "i'm here");
                Toast.makeText(getActivity(), "Clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });
        recyclerView = view.findViewById(R.id.cart_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        cartItems = new ArrayList<>();
        continueShoppingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //just switch to the first home tab
                tabLayout.getTabAt(0).select();
            }
        });
        checkUser();
        getCartItem();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(listener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuth != null) {
            mAuth.addAuthStateListener(listener);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        reloadFragment();
    }

    private void checkUser() {
        listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    loginLayout.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    //noItemsLayout.setVisibility(View.VISIBLE);
                }
            }
        };
    }


    private void getCartItem() {
        if (mAuth.getCurrentUser() != null) {
            //setting total price to 0 as i don't knwo whay it gets a high value from no where :D
            totalPrice =0;
            Log.e("total price :" , String.valueOf(totalPrice));
            //noItemsLayout.setVisibility(View.GONE);
            firebaseFirestore.collection("users/").document(mAuth.getCurrentUser().getUid())
                    .collection("cart/").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);
                        List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                        for (DocumentSnapshot document : documentSnapshots) {
                            /*CartItem cartItem = document.toObject(CartItem.class);
                            cartItems.add(cartItem);*/
                            CartItem cartItem = new CartItem();
                            cartItem.setId(document.getId());
                            cartItem.setCategory(document.get("category").toString());
                            cartItem.setFreeShipping(Boolean.parseBoolean(document.get("freeShipping").toString()));
                            cartItem.setImageUrl(document.get("imageUrl").toString());
                            cartItem.setPrice(document.get("price").toString());
                            cartItem.setOldPrice(document.get("oldPrice").toString());
                            cartItem.setTitle(document.get("title").toString());
                            cartItem.setBrand(document.get("brand").toString());
                            cartItem.setDiscount(document.get("discount").toString());
                            currentItemId = document.get("id").toString();
                            currentPrice = document.get("price").toString();
                            Log.e("current price :" , String.valueOf(getPrice(currentPrice)));
                            totalPrice+=getPrice(currentPrice);
                            Log.e("total price :" , String.valueOf(totalPrice));
                            cartItems.add(cartItem);
                        }
                        adapter = new CartAdapter(cartItems, getContext());
                        recyclerView.setAdapter(adapter);
                        adapter.setOnRemoveItemClickListener(new CartAdapter.OnRemoveItemClickListener() {
                            @Override
                            public void OnItemClick(int position) {
                                adapter.notifyItemRemoved(position);
                                firebaseFirestore.collection("users/").document(mAuth.getCurrentUser().getUid())
                                        .collection("cart/").document(cartItems.get(position).getId()).delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getActivity(), "Item Removed ", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                cartItems.remove(cartItems.get(position));
                                reloadFragment();
                                if (cartItems.size() == 0) {
                                    noItemsLayout.setVisibility(View.VISIBLE);
                                    completeOrderLayout.setVisibility(View.GONE);
                                } else {
                                    noItemsLayout.setVisibility(View.GONE);
                                    completeOrderLayout.setVisibility(View.VISIBLE);
                                }
                            }
                        });

                        adapter.setOnItemClickListener(new CartAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                                intent.putExtra(KEY_LABEL_ID,cartItems.get(position).getCategory());
                                intent.putExtra(KEY_GRID_ID,cartItems.get(position).getId());
                                startActivity(intent);
                            }
                        });

                        if (cartItems.size() == 0) {
                            noItemsLayout.setVisibility(View.VISIBLE);
                            completeOrderLayout.setVisibility(View.GONE);
                        } else {
                            completeOrderLayout.setVisibility(View.VISIBLE);
                            noItemsLayout.setVisibility(View.GONE);
                        }
                        totalPriceTextView.setText("EGP " + addComa(String.valueOf(totalPrice)));
                    }
                }
            });
            Log.e("cartItems size is :", String.valueOf(cartItems.size()));
            /*if(cartItems.size() ==0){
                noItemsLayout.setVisibility(View.VISIBLE);
            }*/
        }
    }

    private void reloadFragment(){
        getFragmentManager().beginTransaction().detach(this).attach(this).commit();
    }

    //this method add the coma to the price to make it look better
    private String addComa(String price){
        if(price.length() >=4){
            int length = price.length();
            int position = ((length+1)/2)-1;
            return price.substring(0,position) + ',' + price.substring(position);
        }else{
            return price;
        }
    }

    //as we stored the price as a string so we convert it into integer with splitting the EGP and , of course
    private int getPrice(String price){
        String [] parts = price.split(" ");
        String priceString = parts[1];
        String finalPrice ="";
        for(int i=0;i<priceString.length();i++){
            if(priceString.charAt(i) == ','){
                continue;
            }else{
                finalPrice+=priceString.charAt(i);
            }
        }
        return Integer.parseInt(finalPrice);
    }
}

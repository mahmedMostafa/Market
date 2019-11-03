package com.example.market.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.market.Activities.AllDealsActivity;
import com.example.market.Activities.DetailsActivity;
import com.example.market.Adapters.BestProductsAdapter;
import com.example.market.Adapters.DealsAdapter;
import com.example.market.Adapters.SlidingAdapter;
import com.example.market.Adapters.TopCategoriesAdapter;
import com.example.market.Models.BestProductsItem;
import com.example.market.Models.DealsItem;
import com.example.market.Models.GridItem;
import com.example.market.Models.LabelItem;
import com.example.market.Models.PicturesItem;
import com.example.market.Models.TopCategoryItem;
import com.example.market.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import static com.example.market.Activities.MainActivity.KEY_GRID_ID;
import static com.example.market.Activities.MainActivity.KEY_LABEL_ID;

public class HomeFragment extends Fragment {

    //private static final String KEY_RECYCLER_STATE ="recycler_state";
    //private static Bundle mBundleRecyclerViewState;
    private FirebaseFirestore fireStore;
    private StorageReference storageReference;
    private LinearLayout sliderDotSpanel;
    private static final int dotsCount = 5;
    //private ImageView[] dots;
    private RecyclerView bestProductsRecyclerView;
    private BestProductsAdapter bestProductsAdapter;
    private ArrayList<BestProductsItem> bestProductsItems;
    private RecyclerView dealsRecyclerView;
    private ArrayList<Object> dealsItems;
    private DealsAdapter dealsAdapter;
    private GridLayoutManager gridLayoutManager;
    private RecyclerView categoriesRecyclerView;
    private TopCategoriesAdapter categoriesAdapter;
    private ArrayList<TopCategoryItem> topCategoriesList;
    private ViewPager viewPager;
    private static int NUM_PAGES = 0;
    private int currentPosition = 1;
    private ArrayList<String> adsList = new ArrayList<>();
    private int lastPosition = 6;
    private Handler autoScrollHandler;
    private SlidingAdapter slidingAdapter;
    private ArrayList<String> offerPictures;
    private ArrayList<LabelItem> labelItems;
    private ArrayList<DealsItem> deals;
    private ArrayList<PicturesItem> picturesItems;
    //private ArrayList<DealsItem> dealsList;

    @SuppressLint("WrongConstant")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //Don't forget to write the code here before the return statement
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        storageReference = FirebaseStorage.getInstance().getReference();
        fireStore = FirebaseFirestore.getInstance();
        addingLabels();
        addingGridSales();
        addingPictureItems();
        //addingGridItems();
        //addingPictures();

        setImageSlider(view);
        initAutoSlider();
        settingUpTopCategoriesRecyclerView(view);
        Log.d("size of labels", String.valueOf(labelItems.size()));
        settingUpDealsRecyclerView(view);
        initializeBestProducts(view);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this method doesn't call onDestroy and keeps the activity context alive so that there won't be any null pointer exceptions for rotations
        //this is not the best practice as this may cause memory leaks but it works just fine at least for now
        setRetainInstance(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (autoScrollHandler != null) {
            autoScrollHandler.removeCallbacksAndMessages(null);
            viewPager.setCurrentItem(viewPager.getCurrentItem());
            initAutoSlider();
        }
    }

    //this method is to set up the top categories recycler view and the gridRecyclerAdapter together
    private void settingUpTopCategoriesRecyclerView(View view) {
        topCategoriesList = new ArrayList<>();
        gridLayoutManager = new GridLayoutManager(getActivity(), 4);
        //gridLayoutManager.setOrientation(LinearLayout.VERTICAL);
        categoriesRecyclerView = view.findViewById(R.id.top_categories_recycler_view);
        categoriesAdapter = new TopCategoriesAdapter(getActivity(), topCategoriesList);
        categoriesRecyclerView.setHasFixedSize(true);
        categoriesRecyclerView.setLayoutManager(gridLayoutManager);
        categoriesRecyclerView.setAdapter(categoriesAdapter);
        addingTopCategories();
        categoriesAdapter.setOnItemClickListener(new TopCategoriesAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int position) {
                Intent intent = new Intent(getActivity(),AllDealsActivity.class);
                intent.putExtra(KEY_LABEL_ID,topCategoriesList.get(position).getCategory());
                startActivity(intent);
            }
        });
    }

    private void settingUpDealsRecyclerView(View view) {
        dealsItems = new ArrayList<>();
        createDummyDataForTheDealsRecyclerView();
        dealsRecyclerView = view.findViewById(R.id.deals_items_recycler_view);
        dealsRecyclerView.setHasFixedSize(true);
        dealsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        dealsAdapter = new DealsAdapter(dealsItems, getActivity());
        dealsRecyclerView.setAdapter(dealsAdapter);
        dealsAdapter.setOnLabelClickListener(new DealsAdapter.OnLabelClickListener() {
            @Override
            public void OnItemClick(int position) {
                Intent intent = new Intent(getActivity(), AllDealsActivity.class);
                LabelItem labelItem = (LabelItem) dealsItems.get(position);
                intent.putExtra(KEY_LABEL_ID,labelItem.getId());
                startActivity(intent);
            }
        });
    }

    private void createDummyDataForTheDealsRecyclerView() {
        //int k = 0;
        for (int j = 1; j <= 6; j++) {

            LabelItem labelItem = labelItems.get(j - 1);
            dealsItems.add(labelItem);

            DealsItem item = deals.get(j - 1);
            dealsItems.add(item);
            /*final DealsItem item = new DealsItem();
            ArrayList<GridItem> gridItems = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                gridItems.add(new GridItem("", "", ""));
            }*/
            /*item.setItemsList(gridItems);
            //dealsList.add(item);
            dealsItems.add(item)*/
            ;
            //dealsAdapter.notifyDataSetChanged();
            //LabelItem labelItem = new LabelItem("s","s");
            //dealsItems.add(labelItem);

            PicturesItem picturesItem = picturesItems.get(j - 1);
            dealsItems.add(picturesItem);
            //dealsAdapter.notifyDataSetChanged();


            //dealsItems.add(new PicturesItem(offerPictures.get(0),offerPictures.get(1)));
            //dealsItems.add(new PicturesItem(first, second));
            //dealsAdapter.notifyDataSetChanged();

        }
        //addingGridSales();
        //Log.d("the total items is :" ,String.valueOf(dealsItems.size()));
    }

   /* private void addingGridItems() {
        for (int j = 0; j < 5; j++) {
            final ArrayList<GridItem> gridItems = new ArrayList<>();
            final CollectionReference mRef = fireStore.collection("grid_items/");
            final int s = j;
            mRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.d("", "Error is :" + e.getMessage());
                        Toast.makeText(getActivity(), "Error is :" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            switch (doc.getType()) {
                                case ADDED:
                                    doc.getDocument().getReference().collection("items/").addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                                            if (e != null) {
                                                Log.d("", "Error is :" + e.getMessage());
                                                Toast.makeText(getActivity(), "Error is :" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            } else {
                                                int k = 0;
                                                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                                    switch (doc.getType()) {
                                                        case ADDED:
                                                            final GridItem gridItem = new GridItem();
                                                            gridItem.setDealsTitle(doc.getDocument().get("title").toString());
                                                            gridItem.setDealsPrice(doc.getDocument().get("price").toString());
                                                            gridItem.setId(doc.getDocument().getId());
                                                            Log.d("", "the document id is :" + doc.getDocument().getId());
                                                            StorageReference mRef = storageReference.child("grid_sales/" + doc.getDocument().getId() + ".jpg");
                                                            mRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                @Override
                                                                public void onSuccess(Uri uri) {
                                                                    gridItem.setDealsImageUrl(uri.toString());
                                                                    //gridItems.add(gridItem);
                                                                    dealsAdapter.notifyDataSetChanged();
                                                                    Log.d("", "i have loaded grid picture");
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(getActivity(), "Something went wrong with grid item picture!!", Toast.LENGTH_SHORT).show();
                                                                    Log.d("", "Error with grid item picture is : " + e.getMessage());
                                                                }
                                                            });
                                                            break;
                                                        case MODIFIED:
                                                            //here we will show the notification bar to tell the user to refresh the data
                                                            break;
                                                        case REMOVED:
                                                            //here is to handle the removal of item in case of a change
                                                            break;
                                                    }
                                                }
                                                dealsAdapter.updateGridList(gridItems);
                                                dealsItems.set(s, gridItems);
                                                dealsAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
                                    break;
                                case MODIFIED:
                                    //here we will show the notification bar to tell the user to refresh the data
                                    break;
                                case REMOVED:
                                    //here is to handle the removal of item in case of a change
                                    break;
                            }
                        }
                    }
                }
            });
        }
    }*/

    private void addingPictureItems() {
        int k = 2;
        int s = 1;
        picturesItems = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            picturesItems.add(new PicturesItem("", ""));
        }
        for (int i = 0; i < 6; i++) {

            StorageReference mRef = storageReference.child("offers/" + s + ".jpg");
            final PicturesItem mItem = picturesItems.get(i);
            mRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    mItem.setFirstPicture(uri.toString());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("", "the error is :" + e.getMessage());
                }
            });
            StorageReference ref = storageReference.child("offers/" + k + ".jpg");
            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    mItem.setSecondPicture(uri.toString());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("", "the error is :" + e.getMessage());
                }
            });
            picturesItems.set(i, mItem);
            //dealsAdapter.notifyDataSetChanged();
            s += 2;
            k += 2;
        }
    }

    private void addingGridSales() {
        deals = new ArrayList<>();
        //filling the deals items with dummy data
        for (int j = 0; j < 6; j++) {
            ArrayList<GridItem> gridItems = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                gridItems.add(new GridItem("", "", "",""));
            }
            DealsItem dealsItem = new DealsItem();
            dealsItem.setItemsList(gridItems);
            deals.add(dealsItem);
        }
        //int k=0;
        CollectionReference query = fireStore.collection("grid_items/");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> docs = task.getResult().getDocuments();
                    for (int i = 0; i < 6; i++) {
                        final int k = i;
                        DocumentSnapshot document = docs.get(i);

                        CollectionReference reference = document.getReference().collection("items/");
                        reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    List<DocumentSnapshot> docs = task.getResult().getDocuments();
                                    final ArrayList<GridItem> items = deals.get(k).getItemsList();
                                    //final GridItem gridItem = items.get(k);
                                    //int y=0;
                                    for (int j = 0; j < 4; j++) {
                                        DocumentSnapshot document = docs.get(j);
                                        final GridItem gridItem = items.get(j);
                                        gridItem.setTitle(document.get("title").toString());
                                        gridItem.setPrice(document.get("price").toString());
                                        gridItem.setDiscount(document.get("discount").toString());
                                        gridItem.setId(document.getId());
                                        StorageReference mRef = storageReference.child("grid_sales/" + gridItem.getId() + ".jpg");
                                        mRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                gridItem.setImageUrl(uri.toString());
                                                //items.add(gridItem);
                                                dealsAdapter.notifyDataSetChanged();
                                                //Toast.makeText(getActivity(), "fucking added!!!", Toast.LENGTH_SHORT).show();
                                                //Log.d("","the id is :" + document.getId());
                                                //Log.d("","the url is :" + uri.toString());

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("", "the damn error is :" + e.getMessage());
                                                Log.d("gridItem id : ", gridItem.getId());
                                                //Toast.makeText(getActivity(), "the same damn error!!!!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                    dealsAdapter.updateGridList(items);
                                    DealsItem dealsItem = new DealsItem();
                                    dealsItem.setItemsList(items);
                                    deals.set(k, dealsItem);
                                    dealsAdapter.notifyDataSetChanged();
                                }
                            }
                        });


                    }
                }
            }
        });
    }

    private void addingLabels() {

        labelItems = new ArrayList<>();
        //first we fill the array list with empty data
        for (int i = 0; i < 6; i++) {
            labelItems.add(new LabelItem("", ""));
        }
        final CollectionReference query = fireStore.collection("labels/");
        //final int i = 0;
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int k = 0;
                    QuerySnapshot querySnapshot = task.getResult();
                    assert querySnapshot != null;
                    List<DocumentSnapshot> docs = querySnapshot.getDocuments();
                    for (DocumentSnapshot document : docs) {
                        LabelItem item = labelItems.get(k++);
                        item.setTitle(document.get("title").toString());
                        item.setDescription(document.get("offer").toString());
                        item.setId(document.getId());
                        /*if(!document.get("offer").toString().equals("")){
                            dealsAdapter.setVisible();
                        }*/
                        //labelItems.add(item);
                        dealsAdapter.notifyDataSetChanged();
                    }
                    Log.d("yes i'm here", "i'm here");
                } else {
                    Log.d("errorsa", "i'm here");
                }

            }
        });
    }

    private void initializeBestProducts(View view) {
        bestProductsItems = new ArrayList<>();
        addingBestSales();
        bestProductsRecyclerView = view.findViewById(R.id.best_products_recycler_view);
        bestProductsRecyclerView.setHasFixedSize(true);
        bestProductsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        bestProductsAdapter = new BestProductsAdapter(getActivity(), bestProductsItems);
        bestProductsRecyclerView.setAdapter(bestProductsAdapter);
        bestProductsAdapter.setOnItemClickListener(new BestProductsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String label, String id) {
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(KEY_LABEL_ID, label);
                intent.putExtra(KEY_GRID_ID, id);
                startActivity(intent);
            }
        });
    }

    //this method is to add all the top categories items from the fire base
    private void addingTopCategories() {
        //first we add fake dummy empty data so that the items will appear and then we change the content with the retrieved data
        for (int i = 0; i < 8; i++) {
            topCategoriesList.add(new TopCategoryItem("","", "", ""));
        }
        //getting a reference to the top categories collection
        CollectionReference query = fireStore.collection("top_categories/");
        //get all the documents inside the collection
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    //get the QuerySnapShots and make a list of the documents inside
                    QuerySnapshot querySnapshot = task.getResult();
                    assert querySnapshot != null;
                    List<DocumentSnapshot> docs = querySnapshot.getDocuments();
                    //iterate inside all the documents
                    int i = 0;
                    for (DocumentSnapshot document : docs) {
                        //make an object of the TopCategoryItem and set the title and id of that object
                        final TopCategoryItem item = topCategoriesList.get(i++);
                        item.setLabel(document.get("title").toString());
                        String currentItemId = document.getId();
                        item.setItemId(currentItemId);
                        item.setCategory(document.get("category").toString());
                        //get a reference of the associated image and the stored id
                        StorageReference mRef = storageReference.child("top_categories/" + item.getItemId() + ".png");
                        //get the url of the image
                        mRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //and finally add it to the list
                                item.setImage(uri.toString());
                                //notify that the data has changed as it might take some time to load from the fire base
                                categoriesAdapter.notifyDataSetChanged();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.getMessage();
                                e.printStackTrace();
                                topCategoriesList.add(item);
                                categoriesAdapter.notifyDataSetChanged();
                                Toast.makeText(getActivity(), "Something went wrong with loading the data!!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    //hide the top categories section if we decrease the items than 8 for testing purposes
                    /*if(topCategoriesList.size() < 8){
                        categoriesRecyclerView.setVisibility(View.GONE);
                    }*/
                }
            }
        });
        Log.d("size of top categories", String.valueOf(topCategoriesList.size()));
    }

    //this method is retrieve all the images from the fire base
    private void addingSliderImages() {
        for (int i = 0; i < 7; i++) {
            adsList.add("");
        }

        StorageReference re = storageReference.child("slider_images/1.jpg");
        re.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                adsList.set(6, uri.toString());
                //dealsAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "something with the slider", Toast.LENGTH_SHORT).show();
            }
        });

        StorageReference ref = storageReference.child("slider_images/" + "5.jpg");
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                adsList.set(0, uri.toString());
                //dealsAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "something with the slider", Toast.LENGTH_SHORT).show();
            }
        });

        //i called them all with numbers from 1 to 5 so i can retrieve them with for loop
        for (int i = 1; i <= 5; i++) {
            StorageReference mRef = storageReference.child("slider_images/" + i + ".jpg");
            final int finalI = i;
            mRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    //adsList.add(uri.toString());
                    adsList.set(finalI, uri.toString());
                    //dealsAdapter.notifyDataSetChanged();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), "Something went wrong!!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void addingBestSales() {
        for (int i = 0; i < 5; i++) {
            bestProductsItems.add(new BestProductsItem("", "", "", ""));
        }
        //z is the label id for the best sales
        //we named it like that so that it wouldn't bel loaded in the deals recyclerview
        CollectionReference collectionReference = fireStore.collection("grid_items/").document("z")
                .collection("items/");
        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    assert querySnapshot != null;
                    List<DocumentSnapshot> docs = querySnapshot.getDocuments();

                    int i = 0;
                    for (DocumentSnapshot document : docs) {

                        final BestProductsItem item = bestProductsItems.get(i++);
                        item.setBestProductsTitle(document.get("title").toString());
                        item.setBestProductsOriginalPrice(document.get("price").toString());
                        item.setBestProductsCrossedPrice(document.get("oldPrice").toString());
                        item.setBestProductsID(document.getId());

                        StorageReference mRef = storageReference.child("best_sales/" + document.getId() + ".jpg");
                        mRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                item.setBestProductsImage(uri.toString());
                                //bestProductsItems.add(item);
                                bestProductsAdapter.notifyDataSetChanged();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(getTag(), e.getMessage());
                                Toast.makeText(getActivity(), "Something wrong loading best products!!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setImageSlider(View view) {
        //instead of for loop
        addingSliderImages();
        //Collections.addAll(adsList, IMAGES);
        ;
        viewPager = view.findViewById(R.id.view_pager);
        //indicator = view.findViewById(R.id.indicator);
        //sliderDotSpanel = view.findViewById(R.id.slider_dots);
        //dots = new ImageView[dotsCount];

       /* for (int i = 0; i < dotsCount; i++) {
            dots[i] = new ImageView(getActivity());
            dots[i].setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.non_active_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            params.setMargins(8, 0, 8, 0);

            sliderDotSpanel.addView(dots[i], params);
        }*/

        //dots[0].setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.active_dot));
        //getSliderImages();
        slidingAdapter = new SlidingAdapter(getActivity(), adsList);
        viewPager.setAdapter(slidingAdapter);
        //indicator.setupWithViewPager(viewPager,true);
        viewPager.setCurrentItem(1);
        if (isAdded()) {
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    currentPosition = position;

                   /* for (int i = 0; i < dotsCount; i++) {
                        //dots[i] = new ImageView(getActivity());
//                        dots[i].setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.non_active_dot));
                        dots[i].setImageDrawable(getContext().getResources().getDrawable( R.drawable.non_active_dot));
                    }

                    if (position == 0) {
                        dots[4].setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.active_dot));
                    } else if (position == 6) {
                        dots[0].setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.active_dot));
                    } else {
                        dots[position - 1].setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.active_dot));
                    }*/
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    if (currentPosition == 0)
                        viewPager.setCurrentItem(lastPosition - 1, false);
                    if (currentPosition == lastPosition)
                        viewPager.setCurrentItem(1, false);
                }
            });
            viewPager.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        initAutoSlider();
                    } else {
                        if (autoScrollHandler != null) {
                            autoScrollHandler.removeCallbacksAndMessages(null);
                            autoScrollHandler = null;
                        }
                    }
                    //init the slider here so that after stopping the swipe the auto sliding begins again
                    initAutoSlider();
                    return false;
                }
            });
        }
    }


    private void initAutoSlider() {
        NUM_PAGES = 7;

        // Auto start of viewpager
        if (autoScrollHandler == null)
            autoScrollHandler = new Handler();
        autoScrollHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int position = viewPager.getCurrentItem();
                position++;
                if (position == NUM_PAGES) position = 0;
                //slidingAdapter.notifyDataSetChanged();
                viewPager.setCurrentItem(position, true);
                autoScrollHandler.postDelayed(this, 4000);
            }
        }, 4000);
    }
}

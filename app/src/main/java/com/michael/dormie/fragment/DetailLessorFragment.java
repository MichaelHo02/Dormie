package com.michael.dormie.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.michael.dormie.R;
import com.michael.dormie.adapter.AmenityAdapter;
import com.michael.dormie.adapter.PhotoAdapter;
import com.michael.dormie.databinding.FragmentDetailLessorBinding;
import com.michael.dormie.model.Place;
import com.michael.dormie.utils.DataConverter;

import java.util.ArrayList;
import java.util.List;

public class DetailLessorFragment extends Fragment {
    private static final String TAG = "DetailFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    FragmentDetailLessorBinding b;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private PhotoAdapter photoAdapter;
    private List<Bitmap> photos;
    private AmenityAdapter amenityAdapter;
    private List<String> amenitites;

    public DetailLessorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        b = FragmentDetailLessorBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        b.dtToolbar.setNavigationOnClickListener(this::handleNavigationOnClick);

        try {
            Place place = (Place) getArguments().getSerializable("place_detail");
            b.dtPlaceName.setText(place.getName());
            b.dtPlaceAddress.setText(place.getLocation().address);
            b.dtPlaceAddress.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    final int DRAWABLE_RIGHT = 2;
                    final int DRAWABLE_BOTTOM = 3;

                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (event.getRawX() >= (b.dtPlaceAddress.getRight() - b.dtPlaceAddress.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("place", place);
                            Navigation.findNavController(view).navigate(R.id.action_detailFragment_to_mapsActivity, bundle);
                        }
                    }

                    return false;
                }
            });
            b.dtPlaceDescription.setText(place.getDescription());

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());

            photos = new ArrayList<>();
            for (String photo : place.getImages()) {
                photos.add(DataConverter.getImageBitmap(photo));
            }
            photoAdapter = new PhotoAdapter(getContext(), photos);
            b.dtViewPager.setLayoutDirection(linearLayoutManager.getLayoutDirection());
            b.dtViewPager.setAdapter(photoAdapter);

            amenitites = place.getAmenities();
            amenityAdapter = new AmenityAdapter(getContext(), amenitites);
            b.dtViewPager.setLayoutDirection(linearLayoutManager.getLayoutDirection());
            b.dtViewPager.setAdapter(amenityAdapter);

            b.dtEditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("place", place);
                }
            });
        } catch (Exception e) {
            Log.e("ERROR", String.valueOf(e));
        }
    }

    private void handleNavigationOnClick(View view) {
        DrawerLayout drawerLayout = view.getRootView().findViewById(R.id.activity_master_drawer_layout);
        drawerLayout.open();
    }

    @Override
    public void onStart() {
        super.onStart();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}
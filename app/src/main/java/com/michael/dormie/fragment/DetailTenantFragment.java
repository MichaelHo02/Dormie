package com.michael.dormie.fragment;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

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
import com.michael.dormie.databinding.FragmentDetailTenantBinding;
import com.michael.dormie.model.Place;
import com.michael.dormie.utils.DataConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetailTenantFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailTenantFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    FragmentDetailTenantBinding b;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private PhotoAdapter photoAdapter;
    private List<Bitmap> photos;
    private AmenityAdapter amenityAdapter;
    private List<String> amenitites;

    public DetailTenantFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static DetailTenantFragment newInstance(String param1, String param2) {
        DetailTenantFragment fragment = new DetailTenantFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
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
        b = FragmentDetailTenantBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        b.tdtToolbar.setNavigationOnClickListener(this::handleNavigationOnClick);

        try {
            Place place = (Place) getArguments().getSerializable("place_detail");
            b.tdtPlaceName.setText(place.getName());
            b.tdtPlaceAddress.setText(place.getLocation().address);
            b.tdtPlaceAddress.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    final int DRAWABLE_RIGHT = 2;
                    final int DRAWABLE_BOTTOM = 3;

                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (event.getRawX() >= (b.tdtPlaceAddress.getRight() - b.tdtPlaceAddress.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("place", place);
                            Navigation.findNavController(view).navigate(R.id.action_detailFragment_to_mapsActivity, bundle);
                        }
                    }

                    return false;
                }
            });
            b.tdtPlaceDescription.setText(place.getDescription());

            photos = new ArrayList<>();
            for (String photo : place.getImages()) {
                Bitmap p = DataConverter.getImageBitmap(photo);
                photos.add(p);
            }
            photoAdapter = new PhotoAdapter(requireContext(), photos);
            b.tdtViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
            b.tdtViewPager.setAdapter(photoAdapter);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false);
            amenitites = place.getAmenities();
            amenityAdapter = new AmenityAdapter(requireContext(), amenitites);
            b.tdtAmenities.setLayoutManager(linearLayoutManager);
            b.tdtAmenities.setAdapter(amenityAdapter);

            b.tdtSendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Lead to chat
                }
            });
        } catch (Exception e) {
            Log.e("ERROR", String.valueOf(e));
        }
    }

    private void handleNavigationOnClick(View view) {
        DrawerLayout drawerLayout = view.getRootView().findViewById(R.id.drawerLayout);
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
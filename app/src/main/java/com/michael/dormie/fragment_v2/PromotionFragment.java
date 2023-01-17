package com.michael.dormie.fragment_v2;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;
import com.github.kittinunf.fuel.core.ResponseHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec;
import com.google.android.material.progressindicator.IndeterminateDrawable;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.michael.dormie.R;
import com.michael.dormie.databinding.FragmentPromotionBinding;
import com.michael.dormie.model.Place;
import com.michael.dormie.model.User;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PromotionFragment extends Fragment {
    private static final String TAG = "PromotionFragment";

    private FragmentPromotionBinding b;

    private PaymentSheet paymentSheet;
    private String paymentIntentClientSecret;
    private PaymentSheet.CustomerConfiguration customerConfiguration;
    private boolean isOneMonth;

    private IndeterminateDrawable loadIcon;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentPromotionBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CircularProgressIndicatorSpec spec = new CircularProgressIndicatorSpec(this.requireContext(), null, 0,
                com.google.android.material.R.style.Widget_Material3_CircularProgressIndicator);
        loadIcon = IndeterminateDrawable.createCircularDrawable(this.requireContext(), spec);

        b.toolbar.setNavigationOnClickListener(v -> {
            DrawerLayout drawerLayout = view.getRootView().findViewById(R.id.drawerLayout);
            drawerLayout.open();
        });
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
        b.payBtnOneMonth.setOnClickListener(v -> {
            b.payBtnOneMonth.setIcon(loadIcon);
            loadingProcess();
            isOneMonth = true;
            getDetails(999);

        });
        b.payBtnOneYear.setOnClickListener(v -> {
            b.payBtnOneYear.setIcon(loadIcon);
            loadingProcess();
            isOneMonth = false;
            getDetails(9999);
        });

        // Disable button if user is promoted
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            User currentUser = documentSnapshot.toObject(User.class);
            if (currentUser.isPromoted()) {
                b.payBtnOneMonth.setEnabled(false);
                b.payBtnOneYear.setEnabled(false);
            }
        });
    }

    private void getDetails(int amount) {
        Fuel.INSTANCE.post(getString(R.string.paymentURL) + "?amount=" + amount, null)
                .responseString(new ResponseHandler<String>() {
                    @Override
                    public void success(@NonNull Request request, @NonNull Response response, String s) {
                        try {
                            JSONObject result = new JSONObject(s);
                            customerConfiguration = new PaymentSheet.CustomerConfiguration(
                                    result.getString("customer"),
                                    result.getString("ephemeralKey")
                            );
                            paymentIntentClientSecret = result.getString(
                                    "paymentIntent");
                            PaymentConfiguration.init(requireContext(), result.getString(
                                    "publishableKey"));

                            PromotionFragment.this
                                    .requireActivity().runOnUiThread(() -> showPaymentSheet());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void failure(@NonNull Request request, @NonNull Response response, @NonNull FuelError fuelError) {

                    }
                });
    }

    private void showPaymentSheet() {
        final PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder(
                "Michael Ho Company")
                .customer(customerConfiguration)
                .allowsDelayedPaymentMethods(true)
                .build();
        paymentSheet.presentWithPaymentIntent(
                paymentIntentClientSecret,
                configuration
        );
    }

    private Date chooseExpiryDate() {
        Date expiryDate;
        Calendar calendar = Calendar.getInstance();

        if (isOneMonth) {
            calendar.add(Calendar.MONTH, 1);
        } else {
            calendar.add(Calendar.YEAR, 1);
        }

        expiryDate = calendar.getTime();
        return expiryDate;
    }

    private void onPaymentSheetResult(PaymentSheetResult paymentSheetResult) {
        completeLoadingProcess();
        b.payBtnOneYear.setIcon(null);
        b.payBtnOneMonth.setIcon(null);

        if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Log.d(TAG, "Canceled");
            Snackbar.make(b.getRoot(), "Canceled payment", Snackbar.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Log.e(TAG, "Got error: ", ((PaymentSheetResult.Failed) paymentSheetResult).getError());
            Snackbar.make(b.getRoot(), "Got payment error. Please try again!",
                    Snackbar.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Log.d(TAG, "Completed");

            // Update user
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    User currentUser = documentSnapshot.toObject(User.class);
                    currentUser.setPromoted(true);
                    currentUser.setExpiryDate(chooseExpiryDate());
                    db.collection("users").document(user.getUid()).set(currentUser, SetOptions.merge());
                }
            });

            // Update user's posts
            db.collection("properties")
                    .whereEqualTo("authorId", user.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Place place = document.toObject(Place.class);
                                    place.setPromoted(true);
                                    place.setExpiryDate(chooseExpiryDate());
                                    db.collection("properties").document(document.getId()).set(place, SetOptions.merge());
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }

                        }
                    });

            b.payBtnOneMonth.setEnabled(false);
            b.payBtnOneYear.setEnabled(false);
            Snackbar.make(b.getRoot(), "Complete payment!",
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    private void loadingProcess() {
        List<View> views = Arrays.asList(b.payBtnOneMonth, b.payBtnOneYear);
        for (View view : views) {
            view.setEnabled(false);
        }
    }

    private void completeLoadingProcess() {
        List<View> views = Arrays.asList(b.payBtnOneMonth, b.payBtnOneYear);
        for (View view : views) {
            view.setEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(user.getUid());

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User currentUser = documentSnapshot.toObject(User.class);
                if (currentUser.afterExpiryDate()) {
                    currentUser.setPromoted(false);
                    db.collection("users").document(user.getUid()).set(currentUser, SetOptions.merge());
                    db.collection("properties")
                            .whereEqualTo("authorId", user.getUid())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Place place = document.toObject(Place.class);
                                            place.setPromoted(false);
                                            db.collection("properties").document(document.getId()).set(place, SetOptions.merge());
                                        }
                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }

                                }
                            });

                    b.payBtnOneMonth.setEnabled(true);
                    b.payBtnOneYear.setEnabled(true);
                }
            }
        });

    }
}
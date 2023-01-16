package com.michael.dormie;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;
import com.github.kittinunf.fuel.core.ResponseHandler;
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec;
import com.google.android.material.progressindicator.IndeterminateDrawable;
import com.google.android.material.snackbar.Snackbar;
import com.michael.dormie.databinding.FragmentPromotionBinding;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class PromotionFragment extends Fragment {
    private static final String TAG = "PromotionFragment";

    FragmentPromotionBinding b;

    PaymentSheet paymentSheet;
    String paymentIntentClientSecret;
    PaymentSheet.CustomerConfiguration customerConfiguration;

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
            getDetails(999);
        });
        b.payBtnOneYear.setOnClickListener(v -> {
            b.payBtnOneYear.setIcon(loadIcon);
            loadingProcess();
            getDetails(9999);
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

    private void onPaymentSheetResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Log.d(TAG, "Canceled");
            Snackbar.make(b.getRoot(), "Canceled payment", Snackbar.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Log.e(TAG, "Got error: ", ((PaymentSheetResult.Failed) paymentSheetResult).getError());
            Snackbar.make(b.getRoot(), "Got payment error. Please try again!",
                    Snackbar.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Log.d(TAG, "Completed");
            Snackbar.make(b.getRoot(), "Complete payment!",
                    Snackbar.LENGTH_SHORT).show();
        }
        completeLoadingProcess();
        b.payBtnOneYear.setIcon(null);
        b.payBtnOneMonth.setIcon(null);
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
}
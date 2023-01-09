package com.michael.dormie.implement;

import com.google.android.libraries.places.api.model.AutocompletePrediction;

public interface IClickableCallback {
    void onClickItem(int pos, AutocompletePrediction autocompletePrediction);
}

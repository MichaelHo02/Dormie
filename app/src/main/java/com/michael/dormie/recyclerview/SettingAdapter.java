package com.michael.dormie.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.michael.dormie.R;

import java.util.List;

public class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.SettingViewHolder> {

    private List<SettingCard> list;

    public SettingAdapter(List<SettingCard> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public SettingAdapter.SettingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.setting_card, parent, false);
        return new SettingAdapter.SettingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingAdapter.SettingViewHolder holder, int position) {
        SettingCard card = list.get(position);
        if (card == null) {
            return;
        }
        holder.cardTitle.setText(card.getCardTitle());
        holder.cardContent.setText(card.getCardContent());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class SettingViewHolder extends RecyclerView.ViewHolder {
        private TextView cardTitle;
        private TextView cardContent;
        private Switch cardSwitch;

        public SettingViewHolder(@NonNull View itemView) {
            super(itemView);

            cardTitle = itemView.findViewById(R.id.setting_title);
            cardContent = itemView.findViewById(R.id.setting_content);
            cardSwitch = itemView.findViewById(R.id.setting_switch);

            cardSwitch.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            /** Do something **/
                        }
                    }
            );
        }
    }
}

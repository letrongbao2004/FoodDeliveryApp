package com.fooddeliveryapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fooddeliveryapp.R;
import com.fooddeliveryapp.models.Advertisement;
import com.fooddeliveryapp.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

public class AdCarouselAdapter extends RecyclerView.Adapter<AdCarouselAdapter.AdViewHolder> {
    public interface Listener {
        void onBannerClick(Advertisement ad);
        void onPrimaryCtaClick(Advertisement ad);
        void onSecondaryCtaClick(Advertisement ad);
    }

    private final Context context;
    private final boolean merchantMode;
    private final Listener listener;
    private final List<Advertisement> ads = new ArrayList<>();

    public AdCarouselAdapter(Context context, boolean merchantMode, Listener listener) {
        this.context = context;
        this.merchantMode = merchantMode;
        this.listener = listener;
    }

    public void submit(List<Advertisement> list) {
        ads.clear();
        if (list != null) ads.addAll(list);
        notifyDataSetChanged();
    }

    public void prepend(Advertisement ad) {
        if (ad == null) return;
        ads.add(0, ad);
        notifyItemInserted(0);
    }

    @NonNull
    @Override
    public AdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ad_banner, parent, false);
        return new AdViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdViewHolder holder, int position) {
        Advertisement ad = ads.get(position);
        holder.tvTitle.setText(ad.getTitle());
        holder.tvDescription.setText(ad.getDescription() == null ? "" : ad.getDescription());
        holder.tvPrice.setText(ad.getMenuItem() != null ? AppUtils.formatPrice(ad.getMenuItem().getPrice()) : "");
        holder.tvSecondary.setVisibility(merchantMode ? View.GONE : View.VISIBLE);
        holder.tvPrimary.setText(merchantMode ? "View Menu Item" : "Add to Cart");
        holder.tvSecondary.setText("Buy Now");

        Glide.with(context)
                .load(ad.getImageUrl())
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(holder.ivBanner);

        holder.itemView.setOnClickListener(v -> listener.onBannerClick(ad));
        holder.tvPrimary.setOnClickListener(v -> listener.onPrimaryCtaClick(ad));
        holder.tvSecondary.setOnClickListener(v -> listener.onSecondaryCtaClick(ad));
    }

    @Override
    public int getItemCount() {
        return ads.size();
    }

    static class AdViewHolder extends RecyclerView.ViewHolder {
        android.widget.ImageView ivBanner;
        TextView tvTitle, tvDescription, tvPrice, tvPrimary, tvSecondary;

        public AdViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBanner = itemView.findViewById(R.id.ivAdBanner);
            tvTitle = itemView.findViewById(R.id.tvAdTitle);
            tvDescription = itemView.findViewById(R.id.tvAdDescription);
            tvPrice = itemView.findViewById(R.id.tvAdPrice);
            tvPrimary = itemView.findViewById(R.id.btnAdPrimary);
            tvSecondary = itemView.findViewById(R.id.btnAdSecondary);
        }
    }
}

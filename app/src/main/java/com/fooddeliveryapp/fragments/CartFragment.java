package com.fooddeliveryapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.activities.CartActivity;
import com.fooddeliveryapp.adapters.CartAdapter;
import com.fooddeliveryapp.managers.CartManager;
import com.fooddeliveryapp.models.CartItem;
import com.fooddeliveryapp.utils.AppUtils;
import com.fooddeliveryapp.utils.SessionManager;

import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.OnCartActionListener {

    private CartManager cartManager;
    private SessionManager session;
    private List<CartItem> cartItems;
    private CartAdapter adapter;

    private RecyclerView rvCart;
    private View layoutEmptyCart, layoutCartContent;
    private TextView tvSubtotal, tvTotal, btnCheckout;

    private static final double DELIVERY_FEE = 1.99;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cartManager = CartManager.getInstance(requireContext());
        session     = SessionManager.getInstance(requireContext());

        rvCart          = view.findViewById(R.id.rvFragmentCart);
        layoutEmptyCart = view.findViewById(R.id.layoutFragmentEmptyCart);
        layoutCartContent = view.findViewById(R.id.layoutFragmentCartContent);
        tvSubtotal      = view.findViewById(R.id.tvFragSubtotal);
        tvTotal         = view.findViewById(R.id.tvFragTotal);
        btnCheckout     = view.findViewById(R.id.btnFragCheckout);

        loadCart();

        btnCheckout.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CartActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCart();
    }

    private void loadCart() {
        cartItems = cartManager.getCartItems(session.getUserId());
        if (cartItems.isEmpty()) {
            layoutEmptyCart.setVisibility(View.VISIBLE);
            layoutCartContent.setVisibility(View.GONE);
        } else {
            layoutEmptyCart.setVisibility(View.GONE);
            layoutCartContent.setVisibility(View.VISIBLE);
            adapter = new CartAdapter(requireContext(), cartItems, this);
            rvCart.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvCart.setAdapter(adapter);
            updateSummary();
        }
    }

    private void updateSummary() {
        double sub = cartManager.getSubtotal(session.getUserId());
        double total = sub + DELIVERY_FEE;
        tvSubtotal.setText(AppUtils.formatPrice(sub));
        tvTotal.setText(AppUtils.formatPrice(total));
        btnCheckout.setText("Proceed to Checkout - " + AppUtils.formatPrice(total));
    }

    @Override
    public void onIncrease(CartItem item, int position) {
        cartManager.incrementQuantity(item.getId(), item.getQuantity());
        adapter.updateItem(position);
        updateSummary();
    }

    @Override
    public void onDecrease(CartItem item, int position) {
        if (item.getQuantity() <= 1) {
            cartManager.removeFromCart(item.getId());
            cartItems.remove(position);
            adapter.removeItem(position);
            if (cartItems.isEmpty()) loadCart();
        } else {
            cartManager.decrementQuantity(item.getId(), item.getQuantity());
            adapter.updateItem(position);
        }
        updateSummary();
    }

    @Override
    public void onRemove(CartItem item, int position) {
        cartManager.removeFromCart(item.getId());
        cartItems.remove(position);
        adapter.removeItem(position);
        if (cartItems.isEmpty()) loadCart();
        updateSummary();
    }
}

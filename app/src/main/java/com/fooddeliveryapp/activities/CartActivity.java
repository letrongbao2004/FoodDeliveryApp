package com.fooddeliveryapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.adapters.CartAdapter;
import com.fooddeliveryapp.managers.CartManager;
import com.fooddeliveryapp.models.CartItem;
import com.fooddeliveryapp.utils.AppUtils;
import com.fooddeliveryapp.utils.SessionManager;

import java.util.List;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartActionListener {

    private CartManager cartManager;
    private SessionManager session;
    private List<CartItem> cartItems;
    private CartAdapter adapter;

    private RecyclerView rvCart;
    private TextView tvEmptyCart, tvSubtotal, tvDeliveryFee, tvTotal, tvCheckout;
    private View layoutCartContent, layoutEmptyCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartManager = CartManager.getInstance(this);
        session     = SessionManager.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Cart");
        }

        bindViews();
        loadCart();

        tvCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                AppUtils.showToast(this, "Your cart is empty");
                return;
            }
            startActivity(new Intent(this, CheckoutActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCart();
    }

    private void bindViews() {
        rvCart           = findViewById(R.id.rvCart);
        tvEmptyCart      = findViewById(R.id.tvEmptyCart);
        tvSubtotal       = findViewById(R.id.tvCartSubtotal);
        tvDeliveryFee    = findViewById(R.id.tvCartDeliveryFee);
        tvTotal          = findViewById(R.id.tvCartTotal);
        tvCheckout       = findViewById(R.id.btnCartCheckout);
        layoutCartContent= findViewById(R.id.layoutCartContent);
        layoutEmptyCart  = findViewById(R.id.layoutEmptyCart);
    }

    private void loadCart() {
        cartItems = cartManager.getCartItems(session.getUserId());
        if (cartItems.isEmpty()) {
            layoutCartContent.setVisibility(View.GONE);
            layoutEmptyCart.setVisibility(View.VISIBLE);
        } else {
            layoutCartContent.setVisibility(View.VISIBLE);
            layoutEmptyCart.setVisibility(View.GONE);
            adapter = new CartAdapter(this, cartItems, this);
            rvCart.setLayoutManager(new LinearLayoutManager(this));
            rvCart.setAdapter(adapter);
            updateSummary();
        }
    }

    private void updateSummary() {
        double subtotal = cartManager.getSubtotal(session.getUserId());
        double deliveryFee = 1.99;
        tvSubtotal.setText(AppUtils.formatPrice(subtotal));
        tvDeliveryFee.setText(AppUtils.formatPrice(deliveryFee));
        tvTotal.setText(AppUtils.formatPrice(subtotal + deliveryFee));
    }

    @Override
    public void onIncrease(CartItem item, int position) {
        cartManager.incrementQuantity(item.getId(), item.getQuantity());
        item.setQuantity(item.getQuantity() + 1);
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
            item.setQuantity(item.getQuantity() - 1);
            adapter.updateItem(position);
        }
        updateSummary();
    }

    @Override
    public void onRemove(CartItem item, int position) {
        cartManager.removeFromCart(item.getId());
        cartItems.remove(position);
        adapter.removeItem(position);
        updateSummary();
        if (cartItems.isEmpty()) loadCart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

package com.fooddeliveryapp.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.fooddeliveryapp.R;
import com.fooddeliveryapp.models.OrderStatus;

public class OrderProgressView extends ConstraintLayout {

    private View dotPlaced, dotPacked, dotDelivering, dotDelivered;
    private View line1, line2, line3;
    
    // primary brand color
    private final int ACTIVE_COLOR = Color.parseColor("#4CAF50");
    private final int INACTIVE_COLOR = Color.parseColor("#E0E0E0");

    public OrderProgressView(Context context) {
        super(context);
        init(context);
    }

    public OrderProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.order_tracking_progress, this, true);
        dotPlaced = findViewById(R.id.dotPlaced);
        dotPacked = findViewById(R.id.dotPacked);
        dotDelivering = findViewById(R.id.dotDelivering);
        dotDelivered = findViewById(R.id.dotDelivered);

        line1 = findViewById(R.id.line1);
        line2 = findViewById(R.id.line2);
        line3 = findViewById(R.id.line3);
    }

    public void updateProgress(OrderStatus status) {
        if (status == null) return;
        
        // Reset all
        setDotActive(dotPlaced, false);
        setDotActive(dotPacked, false);
        setDotActive(dotDelivering, false);
        setDotActive(dotDelivered, false);
        
        line1.setBackgroundColor(INACTIVE_COLOR);
        line2.setBackgroundColor(INACTIVE_COLOR);
        line3.setBackgroundColor(INACTIVE_COLOR);

        if (status == OrderStatus.CANCELLED) {
            setDotColor(dotPlaced, Color.RED);
            return;
        }

        // Apply progressive active states using fall-through switch
        switch (status) {
            case DELIVERED:
                setDotActive(dotDelivered, true);
                line3.setBackgroundColor(ACTIVE_COLOR);
            case OUT_FOR_DELIVERY:
                setDotActive(dotDelivering, true);
                line2.setBackgroundColor(ACTIVE_COLOR);
            case ORDER_PACKED:
                setDotActive(dotPacked, true);
                line1.setBackgroundColor(ACTIVE_COLOR);
            case ORDER_PLACED:
                setDotActive(dotPlaced, true);
                break;
        }
    }

    private void setDotActive(View dot, boolean active) {
        setDotColor(dot, active ? ACTIVE_COLOR : INACTIVE_COLOR);
    }

    private void setDotColor(View dot, int color) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(color);
        dot.setBackground(shape);
    }
}

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

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.activities.AuthActivity;
import com.fooddeliveryapp.utils.SessionManager;

public class ProfileFragment extends Fragment {

    private SessionManager session;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = SessionManager.getInstance(requireContext());

        TextView tvName    = view.findViewById(R.id.tvProfileName);
        TextView tvEmail   = view.findViewById(R.id.tvProfileEmail);
        TextView tvPhone   = view.findViewById(R.id.tvProfilePhone);
        TextView tvAddress = view.findViewById(R.id.tvProfileAddress);
        TextView btnLogout = view.findViewById(R.id.btnProfileLogout);

        tvName.setText(session.getName());
        tvEmail.setText(session.getEmail());
        tvPhone.setText(session.getPhone().isEmpty() ? "Not set" : session.getPhone());
        tvAddress.setText(session.getAddress().isEmpty() ? "Not set" : session.getAddress());

        btnLogout.setOnClickListener(v -> {
            session.logout();
            Intent intent = new Intent(requireContext(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}

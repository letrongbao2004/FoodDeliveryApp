package com.fooddeliveryapp.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.fooddeliveryapp.R;
import com.fooddeliveryapp.activities.AuthActivity;
import com.fooddeliveryapp.remote.ApiClient;
import com.fooddeliveryapp.remote.ApiService;
import com.fooddeliveryapp.remote.dto.UploadResponse;
import com.fooddeliveryapp.models.User;
import com.fooddeliveryapp.utils.AppUtils;
import com.fooddeliveryapp.utils.FileUtils;
import com.fooddeliveryapp.utils.NetworkUtils;
import com.fooddeliveryapp.utils.SessionManager;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private SessionManager session;
    private ApiService apiService;
    private ActivityResultLauncher<String> pickAvatarLauncher;
    private CircleImageView ivAvatar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = SessionManager.getInstance(requireContext());
        apiService = ApiClient.getClient(requireContext()).create(ApiService.class);

        TextView tvName = view.findViewById(R.id.tvProfileName);
        TextView tvEmail = view.findViewById(R.id.tvProfileEmail);
        TextView tvPhone = view.findViewById(R.id.tvProfilePhone);
        TextView tvAddress = view.findViewById(R.id.tvProfileAddress);
        TextView btnLogout = view.findViewById(R.id.btnProfileLogout);
        TextView btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);
        ivAvatar = view.findViewById(R.id.ivProfileAvatar);

        tvName.setText(session.getName());
        tvEmail.setText(session.getEmail());
        tvPhone.setText(session.getPhone().isEmpty() ? "Not set" : session.getPhone());
        tvAddress.setText(session.getAddress().isEmpty() ? "Not set" : session.getAddress());

        String avatarUrl = session.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this).load(avatarUrl).into(ivAvatar);
        }
        refreshProfileAvatarFromServer();

        pickAvatarLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) uploadAvatar(uri);
        });
        btnChangeAvatar.setOnClickListener(v -> pickAvatarLauncher.launch("image/*"));

        btnLogout.setOnClickListener(v -> {
            session.logout();
            Intent intent = new Intent(requireContext(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void uploadAvatar(Uri uri) {
        try {
            java.io.File file = FileUtils.copyUriToCacheFile(requireContext(), uri, "avatar.jpg");
            RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), fileBody);
            RequestBody ctx = RequestBody.create(MediaType.parse("text/plain"), "avatar");

            // Optimistic preview
            ivAvatar.setImageURI(uri);

            apiService.uploadImage(part, ctx).enqueue(new Callback<UploadResponse>() {
                @Override
                public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                    if (!isAdded()) return;
                    if (response.isSuccessful() && response.body() != null && response.body().getUrl() != null) {
                        session.setAvatarUrl(response.body().getUrl());
                        Glide.with(ProfileFragment.this).load(response.body().getUrl()).into(ivAvatar);
                        AppUtils.showToast(requireContext(), "Avatar updated");
                    } else {
                        String err = NetworkUtils.readError(response);
                        AppUtils.showToast(requireContext(),
                                err != null ? err : ("Upload failed (" + response.code() + ")"));
                    }
                }

                @Override
                public void onFailure(Call<UploadResponse> call, Throwable t) {
                    if (!isAdded()) return;
                    AppUtils.showToast(requireContext(), "Upload failed");
                }
            });
        } catch (Exception e) {
            if (!isAdded()) return;
            AppUtils.showToast(requireContext(), "Upload error");
        }
    }

    private void refreshProfileAvatarFromServer() {
        long uid = session.getUserId();
        if (uid <= 0) return;
        apiService.getUserDetails((int) uid).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    String url = response.body().getProfileImage();
                    if (url != null && !url.isEmpty()) {
                        session.setAvatarUrl(url);
                        Glide.with(ProfileFragment.this).load(url).into(ivAvatar);
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // Keep silent: avatar refresh is best-effort.
            }
        });
    }
}
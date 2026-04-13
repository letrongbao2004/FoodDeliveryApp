package com.fooddeliveryapp.api.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private final String baseFolder;

    public CloudinaryService(Cloudinary cloudinary, @Value("${cloudinary.folder:food-delivery}") String baseFolder) {
        this.cloudinary = cloudinary;
        this.baseFolder = baseFolder != null && !baseFolder.isBlank() ? baseFolder.trim() : "food-delivery";
    }

    public UploadResult uploadImage(MultipartFile file, String folder) throws IOException {
        String targetFolder = (baseFolder + "/" + (folder == null ? "" : folder)).replaceAll("/+$", "");

        Map<?, ?> res = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "resource_type", "image",
                "folder", targetFolder,
                "overwrite", true,
                "transformation", new Transformation()
                        .quality("auto")
                        .fetchFormat("auto")
                        .crop("limit")
                        .width(1024)
        ));

        String secureUrl = (String) res.get("secure_url");
        String publicId = (String) res.get("public_id");
        return new UploadResult(secureUrl, publicId);
    }

    public void deleteByPublicId(String publicId) {
        if (publicId == null || publicId.isBlank()) return;
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (Exception ignored) {
        }
    }

    public record UploadResult(String imageUrl, String publicId) {}
}


package org.metube.service;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

@Service
public class CloudServiceImpl implements CloudService {

    private final Cloudinary cloudinary;

    @Autowired
    public CloudServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String upload(MultipartFile multipartFile) throws IOException {
        File file = File.createTempFile("temp", multipartFile.getOriginalFilename());
        multipartFile.transferTo(file);

        return this.cloudinary
                .uploader()
                .upload(file, new HashMap())
                .get("url")
                .toString();
    }

    @Override
    public void delete(String imageId) throws IOException {
        this.cloudinary.uploader()
                .destroy(imageId, new HashMap());
    }
}

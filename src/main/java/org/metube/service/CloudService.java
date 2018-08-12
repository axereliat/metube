package org.metube.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CloudService {

    String upload(MultipartFile multipartFile) throws IOException;

    void delete(String imageId) throws IOException;
}

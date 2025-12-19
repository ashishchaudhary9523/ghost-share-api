package online.backend.ghostshare.service;


import online.backend.ghostshare.model.ShareMultipleFiles;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public interface ShareMultipleFilesService {
    String uploadOneToOne(MultipartFile[] file) throws Exception;

    String uploadOneToMany(MultipartFile[] file) throws Exception;

    void deleteFile(String code);

    Optional<ShareMultipleFiles> downloadFile(String code) throws Exception;
}

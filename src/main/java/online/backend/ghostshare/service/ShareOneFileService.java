package online.backend.ghostshare.service;


import online.backend.ghostshare.model.ShareMultipleFiles;
import online.backend.ghostshare.payload.ShareMultipleFilesDTO;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface ShareOneFileService {
    String uploadOneToOne(ShareMultipleFilesDTO filesDTO) throws Exception;

    String uploadOneMany(ShareMultipleFilesDTO fileDTO) throws Exception;

    Optional<ShareMultipleFiles> downloadFile(String code) throws Exception;

    void deleteFile(String code);
}

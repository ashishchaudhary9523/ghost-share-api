package online.backend.ghostshare.service.serviceImplementation;


import jakarta.annotation.PostConstruct;
import online.backend.ghostshare.AESUtil.AESUtil;
import online.backend.ghostshare.model.ShareMultipleFiles;
import online.backend.ghostshare.payload.ShareMultipleFilesDTO;
import online.backend.ghostshare.repository.ShareMultipleFilesRepository;
import online.backend.ghostshare.service.ShareOneFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ShareOneFileServiceImplementation implements ShareOneFileService {
    @Value("${max.size}")
    private int MAX_SIZE;
    @Value("${masters.key}")
    private String MASTER_KEY;

    @Autowired
    private ShareMultipleFilesRepository shareOneFileRepository;

    private SecureRandom secureRandom = new SecureRandom();

    private long MAX_FILE_SIZE ;

    @PostConstruct
    public void init() {
        MAX_FILE_SIZE = (long) MAX_SIZE * 1024 * 1024;
    }

    @Override
    public String uploadOneToOne(ShareMultipleFilesDTO fileDTO) throws Exception {
        if(fileDTO.getFileSize() > MAX_FILE_SIZE){
            return "The file is too large. Upload a file within " + MAX_SIZE + "MB";
        }
        String code = generateCode();
        if(shareOneFileRepository.existsById(code)){
            while (shareOneFileRepository.existsById(code)){
                code = generateCode();
            }
        }
        ShareMultipleFiles shareOneFile = new ShareMultipleFiles();
        shareOneFile.setFileId(code);
        shareOneFile.setFileName(fileDTO.getFileName());
        shareOneFile.setFileType(fileDTO.getFileType());
        shareOneFile.setFileSize(fileDTO.getFileSize());
        String content = AESUtil.encrypt(new String(fileDTO.getFileContent()) , MASTER_KEY);
        shareOneFile.setFileContent(content.getBytes());
        shareOneFile.setExpiryTime(LocalDateTime.now().plusHours(6));
        shareOneFile.setDownloads(1);
        shareOneFileRepository.save(shareOneFile);
        return code;
    }

    @Override
    public String uploadOneMany(ShareMultipleFilesDTO fileDTO) throws Exception {
        if(fileDTO.getFileSize() > MAX_FILE_SIZE){
            return "The file is too large. Upload a file within " + MAX_SIZE + "MB";
        }
        String code = generateCode();
        if(shareOneFileRepository.existsById(code)){
            while (shareOneFileRepository.existsById(code)){
                code = generateCode();
            }
        }
        ShareMultipleFiles shareOneFile = new ShareMultipleFiles();
        shareOneFile.setFileId(code);
        shareOneFile.setFileName(fileDTO.getFileName());
        shareOneFile.setFileType(fileDTO.getFileType());
        shareOneFile.setFileSize(fileDTO.getFileSize());
        String content = AESUtil.encrypt(new String(fileDTO.getFileContent()) , MASTER_KEY);
        shareOneFile.setFileContent(content.getBytes());
        shareOneFile.setExpiryTime(LocalDateTime.now().plusMinutes(10));
        shareOneFile.setDownloads(Integer.MAX_VALUE);
        shareOneFileRepository.save(shareOneFile);
        return code;
    }

    @Override
    public Optional<ShareMultipleFiles> downloadFile(String code) throws Exception {
        if(!shareOneFileRepository.existsById(code)){
            return Optional.empty();
        }
        Optional<ShareMultipleFiles> file = shareOneFileRepository.findById(code);
        ShareMultipleFiles fileEntity = file.get();
        String content = AESUtil.decrypt(new String(fileEntity.getFileContent()) , MASTER_KEY);
        fileEntity.setFileContent(content.getBytes());
        if(fileEntity.getExpiryTime().isBefore(LocalDateTime.now())){
            shareOneFileRepository.deleteById(code);
            return Optional.empty();
        }
        if(fileEntity.getDownloads() == 1){
            shareOneFileRepository.deleteById(code);
            return Optional.of(fileEntity);
        }
        return Optional.of(fileEntity);
    }

    @Override
    public void deleteFile(String code) {
        if(!shareOneFileRepository.existsById(code)){
            return;
        }
        shareOneFileRepository.deleteById(code);
    }

    private String generateCode(){
        int code = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}

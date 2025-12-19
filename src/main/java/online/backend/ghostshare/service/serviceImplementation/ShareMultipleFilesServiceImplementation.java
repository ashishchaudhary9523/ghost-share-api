package online.backend.ghostshare.service.serviceImplementation;


import jakarta.annotation.PostConstruct;
import online.backend.ghostshare.AESUtil.AESUtil;
import online.backend.ghostshare.model.ShareMultipleFiles;
import online.backend.ghostshare.repository.ShareMultipleFilesRepository;
import online.backend.ghostshare.service.ShareMultipleFilesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ShareMultipleFilesServiceImplementation implements ShareMultipleFilesService {

    @Value("${max.size}")
    private int MAX_SIZE;
    @Value("${masters.key}")
    private String MASTER_KEY;

    @Autowired
    private ShareMultipleFilesRepository shareMultipleFilesRepository;

    private SecureRandom secureRandom = new SecureRandom();

    private long MAX_FILE_SIZE ;

    @PostConstruct
    public void init() {
        MAX_FILE_SIZE = (long) MAX_SIZE * 1024 * 1024;
    }

    @Override
    public String uploadOneToOne(MultipartFile[] file) throws Exception {
        String code = generateCode();
        if(shareMultipleFilesRepository.existsById(code)){
            while (shareMultipleFilesRepository.existsById(code)){
                code = generateCode();
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        long totalSize = 0;
        for (MultipartFile f : file) {
            if(f.isEmpty()){
                continue;
            }
            totalSize += f.getSize();
            if(totalSize > MAX_FILE_SIZE){
                zos.close();
                return "The file is too large. Upload a file within " + MAX_SIZE + "MB";
            }
            ZipEntry zipEntry = new ZipEntry(Objects.requireNonNull(f.getOriginalFilename()));
            try {
                zos.putNextEntry(zipEntry);
                zos.write(f.getBytes());
                zos.closeEntry();
            } catch (Exception e) {
                return "Server Error, Unable to upload file.";
            }
        }
        zos.close();
        if(totalSize > MAX_FILE_SIZE){
            return "The file is too large. Upload a file within " + MAX_SIZE + "MB";
        }
        ShareMultipleFiles files = new ShareMultipleFiles();
        files.setFileId(code);
        files.setFileName("files_" + System.currentTimeMillis() + ".zip");
        files.setFileType("application/zip");
        String content = AESUtil.encrypt(new String(baos.toByteArray()) , MASTER_KEY);
        files.setFileContent(content.getBytes());

        files.setFileContent(baos.toByteArray());
        // Content-Length should match the zipped payload size
        files.setFileSize(baos.size());
        files.setDownloads(1);
        files.setExpiryTime(LocalDateTime.now().plusMinutes(15));
        files.setExpired(false);
        shareMultipleFilesRepository.save(files);

        return code;
    }
    @Override
    public String uploadOneToMany(MultipartFile[] file) throws Exception {
        String code = generateCode();
        if(shareMultipleFilesRepository.existsById(code)){
            while (shareMultipleFilesRepository.existsById(code)){
                code = generateCode();
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        long totalSize = 0;
        for (MultipartFile f : file) {
            if(f.isEmpty()){
                continue;
            }
            totalSize += f.getSize();
            if(totalSize > MAX_FILE_SIZE){
                // Close stream before returning to avoid resource leak
                zos.close();
                return "The file is too large. Upload a file within " + MAX_SIZE + "MB";
            }
            ZipEntry zipEntry = new ZipEntry(Objects.requireNonNull(f.getOriginalFilename()));
            try {
                zos.putNextEntry(zipEntry);
                zos.write(f.getBytes());
                zos.closeEntry();
            } catch (Exception e) {
                return "Server Error, Unable to upload file.";
            }
        }
        zos.close();
        if(totalSize > MAX_FILE_SIZE){
            return "The file is too large. Upload a file within " + MAX_SIZE + "MB";
        }
        ShareMultipleFiles files = new ShareMultipleFiles();
        files.setFileId(code);
        files.setFileName("files_" + System.currentTimeMillis() + ".zip");
        files.setFileType("application/zip");
        String content = AESUtil.encrypt(new String(baos.toByteArray()) , MASTER_KEY);
        files.setFileContent(content.getBytes());
        // Content-Length must reflect zipped content size, not sum of originals
        files.setFileSize(baos.size());
        files.setDownloads(Integer.MAX_VALUE);
        files.setExpiryTime(LocalDateTime.now().plusMinutes(10));
        files.setExpired(false);
        shareMultipleFilesRepository.save(files);

        return code;
    }

    @Override
    public void deleteFile(String code) {
        if(!shareMultipleFilesRepository.existsById(code)){
            return;
        }
        shareMultipleFilesRepository.deleteById(code);
    }

    @Override
    public Optional<ShareMultipleFiles> downloadFile(String code) throws Exception {
        if(!shareMultipleFilesRepository.existsById(code)){
            return Optional.empty();
        }
        Optional<ShareMultipleFiles> file = shareMultipleFilesRepository.findById(code);
        if(file.isEmpty()){
            return Optional.empty();
        }
        ShareMultipleFiles shareMultipleFiles = file.get();
        String content = AESUtil.decrypt(new String(shareMultipleFiles.getFileContent()) , MASTER_KEY);
        shareMultipleFiles.setFileContent(content.getBytes());
        if(shareMultipleFiles.getExpiryTime().isBefore(LocalDateTime.now())){
            shareMultipleFilesRepository.deleteById(code);
            return Optional.empty();
        }
        if(shareMultipleFiles.getDownloads() == 1){
            shareMultipleFilesRepository.deleteById(code);
            return Optional.of(shareMultipleFiles);
        }
        return Optional.of(shareMultipleFiles);
    }


    private String generateCode(){
        int code = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

}

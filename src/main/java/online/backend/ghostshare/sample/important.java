package online.backend.ghostshare.sample;

public class important {

    /*
    package online.backend.ghostshare.controller;


import online.backend.ghostshare.payload.ShareMultipleFilesDTO;
import online.backend.ghostshare.service.ShareMultipleFilesService;
import online.backend.ghostshare.model.ShareMultipleFiles;
import online.backend.ghostshare.service.ShareOneFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/share-files")
public class ShareMultipleFilesController {

    @Autowired
    private ShareMultipleFilesService shareMultipleFilesService;
    @Autowired
    private ShareOneFileService shareOneFileService;

    @PostMapping("/upload/one-to-one")
    public ResponseEntity<?> uploadOneToOneFile(@RequestParam("file") MultipartFile []file) {
        try {
            if(file.length > 1) {
                String code = shareMultipleFilesService.uploadOneToOne(file);
                return ResponseEntity.ok(code);
            }
            if(file.length == 1){
                ShareMultipleFilesDTO fileDTO = new ShareMultipleFilesDTO();
                fileDTO.setFileName(file[0].getOriginalFilename());
                fileDTO.setFileSize(file[0].getSize());
                fileDTO.setFileType(file[0].getContentType());
                fileDTO.setFileContent(file[0].getBytes());
                String code = shareOneFileService.uploadOneToOne(fileDTO);
                return new ResponseEntity<>(code, HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Server Error, Unable to upload file.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("Server Error, Unable to upload file.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/upload/one-to-many")
    public ResponseEntity<?> uploadOneToManyFile(@RequestParam("file") MultipartFile []file) {
        try {
            if(file.length > 1) {
                String code = shareMultipleFilesService.uploadOneToMany(file);
                return new ResponseEntity<>(code, HttpStatus.OK);
            }
            if(file.length == 1){
                ShareMultipleFilesDTO fileDTO = new ShareMultipleFilesDTO();
                fileDTO.setFileName(file[0].getOriginalFilename());
                fileDTO.setFileSize(file[0].getSize());
                fileDTO.setFileType(file[0].getContentType());
                fileDTO.setFileContent(file[0].getBytes());
                String code = shareOneFileService.uploadOneMany(fileDTO);
                return new ResponseEntity<>(code, HttpStatus.OK);
            }

        } catch (Exception e) {
            return new ResponseEntity<>("Server Error, Unable to upload file.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("Server Error, Unable to upload file.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/get-files/{code}")
    public ResponseEntity<?> getFiles(@PathVariable String code){
        Optional<ShareMultipleFiles> optionalfile = shareMultipleFilesService.downloadFile(code);
        if(optionalfile.isEmpty()){
            return new ResponseEntity<>("File Not Found.", HttpStatus.NOT_FOUND);
        }
        ShareMultipleFiles file = optionalfile.get();
        try {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION ,
                            "attachment; filename=\"" + optionalfile.get().getFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(file.getFileSize())
                    .body(file.getFileContent());
        } catch (Exception e) {
            return new ResponseEntity<>("Server Error, Unable to download file.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{code}")
    public ResponseEntity<?> deleteFiles(@PathVariable String code){
        try {
            shareMultipleFilesService.deleteFile(code);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
package online.backend.ghostshare.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/start")
public class StartController {

    @GetMapping("/server")
    public String startServer(){
        return "Server started";
    }

}
package online.backend.ghostshare.payload;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShareMultipleFilesDTO {
    private String fileId;
    @NotEmpty
    private String fileName;
    @NotEmpty
    private long fileSize;
    @NotEmpty
    private String fileType;
    @NotEmpty
    private byte[] fileContent;

}
package online.backend.ghostshare.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class ShareMultipleFiles {
    @Id
    @Column(name = "file_id" , unique = true, nullable = false)
    private String fileId;
    @Column(name = "file_name" , nullable = false)
    private String fileName;
    @Column(name = "file_size")
    private long fileSize;
    @Column(name = "file_type" , nullable = false)
    private String fileType;
    @Lob
    @Column(name = "file_content" , nullable = false)
    private byte[] fileContent;

    private LocalDateTime expiryTime;
    private boolean isExpired = false;
    private int downloads;
}
package online.backend.ghostshare.service.serviceImplementation;


import jakarta.annotation.PostConstruct;
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

    @Autowired
    private ShareMultipleFilesRepository shareMultipleFilesRepository;

    private SecureRandom secureRandom = new SecureRandom();

    private long MAX_FILE_SIZE ;

    @PostConstruct
    public void init() {
        MAX_FILE_SIZE = (long) MAX_SIZE * 1024 * 1024;
    }

    @Override
    public String uploadOneToOne(MultipartFile[] file) throws IOException {
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
    public String uploadOneToMany(MultipartFile[] file) throws IOException {
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
        files.setFileContent(baos.toByteArray());
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
    public Optional<ShareMultipleFiles> downloadFile(String code) {
        if(!shareMultipleFilesRepository.existsById(code)){
            return Optional.empty();
        }
        Optional<ShareMultipleFiles> file = shareMultipleFilesRepository.findById(code);
        if(file.isEmpty()){
            return Optional.empty();
        }
        ShareMultipleFiles shareMultipleFiles = file.get();
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
package online.backend.ghostshare.service.serviceImplementation;


import jakarta.annotation.PostConstruct;
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

    @Autowired
    private ShareMultipleFilesRepository shareOneFileRepository;

    private SecureRandom secureRandom = new SecureRandom();

    private long MAX_FILE_SIZE ;

    @PostConstruct
    public void init() {
        MAX_FILE_SIZE = (long) MAX_SIZE * 1024 * 1024;
    }

    @Override
    public String uploadOneToOne(ShareMultipleFilesDTO fileDTO) {
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
        shareOneFile.setFileContent(fileDTO.getFileContent());
        shareOneFile.setExpiryTime(LocalDateTime.now().plusHours(6));
        shareOneFile.setDownloads(1);
        shareOneFileRepository.save(shareOneFile);
        return code;
    }

    @Override
    public String uploadOneMany(ShareMultipleFilesDTO fileDTO) {
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
        shareOneFile.setFileContent(fileDTO.getFileContent());
        shareOneFile.setExpiryTime(LocalDateTime.now().plusMinutes(10));
        shareOneFile.setDownloads(Integer.MAX_VALUE);
        shareOneFileRepository.save(shareOneFile);
        return code;
    }

    @Override
    public Optional<ShareMultipleFiles> downloadFile(String code) {
        if(!shareOneFileRepository.existsById(code)){
            return Optional.empty();
        }
        Optional<ShareMultipleFiles> file = shareOneFileRepository.findById(code);
        ShareMultipleFiles fileEntity = file.get();
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


     */

}

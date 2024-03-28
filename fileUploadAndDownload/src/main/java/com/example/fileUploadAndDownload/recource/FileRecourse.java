package com.example.fileUploadAndDownload.recource;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.copy;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

@RestController
@RequestMapping("/file")
public class FileRecourse {

    // define the location, where we can save the file on the server
    public static final String DIRECTORY = System.getProperty("user.home") + "/Downloads/uploads";


    // Method to upload files
    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadFiles(@RequestParam("files")List<MultipartFile> multipartFiles) throws IOException {
        List<String> filenames = new ArrayList<>();

        for (MultipartFile file :multipartFiles) {
            // we get the file name
            String filename = StringUtils.cleanPath(file.getOriginalFilename());
            // we got the storage location
            // so this is going to give me the storage , where i need to put the file.
            // (file is going to the given directory and the name of the file is going to be the given filename)
            Path fileStorage = get(DIRECTORY, filename).toAbsolutePath().normalize();
            // so we are now copying the file to the storage , and if the file with same name exists , then we are going to replace it.
            // we save the file
            copy(file.getInputStream(), fileStorage, REPLACE_EXISTING);
            filenames.add(filename);
        }

        return ResponseEntity.ok().body(filenames);
    }


    // Define a method to download files
    @GetMapping("download/{filename}")
    public ResponseEntity<Resource> downloadFiles(@PathVariable("filename") String filename) throws IOException {
        // first we need to find the file of the given filename which user want to download
        // so this is going to give us entire path to location of the file , with the file name itself
        Path filePath = get(DIRECTORY).toAbsolutePath().normalize().resolve(filename);

        // throw exception if the file path not exists
        if (!Files.exists(filePath)){
            throw new FileNotFoundException(filename + "was not found on the server");
        }

        // with this we get the resource that we create, out of the path or the file resource that we have on the computer
        // basically we are getting the resource from the filepath(jis file ko download karna hai)
        Resource resource = new UrlResource(filePath.toUri());

        // so this is how we send something to the frontend to be downloaded or make it downloadable
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("File-Name",filename);
        httpHeaders.add(CONTENT_DISPOSITION,"attachment;File-Name=" + resource.getFilename());

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(Files.probeContentType(filePath)))
                .headers(httpHeaders).body(resource);

    }

}

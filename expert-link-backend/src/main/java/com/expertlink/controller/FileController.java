package com.expertlink.controller;

import com.expertlink.dto.ApiResponse;
import com.expertlink.dto.ApiResponses;
import com.expertlink.security.AuthPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/files")
public class FileController {

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FileUploadResponse>> upload(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "文件为空");
        }
        try {
            String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            String ext = safeFileExtension(original);
            String stored = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : ext);
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize().resolve("files");
            Files.createDirectories(dir);
            Path target = dir.resolve(stored).normalize();
            if (!target.startsWith(dir)) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "非法路径");
            }
            file.transferTo(target.toFile());
            return ApiResponses.ok(new FileUploadResponse("/files/" + stored, original));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "文件上传失败", e);
        }
    }

    @GetMapping("/{fileName}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> download(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable String fileName) {
        if (fileName == null || fileName.isBlank() || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "非法文件名");
        }
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize().resolve("files");
        Path file = dir.resolve(fileName).normalize();
        if (!file.startsWith(dir) || !Files.isRegularFile(file)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "文件不存在");
        }
        try {
            Resource resource = new UrlResource(file.toUri());
            ContentDisposition cd = ContentDisposition.attachment()
                    .filename(fileName, StandardCharsets.UTF_8)
                    .build();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                    .body(resource);
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "文件不存在");
        }
    }

    private static String safeFileExtension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int i = originalFilename.lastIndexOf('.');
        if (i < 0 || i == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(i).toLowerCase();
    }

    public record FileUploadResponse(String path, String originalFilename) {
    }
}

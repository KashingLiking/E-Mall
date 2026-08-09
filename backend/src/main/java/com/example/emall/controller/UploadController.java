package com.example.emall.controller;

import com.example.emall.exception.EMallException;
import com.example.emall.vo.ResponseVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    public static final Path UPLOAD_DIR = Paths.get("uploads").toAbsolutePath().normalize();

    private static final Set<String> ALLOWED_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    ));

    @PostMapping
    public ResponseVO<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new EMallException("400", "请选择图片文件");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new EMallException("400", "仅支持 JPG/PNG/WEBP 图片");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new EMallException("400", "图片不能超过 2MB");
        }

        Files.createDirectories(UPLOAD_DIR);

        String original = file.getOriginalFilename() == null ? "image.jpg" : file.getOriginalFilename();
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) {
            ext = original.substring(dot).toLowerCase();
        }
        if (!ext.matches("\\.(jpg|jpeg|png|webp)")) {
            ext = ".jpg";
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = UPLOAD_DIR.resolve(filename);
        file.transferTo(target.toFile());

        // 前端可直接用 http://localhost:8080/uploads/xxx.jpg 访问
        return ResponseVO.buildSuccess("/uploads/" + filename);
    }
}

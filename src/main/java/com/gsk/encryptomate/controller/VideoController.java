package com.gsk.encryptomate.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class VideoController {

    private static final String VIDEO_PATH = "D:\\video.mp4"; // change this

    @GetMapping("/video")
    public ResponseEntity<StreamingResponseBody> streamVideo(
            @RequestHeader(value = "Range", required = false) String httpRangeList) throws IOException {

        Path path = Paths.get(VIDEO_PATH);
        Resource video = new UrlResource(path.toUri());
        long contentLength = video.contentLength();

        long rangeStart = 0;
        long rangeEnd = contentLength - 1;

        if (httpRangeList != null && httpRangeList.startsWith("bytes=")) {
            String[] ranges = httpRangeList.substring(6).split("-");
            rangeStart = Long.parseLong(ranges[0]);
            if (ranges.length > 1 && !ranges[1].isEmpty()) {
                rangeEnd = Long.parseLong(ranges[1]);
            }
        }

        long finalRangeStart = rangeStart;
        long finalRangeEnd = rangeEnd;
        long rangeLength = finalRangeEnd - finalRangeStart + 1;

        StreamingResponseBody responseBody = outputStream -> {
            try (InputStream inputStream = video.getInputStream()) {
                inputStream.skip(finalRangeStart);
                byte[] buffer = new byte[1024];
                long bytesToRead = rangeLength;
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1 && bytesToRead > 0) {
                    outputStream.write(buffer, 0, (int) Math.min(bytesRead, bytesToRead));
                    bytesToRead -= bytesRead;
                }
            }
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaTypeFactory.getMediaType(video).orElse(MediaType.APPLICATION_OCTET_STREAM));
        headers.set("Accept-Ranges", "bytes");
        headers.set("Content-Range", "bytes " + finalRangeStart + "-" + finalRangeEnd + "/" + contentLength);
        headers.setContentLength(rangeLength);

        return new ResponseEntity<>(responseBody, headers, HttpStatus.PARTIAL_CONTENT);
    }
}
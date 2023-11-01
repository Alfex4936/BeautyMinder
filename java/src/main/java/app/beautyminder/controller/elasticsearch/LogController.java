package app.beautyminder.controller.elasticsearch;

import app.beautyminder.dto.CosmeticMetricData;
import app.beautyminder.service.cosmetic.CosmeticRankService;
import app.beautyminder.service.cosmetic.CosmeticSearchService;
import app.beautyminder.service.cosmetic.ReviewSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

// TODO: change all to ADMIN_ROLE
@RequiredArgsConstructor
@RestController
@RequestMapping("/log")
public class LogController {


    @GetMapping("/log")
    public ResponseEntity<Resource> getLog() throws MalformedURLException {
        Path logPath = Paths.get("/beautyminder/logstash.log").toAbsolutePath().normalize();
        Resource logResource = new UrlResource(logPath.toUri());
        return ResponseEntity.ok().body(logResource);
    }
}

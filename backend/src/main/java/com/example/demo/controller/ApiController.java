package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Value("${app.allowed-root:./allowed_root}")
    private String allowedRoot;

    @PostMapping("/delete")
    public Map<String, Object> delete(@RequestBody List<String> paths) {
        List<Map<String, Object>> results = new ArrayList<>();
        Path root;
        try {
            root = Paths.get(allowedRoot).toRealPath();
        } catch (IOException e) {
            return Map.of("error", "allowed_root does not exist: " + allowedRoot);
        }
        for (String p : paths) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("path", p);
            try {
                Path target = Paths.get(p);
                if (Files.isSymbolicLink(target)) {
                    r.put("status", "refused");
                    r.put("reason", "symlink");
                } else if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                    r.put("status", "refused");
                    r.put("reason", "not_found");
                } else {
                    Path real = target.toRealPath();
                    if (!real.startsWith(root)) {
                        r.put("status", "refused");
                        r.put("reason", "outside_allowed_root");
                    } else if (!Files.isRegularFile(real, LinkOption.NOFOLLOW_LINKS)) {
                        r.put("status", "refused");
                        r.put("reason", "not_regular_file");
                    } else {
                        Files.delete(real);
                        r.put("status", "deleted");
                    }
                }
            } catch (Exception e) {
                r.put("status", "error");
                r.put("reason", e.getMessage());
            }
            results.add(r);
        }
        return Map.of("results", results);
    }
}

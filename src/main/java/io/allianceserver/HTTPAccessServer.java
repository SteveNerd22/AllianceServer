package io.allianceserver;

import com.google.gson.Gson;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;

public class HTTPAccessServer {
    private final int HTTP_PORT;
    private final String ASSETS_DIR;
    private final Gson gson;

    public HTTPAccessServer(int port, String assetsDir) {
        this.HTTP_PORT = port;
        this.ASSETS_DIR = assetsDir;

        port(HTTP_PORT);
        staticFiles.externalLocation(ASSETS_DIR);
        gson = new Gson();
        start();
    }

    public HTTPAccessServer(String assetsDir) {
        this(4567, assetsDir);
    }

    public HTTPAccessServer() {
        this("public");
    }

    private void start() {
        get("/assets", (req, res) -> {
            File folder = new File(ASSETS_DIR);
            List<Object> fileStructure = exploreDirectory(folder);

            res.type("application/json");
            return gson.toJson(fileStructure);
        });

        get("/asset/:name", (req, res) -> {
            String assetName = req.params("name");
            File assetFile = new File(ASSETS_DIR, assetName);
            if (!assetFile.exists() || assetFile.isDirectory()) {
                res.status(404);
                return "Asset non trovato";
            }
            String contentType = "application/octet-stream";
            if (assetName.endsWith(".png")) {
                contentType = "image/png";
            } else if (assetName.endsWith(".jpg") || assetName.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (assetName.endsWith(".json")) {
                contentType = "application/json";
            }
            res.type(contentType);
            return Files.readAllBytes(Paths.get(assetFile.getAbsolutePath()));
        });

        get("/ping", (req, res) -> "Pong");

        System.out.println("HTTP Asset Server avviato sulla porta " + HTTP_PORT + " con root in " + ASSETS_DIR);
    }

    private static List<Object> exploreDirectory(File folder) {
        List<Object> contents = new ArrayList<>();
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    Map<String, Object> dirMap = new HashMap<>();
                    dirMap.put("name", file.getName());
                    dirMap.put("type", "cartella");
                    dirMap.put("contents", exploreDirectory(file));
                    contents.add(dirMap);
                } else {
                    Map<String, Object> fileMap = new HashMap<>();
                    fileMap.put("name", file.getName());
                    fileMap.put("type", "file");
                    contents.add(fileMap);
                }
            }
        }
        return contents;
    }
}

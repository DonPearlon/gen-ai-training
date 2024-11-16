package com.aterehov.gen.ai.service.impl;

import com.aterehov.gen.ai.dto.VectorDbRequest;
import com.aterehov.gen.ai.service.JsonDataService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@Slf4j
public class JsonDataServiceIml implements JsonDataService {
    @Override
    public List<VectorDbRequest> readVectorDbRequestFromFile(String fileName) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<VectorDbRequest>>() {}.getType();
        try {
            Path path = new ClassPathResource(fileName).getFile().toPath();
            String jsonContent = Files.readString(path);
            return gson.fromJson(jsonContent, listType);
        } catch (IOException exception) {
            throw new RuntimeException("Failed to read JSON", exception);
        }
    }
}

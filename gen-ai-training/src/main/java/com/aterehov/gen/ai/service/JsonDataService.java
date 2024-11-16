package com.aterehov.gen.ai.service;

import com.aterehov.gen.ai.dto.VectorDbRequest;

import java.util.List;

public interface JsonDataService {
    List<VectorDbRequest> readVectorDbRequestFromFile(String fileName);
}

package com.aterehov.gen.ai.plugin;

import com.aterehov.gen.ai.dto.VectorDbRequest;
import com.aterehov.gen.ai.dto.VectorDbResponse;
import com.aterehov.gen.ai.service.VectorDbService;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
public class ProductReviewPlugin {

    private final VectorDbService vectorDbService;

    public ProductReviewPlugin(VectorDbService vectorDbService) {
        this.vectorDbService = vectorDbService;
    }

    @DefineKernelFunction(
            name = "get_product_reviews",
            description = "Retrieves product review data from VectorDB based on product name",
            returnDescription = "Returns list of vector DB response which contains text with product review data",
            returnType = "com.aterehov.gen.ai.dto.VectorDbResponse")
    public Mono<List<VectorDbResponse>> getProductReviews(
            @KernelFunctionParameter(name = "productReviewSearchRequest")
            VectorDbRequest productReviewSearchRequest

    ) {
        log.info("calling getProductReviews function of the ProductReviewPlugin");
        return vectorDbService.search(productReviewSearchRequest);
    }
}

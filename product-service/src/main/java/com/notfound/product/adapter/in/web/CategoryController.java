package com.notfound.product.adapter.in.web;

import com.notfound.product.adapter.in.web.dto.*;
import com.notfound.product.application.port.in.CreateCategoryUseCase;
import com.notfound.product.application.port.in.GetCategoryListUseCase;
import com.notfound.product.domain.exception.ForbiddenException;
import com.notfound.product.domain.model.Category;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/products/categories")
public class CategoryController {

    private final GetCategoryListUseCase getCategoryListUseCase;
    private final CreateCategoryUseCase createCategoryUseCase;

    public CategoryController(GetCategoryListUseCase getCategoryListUseCase,
                              CreateCategoryUseCase createCategoryUseCase) {
        this.getCategoryListUseCase = getCategoryListUseCase;
        this.createCategoryUseCase = createCategoryUseCase;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryTreeResponse>>> getCategoryList() {
        List<Category> all = getCategoryListUseCase.getCategoryList();
        List<CategoryTreeResponse> tree = buildTree(all, null);
        ProductErrorCode code = ProductErrorCode.CATEGORY_LIST_GET_SUCCESS;
        return ResponseEntity.ok(ApiResponse.success(code.getStatus(), code.getCode(), code.getMessage(), tree));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateCategoryResponse>> createCategory(
            @AuthUser AuthenticatedUser user,
            @RequestBody @Valid CreateCategoryRequest request) {
        if (user == null || !"ADMIN".equals(user.role())) {
            throw new ForbiddenException(ProductErrorCode.FORBIDDEN.getMessage());
        }
        Category category = createCategoryUseCase.createCategory(request.toCommand());
        ProductErrorCode code = ProductErrorCode.CATEGORY_CREATE_SUCCESS;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(code.getStatus(), code.getCode(), code.getMessage(),
                        CreateCategoryResponse.from(category)));
    }

    private List<CategoryTreeResponse> buildTree(List<Category> all, UUID parentId) {
        return all.stream()
                .filter(c -> Objects.equals(c.getParentId(), parentId))
                .sorted(Comparator.comparingInt(Category::getSortOrder))
                .map(c -> CategoryTreeResponse.from(c, buildTree(all, c.getId())))
                .toList();
    }
}

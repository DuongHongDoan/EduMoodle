package com.example.edumoodle.Service;

import com.example.edumoodle.DTO.CategoriesDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CoursesService {

    @Autowired
    private CategoriesService categoriesService;

    public List<String> getParentChildCategories() {
        Map<Integer, List<CategoriesDTO>> categoriesGroupedByParent = categoriesService.getCategoriesGroupedByParent();
        List<String> categoryHierarchy = new ArrayList<>();
        Set<Integer> parentIdsWithChildren = new HashSet<>();

        // Lưu trữ tất cả các danh mục vào tập hợp
        Set<CategoriesDTO> allCategories = new HashSet<>();
        for (List<CategoriesDTO> categories : categoriesGroupedByParent.values()) {
            for (CategoriesDTO category : categories) {
                if (category.getParent() == 0) {  // Chỉ thêm những danh mục có parentId khác 0
                    allCategories.add(category);
                }
            }
        }

        // Duyệt qua các danh mục cha
        for (Map.Entry<Integer, List<CategoriesDTO>> entry : categoriesGroupedByParent.entrySet()) {
            int parentId = entry.getKey();
            List<CategoriesDTO> subCategories = entry.getValue();

            // Tìm danh mục cha
            CategoriesDTO parentCategory = findParentCategory(parentId, categoriesGroupedByParent);
            if (parentCategory != null) {
                String parentCategoryName = parentCategory.getName();
                // Nếu danh mục cha có danh mục con
                if (subCategories != null && !subCategories.isEmpty()) {
                    parentIdsWithChildren.add(parentId); // Đánh dấu danh mục cha có con
                    categoryHierarchy.add(parentCategoryName); // Thêm danh mục cha
                    for (CategoriesDTO subCategory : subCategories) {
                        // Thêm cấu trúc "cha / con" vào danh sách
                        categoryHierarchy.add(parentCategoryName + " / " + subCategory.getName());
                    }
                }
            }
        }

        // Xử lý các danh mục không có con
        for (CategoriesDTO category : allCategories) {
            if (!parentIdsWithChildren.contains(category.getId()) &&
                    !categoryHierarchy.contains(category.getName())) {
                categoryHierarchy.add(category.getName());
            }
        }
        // Debug: In ra danh sách phân cấp cha/con trước khi trả về
//        categoryHierarchy.forEach(System.out::println);

        return categoryHierarchy;
    }

    // Phương thức tìm kiếm danh mục cha theo ID
    private CategoriesDTO findParentCategory(int parentId, Map<Integer, List<CategoriesDTO>> categoriesGroupedByParent) {
        for (List<CategoriesDTO> categories : categoriesGroupedByParent.values()) {
            for (CategoriesDTO category : categories) {
                if (category.getId() == parentId) {
                    return category;
                }
            }
        }
        return null;
    }
}

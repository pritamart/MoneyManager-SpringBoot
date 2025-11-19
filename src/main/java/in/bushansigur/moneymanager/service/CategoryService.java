package in.bushansigur.moneymanager.service;

import in.bushansigur.moneymanager.dto.CategoryDTO;
import in.bushansigur.moneymanager.entity.CategoryEntity;
import in.bushansigur.moneymanager.entity.ProfileEntity;
import in.bushansigur.moneymanager.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;

    public CategoryDTO saveCategory(CategoryDTO categoryDTO) {
        ProfileEntity profile = profileService.getCurrentProfile();
        Long profileId = profile.getId();

        // Check duplicate category name by profileId
        if (categoryRepository.existsByNameAndProfile_Id(categoryDTO.getName(), profileId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "With this category already exists");
        }
        CategoryEntity newCategory = toEntity(categoryDTO, profile);
        newCategory = categoryRepository.save(newCategory);
        return toDTO(newCategory);
    }

    public List<CategoryDTO> getCategoriesForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CategoryEntity> categories = categoryRepository.findAllByProfile(profile);
        return categories.stream().map(this::toDTO).toList();
    }

    //get category if type of user
    public List<CategoryDTO> getCategoryByTypeOfCurrentUser(String type){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CategoryEntity> entities = categoryRepository.findByTypeAndProfile_Id(type,profile.getId());
        return entities.stream().map(this::toDTO).toList();
    }

    //update category
    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity existingCategory = categoryRepository.findByIdAndProfile(categoryId, profile)
                .orElseThrow(() -> new RuntimeException("Category not found or not accessible"));
        existingCategory.setName(categoryDTO.getName());
        existingCategory.setIcon(categoryDTO.getIcon());
//        existingCategory.setType(categoryDTO.getType());
        existingCategory = categoryRepository.save(existingCategory);
        return  toDTO(existingCategory);
    }

    private CategoryEntity toEntity(CategoryDTO categoryDTO, ProfileEntity profileEntity) {
        return CategoryEntity.builder()
                .id(categoryDTO.getId())
                .name(categoryDTO.getName())
                .icon(categoryDTO.getIcon())
                .type(categoryDTO.getType())
                .profile(profileEntity)
                .build();
    }

    private CategoryDTO toDTO(CategoryEntity categoryEntity) {
        return CategoryDTO.builder()
                .id(categoryEntity.getId())
                .name(categoryEntity.getName())
                .icon(categoryEntity.getIcon())
                .type(categoryEntity.getType())
                .profile(categoryEntity.getProfile().getId())
                .createdAt(categoryEntity.getCreatedAt())
                .updatedAt(categoryEntity.getUpdatedAt())
                .build();
    }
}

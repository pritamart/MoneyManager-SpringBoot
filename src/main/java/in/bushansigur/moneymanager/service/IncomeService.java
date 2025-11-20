package in.bushansigur.moneymanager.service;

import in.bushansigur.moneymanager.dto.ExpenseDTO;
import in.bushansigur.moneymanager.dto.IncomeDTO;
import in.bushansigur.moneymanager.entity.CategoryEntity;
import in.bushansigur.moneymanager.entity.ExpenseEntity;
import in.bushansigur.moneymanager.entity.IncomeEntity;
import in.bushansigur.moneymanager.entity.ProfileEntity;
import in.bushansigur.moneymanager.repository.CategoryRepository;
import in.bushansigur.moneymanager.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeService {
    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    private final ProfileService profileService;

    public IncomeDTO addIncome(IncomeDTO incomeDTO) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(incomeDTO.getCategoryId())
                .orElseThrow(()-> new RuntimeException("Category not found"));
        IncomeEntity newIncome = toEntity(incomeDTO, profile, category);
        newIncome = incomeRepository.save(newIncome);
        return toDTO(newIncome);

    }

    public List<IncomeDTO> getCurrentMonthIncomeForUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        List<IncomeEntity> list = incomeRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
        return list.stream()
                .map(this::toDTO)
                .toList();
    }
    //notification
    List<IncomeDTO> getExpenseForCurrentUserOnDate(Long profileId, LocalDate date){
        List<IncomeEntity> income = incomeRepository.findByProfileIdAndDate(profileId, date);
        return income.stream().map(this::toDTO).toList();
    }

    public void  deleteIncome(Long incomeId){
        ProfileEntity profile = profileService.getCurrentProfile();
        IncomeEntity income = incomeRepository.findById(incomeId).orElseThrow(()-> new RuntimeException("Income not found"));
        if(!profile.getId().equals(income.getProfile().getId())) {
            throw new RuntimeException("Unauthorized to delete income");
        }
        incomeRepository.delete(income);
    }
    public List<IncomeDTO> getLast5IncomeForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> list = incomeRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return list.stream().map(this::toDTO).toList();
    }
    public BigDecimal getTotalIncomeForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal total = incomeRepository.findTotalIncomeByProfileId(profile.getId());
        return total != null? total : BigDecimal.ZERO ;
    }

    //filter

    public List<IncomeDTO> filterExpense(LocalDate startDate, LocalDate endDate, String keyword, Sort sort){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> list = incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(),startDate, endDate,keyword, sort);
        return list.stream().map(this::toDTO).toList();
    }

    //handel method
    private IncomeEntity toEntity(IncomeDTO dTO, ProfileEntity profile, CategoryEntity category) {
        return IncomeEntity.builder()
                .name(dTO.getName())
                .amount(dTO.getAmount())
                .category(category)
                .profile(profile)
                .icon(dTO.getIcon())
                .build();
    }
    public IncomeDTO toDTO(IncomeEntity entity){
        return IncomeDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .amount(entity.getAmount())
                .icon(entity.getIcon())
                .createdAt(entity.getCreatedDate())
                .updateAt(entity.getUpdatedDate())
                .date(entity.getDate())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId(): null)
                .categoryName(entity.getCategory() != null? entity.getCategory().getName(): "N/A")
                .build();
    }
}

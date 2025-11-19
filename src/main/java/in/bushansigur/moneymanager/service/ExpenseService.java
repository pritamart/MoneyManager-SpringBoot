package in.bushansigur.moneymanager.service;

import in.bushansigur.moneymanager.dto.ExpenseDTO;
import in.bushansigur.moneymanager.entity.CategoryEntity;
import in.bushansigur.moneymanager.entity.ExpenseEntity;
import in.bushansigur.moneymanager.entity.ProfileEntity;
import in.bushansigur.moneymanager.repository.CategoryRepository;
import in.bushansigur.moneymanager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;

    public ExpenseDTO addExpense(ExpenseDTO expenseDTO) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(expenseDTO.getCategoryId())
                .orElseThrow(()-> new RuntimeException("Category not found"));
        ExpenseEntity newExpense = toEntity(expenseDTO, profile, category);
        newExpense = expenseRepository.save(newExpense);
        return toDTO(newExpense);
    }

    public List<ExpenseDTO> getCurrentMonthExpenseForUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate  startDate = LocalDate.now();
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
        return list.stream()
                .map(this::toDTO)
                .toList();
    }

    public BigDecimal getTotalExpenseForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal total = expenseRepository.findTotalExpenseByProfileID(profile.getId());
        return total != null? total : BigDecimal.ZERO ;
    }

    public void  deleteExpense(Long expenseId){
        ProfileEntity profile = profileService.getCurrentProfile();
        ExpenseEntity expense = expenseRepository.findById(expenseId).orElseThrow(()-> new RuntimeException("Expense not found"));
        if(!profile.getId().equals(expense.getProfile().getId())) {
            throw new RuntimeException("Unauthorized to delete expense");
        }
        expenseRepository.delete(expense);
    }

    public List<ExpenseDTO> getLast5ExpensesForCurrentUser() {
    ProfileEntity profile = profileService.getCurrentProfile();
    List<ExpenseEntity> list = expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
    return list.stream().map(this::toDTO).toList();
    }


    //filter
    public List<ExpenseDTO> filterExpense(LocalDate startDate, LocalDate endDate, String keyword, Sort sort){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(),startDate, endDate,keyword, sort);
        return list.stream().map(this::toDTO).toList();
    }
    //handel method
    private ExpenseEntity toEntity(ExpenseDTO dTO, ProfileEntity profile, CategoryEntity category) {
        return ExpenseEntity.builder()
                .id(dTO.getId())
                .name(dTO.getName())
                .amount(dTO.getAmount())
                .category(category)
                .profile(profile)
                .createdDate(dTO.getCreatedAt())
                .updatedDate(dTO.getUpdatedAt())
                .date(dTO.getDate())
                .icon(dTO.getIcon())
                .build();
    }
    public ExpenseDTO toDTO(ExpenseEntity entity){
        return ExpenseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .amount(entity.getAmount())
                .icon(entity.getIcon())
                .createdAt(entity.getCreatedDate())
                .updatedAt(entity.getUpdatedDate())
                .date(entity.getDate())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId(): null)
                .categoryName(entity.getCategory() != null? entity.getCategory().getName(): "N/A")
                .build();
    }
}

package in.bushansigur.moneymanager.dto;

import in.bushansigur.moneymanager.entity.CategoryEntity;
import in.bushansigur.moneymanager.entity.ProfileEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseDTO {
    private Long id;
    private String name;
    private String icon;
    private LocalDate date;
    private BigDecimal amount;
    private String categoryName;
    private Long categoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package in.bushansigur.moneymanager.service;

import in.bushansigur.moneymanager.dto.ExpenseDTO;
import in.bushansigur.moneymanager.dto.IncomeDTO;
import in.bushansigur.moneymanager.dto.ResentTransactionDTO;
import in.bushansigur.moneymanager.entity.ProfileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final ProfileService  profileService;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;

    public Map<String, Object> getDashboardData() {
        ProfileEntity profile = profileService.getCurrentProfile();
        Map<String, Object> returnValue = new LinkedHashMap<>();
        List<IncomeDTO> lastIncome = incomeService.getLast5IncomeForCurrentUser();
        List<ExpenseDTO> lastExpense = expenseService.getLast5ExpensesForCurrentUser();

        List<ResentTransactionDTO> resentTransactionDTO =
                Stream.concat(
                                lastIncome.stream().map(income ->
                                        ResentTransactionDTO.builder()
                                                .id(income.getId())
                                                .profileID(profile.getId())
                                                .name(income.getName())
                                                .icon(income.getIcon())
                                                .amount(income.getAmount())
                                                .date(income.getDate())
                                                .updatedAt(income.getUpdateAt())
                                                .createdAt(income.getCreatedAt())
                                                .type("income")
                                                .build()
                                ),
                                lastExpense.stream().map(expense ->
                                        ResentTransactionDTO.builder()
                                                .id(expense.getId())
                                                .profileID(profile.getId())
                                                .name(expense.getName())
                                                .icon(expense.getIcon())
                                                .amount(expense.getAmount())
                                                .date(expense.getDate())
                                                .createdAt(expense.getCreatedAt())
                                                .updatedAt(expense.getUpdatedAt())
                                                .type("expense")
                                                .build()
                                )
                        )
                        .sorted((a, b) -> {
                            // If date is null, push item to end
                            if (a.getDate() == null && b.getDate() == null) return 0;
                            if (a.getDate() == null) return 1;
                            if (b.getDate() == null) return -1;

                            int cmp = b.getDate().compareTo(a.getDate()); // latest first

                            if (cmp == 0) {
                                if (a.getUpdatedAt() != null && b.getUpdatedAt() != null) {
                                    return b.getUpdatedAt().compareTo(a.getUpdatedAt());
                                }
                            }
                            return cmp;
                        })

                        .collect(Collectors.toList());

        returnValue.put("totalBalance", incomeService.getTotalIncomeForCurrentUser()
                .subtract(expenseService.getTotalExpenseForCurrentUser()));

        returnValue.put("totalIncome", incomeService.getTotalIncomeForCurrentUser());
        returnValue.put("totalExpense", expenseService.getTotalExpenseForCurrentUser());
        returnValue.put("resent5Expense", lastExpense);
        returnValue.put("resent5Income", lastIncome);
        returnValue.put("recentTransactions", resentTransactionDTO);
        return returnValue;
    }

}

package in.bushansigur.moneymanager.controller;

import in.bushansigur.moneymanager.dto.ExpenseDTO;
import in.bushansigur.moneymanager.service.ExpenseService;
import in.bushansigur.moneymanager.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expense")
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseDTO> addExpense(@RequestBody ExpenseDTO dto) {
        ExpenseDTO saveExpense = expenseService.addExpense(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saveExpense);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDTO>> addExpense() {
       List<ExpenseDTO> expense = expenseService.getCurrentMonthExpenseForUser();
        return ResponseEntity.ok(expense);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntity(@PathVariable Long id){
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}

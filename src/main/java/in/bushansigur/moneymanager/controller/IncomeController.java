package in.bushansigur.moneymanager.controller;

import in.bushansigur.moneymanager.dto.ExpenseDTO;
import in.bushansigur.moneymanager.dto.IncomeDTO;
import in.bushansigur.moneymanager.repository.IncomeRepository;
import in.bushansigur.moneymanager.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/income")
public class IncomeController {
    private final IncomeService incomeService;
    private final IncomeRepository  incomeRepository;

    @PostMapping
    public ResponseEntity<IncomeDTO> addIncome(@RequestBody IncomeDTO dto) {
        IncomeDTO saveIncome = incomeService.addIncome(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saveIncome);
    }
    @GetMapping
    public ResponseEntity<List<IncomeDTO>> getAllIncome() {
        List<IncomeDTO> income = incomeService.getCurrentMonthIncomeForUser();
        return ResponseEntity.status(HttpStatus.OK).body(income);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        incomeService.deleteIncome(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}

package site.leona.wirebarleytest.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import site.leona.wirebarleytest.model.TransitDto;
import site.leona.wirebarleytest.service.TransitService;

@Tag(name = "입금/출금/이쳬 관리 API")
@Validated
@RequiredArgsConstructor
@RequestMapping("/transit")
@RestController
public class TransitRestController {

    private final TransitService transitService;

    @Operation(summary = "입금 API", description = "등록된 계좌에 대해서 입금 처리 (한도제한 없음)")
    @PostMapping("/deposit")
    public TransitDto.transactionInfo deposit(@RequestBody @Valid final TransitDto.depositParam param) {
        return transitService.deposit(param);
    }

    @Operation(summary = "출금 API", description = "등록된 계좌에 대해서 입금 처리 (한도 최대 일백만원/일)")
    @PostMapping("/withdraw")
    public TransitDto.transactionInfo withdraw(@RequestBody @Valid final TransitDto.withdrawParam param) {
        return transitService.withdraw(param);
    }

    @Operation(summary = "이체 API", description = "서로 다른 등록된 계좌에 대해서 이체 처리")
    @PostMapping("/transfer")
    public TransitDto.transactionInfo transfer(@RequestBody @Valid final TransitDto.transferParam param) {
        return transitService.transfer(param);
    }

    @Operation(summary = "이체 API", description = "서로 다른 등록된 계좌에 대해서 이체 처리")
    @GetMapping("/{bankCode}/{accountNumber}/list")
    public TransitDto.transitInfo transactionHistory(@ParameterObject @PathVariable String bankCode
            , @ParameterObject @PathVariable String accountNumber
            , @PageableDefault(size = 20, sort = "transitAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return transitService.transitHistory(bankCode, accountNumber, pageable);
    }
}

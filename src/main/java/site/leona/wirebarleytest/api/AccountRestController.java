package site.leona.wirebarleytest.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import site.leona.wirebarleytest.model.AccountDto;
import site.leona.wirebarleytest.service.AccountService;

@Tag(name = "계좌 관리 API")
@Validated
@RequiredArgsConstructor
@RequestMapping("/accounts")
@RestController
public class AccountRestController {

    private final AccountService accountService;

    @Operation(summary = "계좌 등록 API", description = "계좌를 신규로 등록 처리 (중복시 기존 내역 출력)")
    @PostMapping("/")
    public AccountDto.accountInfo saveAccount(@RequestBody @Valid final AccountDto.saveAccountParam param) {
        return accountService.saveAccount(param);
    }

    @Operation(summary = "계좌 삭제 API", description = "등록된 계좌를 삭제 처리(상태값을 정지상태로 변경)")
    @DeleteMapping("/{id}")
    public AccountDto.accountInfo deleteAccount(
            @Parameter(description = "삭제할 계좌 ID", required = true)
            @Min(value = 1, message = "반드시 ID 값은 0보다 큰 숫자를 입력해야합니다.")
            @PathVariable final Long id) {
        return accountService.deleteAccount(id);
    }
}

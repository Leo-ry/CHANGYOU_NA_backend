package site.leona.wirebarleytest.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 모든 테이블의 최상위 테이블
 * 공통 사항 및 공통 액션을 담아서 반드시 선언하여 사용할 수 있도록 함
 */
@Getter
@MappedSuperclass
public class BaseEntity {

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    protected BaseEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    protected BaseEntity(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

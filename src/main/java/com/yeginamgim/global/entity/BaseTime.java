package com.yeginamgim.global.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@MappedSuperclass // 1] 엔티티 상속용도 클래스 -> 엔티티 확장 가능, 자바에서 쓸꺼면 안 써도 됨.
@Getter // 2] 상복받은 엔티티가 멤버변수 사용
@EntityListeners( AuditingEntityListener.class ) // 4] 해당 엔티티 자동 감시 적용
public class BaseTime {

    @CreatedDate // 3] 엔티티 생성날짜/시간 주입
    private Instant createdAt;

    @LastModifiedDate // 3] 엔티티 수정날짜/시간 주입
    private Instant updatedAt;
}

package com.yeginamgim.trace.entity;

import com.yeginamgim.trace.enums.ContentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "trace_element")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TraceElement {

    /** 요소 고유 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "element_id")
    private Long elementId;

    /** 연결된 흔적 (FK → traces.trace_id) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trace_id", nullable = false)
    private Trace trace;

     /**
     * 흔적 종류
     * POST_IT  : 포스트잇
     * POLAROID : 폴라로이드
     */
    @Column(name = "content_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ContentType contentType;

    /** 글 내용 (포스트잇/폴라로이드 공통 사용) */
    @Column(name = "text_content", length = 500)
    private String textContent;

    /** 폴라로이드 사진 경로 (POLAROID일 때만 사용) */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /** 요소 내 X 좌표 */
    @Column(name = "element_x")
    private Integer elementX;

    /** 요소 내 Y 좌표 */
    @Column(name = "element_y")
    private Integer elementY;

    /**
     * 꾸미기 정보 JSON
     * 포스트잇  예시 : { "color": "#FFE566", "font": "handwriting", "sticker": ["heart"] }
     * 폴라로이드 예시 : { "frame": "white", "font": "handwriting", "sticker": ["flower"] }
     */
    @Column(name = "style_json", columnDefinition = "JSON")
    private String styleJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 흔적 요소 생성 빌더 */
    @Builder
    public TraceElement(Trace trace, ContentType contentType,
                        String textContent, String imageUrl,
                        Integer elementX, Integer elementY, String styleJson) {
        this.trace = trace;
        this.contentType = contentType;
        this.textContent = textContent;
        this.imageUrl = imageUrl;
        this.elementX = elementX;
        this.elementY = elementY;
        this.styleJson = styleJson;
    }

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

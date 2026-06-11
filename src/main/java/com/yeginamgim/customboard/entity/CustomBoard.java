package com.yeginamgim.customboard.entity;

import com.yeginamgim.global.entity.BaseTime;
import com.yeginamgim.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "custom_board")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CustomBoard extends BaseTime {

    /** 커스텀 보드 고유 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "custom_board_id")
    private Long customBoardId;

    /** 보드 생성자 (FK -> users.user_id) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    /** 보드 제목 */
    @Column(name = "board_title", nullable = false, length = 100)
    private String boardTitle;

    /** 보드 설명 */
    @Column(name = "board_description", length = 500)
    private String boardDescription;

    /** 보드 커버 이미지 URL */
    @Column(name = "board_image_url", length = 500)
    private String boardImageUrl;

    public static CustomBoard create(UserEntity user, String boardTitle, String boardDescription, String boardImageUrl) {
        return CustomBoard.builder()
                .user(user)
                .boardTitle(boardTitle)
                .boardDescription(boardDescription)
                .boardImageUrl(boardImageUrl)
                .build();
    }
}

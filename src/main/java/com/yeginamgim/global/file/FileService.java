package com.yeginamgim.global.file;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileService {
    // 1] 로컬 환경
    private String baseDir = System.getProperty("user.dir");
    private String profileUploadDir = baseDir + "/build/resources/main/static/upload/profile/";
    private String boardUploadDir = baseDir + "/build/resources/main/static/upload/board/";
    // 2] AWS 환경( 클라우드 환경 -> * 추후에 * )

    // [1] 프로필 업로드
    public String profileUpload(MultipartFile uploadFile){
        // 1) 만약에 파일이 존재하지 않으면
        if( uploadFile == null || uploadFile.isEmpty() ) return null; // 업로드 실패: 파일이 없음
        // 2) 업로드 할 파일의 경로 * 서버 경로 * ,  개발자(src파일) --배포/실행--> 서버(build파일)
        File uploadPath = new File( profileUploadDir ); // 업로드할 파일명을 file 객체 내 대입
        // *** 만약에 해당 경로의 폴더가 존재하지 않으면 폴더 생성
        if( !uploadPath.exists() ){ // !: 부정문, : file객체.exists(): 경로가 존재하면 true
            uploadPath.mkdir(); // file객체.mkdir() : 경로/폴더 생성
        }
        // 3) 업로드, 실제업로드경로 + 파일명 // DB에는 static 이후 경로를 저장
        // ** 만약에 서로 다른 사람/요청이 동일한 파일명으로 다른 파일 업로드하면 무결이 깨짐. 고유식별 깨짐 **
        // 즉[ 파일명은 동일하지만 다른 파일일 수 있다. UUID vs 날짜/시간(밀리초) vs PK 번호 조합
        String uuid = UUID.randomUUID().toString(); // UUID이란? 중복없는 난수 문자열 생성 함수(고유성 보장)
        // * UUD _ 파일명 : UUID 와 파일명 사이에 _언더바 구분하자(왜? 추후에 분리해야하니깐)
        // * UUID에는 _언더바 저랟없다. 하지만 파일명에는 _언더바 존재할 수 있다.( 파일명 치환 )
        String fileName = uuid + "_" + uploadFile.getOriginalFilename().replaceAll("_", "-"); // 업로드 할 파일명
        File uploadRealPath = new File( profileUploadDir + fileName ); // 파일명과 경로 연결해서 최종적인 경로 파일객체 생성
        try {
            uploadFile.transferTo( uploadRealPath ); // 업로드파일을 특정한 경로에 이송/복사한다. *예외처리 발생*
            return fileName;
        } catch (IOException e) { System.out.println(e); }
        return null;
    }

    // [2] 보드이미지 업로드
    public String boardUpload(MultipartFile uploadFile){
        if( uploadFile == null || uploadFile.isEmpty() ) return null;

        File uploadPath = new File( boardUploadDir );
        if( !uploadPath.exists() ) uploadPath.mkdir();

        String uuid = UUID.randomUUID().toString();
        String fileName = uuid + "_" + uploadFile.getOriginalFilename().replaceAll("_", "-");
        File uploadRealPath = new File( boardUploadDir + fileName );
        try {
            uploadFile.transferTo( uploadRealPath );
            return fileName;
        } catch (IOException e) { System.out.println(e); }
        return null;
    }
}

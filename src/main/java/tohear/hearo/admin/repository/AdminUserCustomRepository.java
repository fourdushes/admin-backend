package tohear.hearo.admin.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import tohear.hearo.admin.dto.response.AdminGuardUserDto;
import tohear.hearo.admin.dto.response.AdminInstitutionsUserDto;
import tohear.hearo.admin.dto.response.AdminWardUserDto;
import tohear.hearo.archive.domain.Archive;
import tohear.hearo.care.domain.Care;

public interface AdminUserCustomRepository {
    /**
     *  관리자 페이지에서 사용해야할 데이터 목록 조회 방법들
     * 1. 사용자 정보와 사용자가 받은 진료 기록의 개수를 가져온다. + 추가로 증상에 대한 정보들이 들어온다.
     * 2. 유저 이름으로 검색할 수 있게 만든다 (페이징)
     * 3. 증상을 뽑을 것을 대비한다
     * 4. 유저의 아카이브를 확인할 수 있다 삭제 또한 가능하다
     * 5. 유저의 보호자 목록을 확인할 수 있다 삭제 또한 가능하다
     */

    Page<AdminWardUserDto> findWardUsers(Pageable pageable, String keyword, LocalDateTime startDate, LocalDateTime endDate);
    Long GuardUserCount(String wardUserId); // 피보호자의 보호자 수
    Page<AdminGuardUserDto> findGuardUsers(Pageable pageable, String keyword, LocalDateTime startDate, LocalDateTime endDate);
    Long WardUserCount(String guardUserId);
    Page<AdminInstitutionsUserDto> findInstitutionsUsers(Pageable pageable, String keyword, LocalDateTime startDate, LocalDateTime endDate);
    Long countTotalTreatment(String institutionUserId);
    Page<Archive> findArchives(Pageable pageable, String userId, LocalDateTime startDate, LocalDateTime endDate);
    Page<Care> findCares(Pageable pageable, String userId);
    
}

package tohear.hearo.admin.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tohear.hearo.admin.domain.AdminUser;
import tohear.hearo.admin.dto.request.AdminInstitutionsRequest;
import tohear.hearo.admin.dto.request.AdminLoginRequest;
import tohear.hearo.admin.dto.request.AdminUserRequest;
import tohear.hearo.admin.dto.request.DeleteArchiveRequest;
import tohear.hearo.admin.dto.request.DeleteCareRequest;
import tohear.hearo.admin.dto.request.DeleteInstitutionRequest;
import tohear.hearo.admin.dto.request.DeleteUserRequest;
import tohear.hearo.admin.dto.request.DetermineInstitutionReqeust;
import tohear.hearo.admin.dto.request.FindArchivesRequest;
import tohear.hearo.admin.dto.request.FindGuardCareRequest;
import tohear.hearo.admin.dto.request.FindInstitutionUserInIsRequest;
import tohear.hearo.admin.dto.request.FindWardCareRequest;
import tohear.hearo.admin.dto.request.ForApprovalInstitutionRequest;
import tohear.hearo.admin.dto.request.SingleArchiveRequest;
import tohear.hearo.admin.dto.response.AdminGuardCareDto;
import tohear.hearo.admin.dto.response.AdminGuardCareResponse;
import tohear.hearo.admin.dto.response.AdminGuardUserDto;
import tohear.hearo.admin.dto.response.AdminGuardUserResponse;
import tohear.hearo.admin.dto.response.AdminInstitutionDto;
import tohear.hearo.admin.dto.response.AdminInstitutionResponse;
import tohear.hearo.admin.dto.response.AdminInstitutionUserInIsDto;
import tohear.hearo.admin.dto.response.AdminInstitutionUserInIsResponse;
import tohear.hearo.admin.dto.response.AdminInstitutionsUserDto;
import tohear.hearo.admin.dto.response.AdminInstitutionsUserResponse;
import tohear.hearo.admin.dto.response.AdminLoginResponse;
import tohear.hearo.admin.dto.response.AdminWardArchiveDto;
import tohear.hearo.admin.dto.response.AdminWardArchiveResponse;
import tohear.hearo.admin.dto.response.AdminWardCareDto;
import tohear.hearo.admin.dto.response.AdminWardCareResponse;
import tohear.hearo.admin.dto.response.AdminWardUserDto;
import tohear.hearo.admin.dto.response.AdminWardUserResponse;
import tohear.hearo.admin.dto.response.ForApprovalInstitutionDto;
import tohear.hearo.admin.dto.response.ForApprovalInstitutionResponse;
import tohear.hearo.admin.dto.response.SingleArchiveResponse;
import tohear.hearo.admin.repository.AdminUserRepository;
import tohear.hearo.archive.domain.Archive;
import tohear.hearo.archive.repository.ArchiveRepository;
import tohear.hearo.care.domain.Care;
import tohear.hearo.care.repository.CareRepository;
import tohear.hearo.global.security.JwtTokenProvider;
import tohear.hearo.institution.domain.Institution;
import tohear.hearo.institution.repository.InstitutionRepository;
import tohear.hearo.user.guardian.GuardUser;
import tohear.hearo.user.guardian.GuardUserRepository;
import tohear.hearo.user.institution.InstitutionsUser;
import tohear.hearo.user.institution.InstitutionsUserRepository;
import tohear.hearo.user.ward.WardUser;
import tohear.hearo.user.ward.WardUserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final CareRepository careRepository;
    private final ArchiveRepository archiveRepository;
    private final WardUserRepository wardUserRepository;
    private final GuardUserRepository guardUserRepository;
    private final InstitutionsUserRepository institutionsUserRepository;
    private final InstitutionRepository institutionRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    // 관리자 로그인
    @Transactional
    public AdminLoginResponse validateLogin(AdminLoginRequest request) { // 로그인 검증
        AdminUser user = adminUserRepository.findById(request.getId()).orElseThrow(() -> new IllegalArgumentException("아이디가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        String accessToken = tokenProvider.createAccessToken(user.getId());
        String refreshToken = tokenProvider.createRefreshToken(user.getId());

        user.updateLoginTime();

        redisTemplate.opsForValue().set(
            "refresh-token:admin:" + user.getId(),
            refreshToken,
            Duration.ofMillis(tokenProvider.getAdminRefreshTokenValidityInMilliseconds())
        );
        
        return new AdminLoginResponse(accessToken, refreshToken);
    }

    // 관리자가 피보호자 유저 목록을 조회
    public AdminWardUserResponse findWardUser(AdminUserRequest request, Pageable pageable) {
        Page<AdminWardUserDto> wardUsers = adminUserRepository.findWardUsers(pageable, request.getKeyword(), request.getStartDate(), request.getEndDate());

        for (AdminWardUserDto adminWardUserDto : wardUsers.getContent()) {
            adminWardUserDto.setTotalGuardCount(adminUserRepository.GuardUserCount(adminWardUserDto.getUserId()));
        }

        return new AdminWardUserResponse(
            wardUsers.getTotalElements(),
            wardUsers.getNumber(),
            wardUsers.getSize(),
            wardUsers.hasNext(),
            wardUsers.getContent()
        );
    }

    // 관리자가 보호자 유저 목록을 조회
    public AdminGuardUserResponse findGuardUser(AdminUserRequest request, Pageable pageable) {
        Page<AdminGuardUserDto> guardUsers = adminUserRepository.findGuardUsers(pageable, request.getKeyword(), request.getStartDate(), request.getEndDate());

        for (AdminGuardUserDto adminGuardUserDto : guardUsers.getContent()) {
            adminGuardUserDto.setTotalWardCount(adminUserRepository.WardUserCount(adminGuardUserDto.getUserId()));
        }

        return new AdminGuardUserResponse(
            guardUsers.getTotalElements(),
            guardUsers.getNumber(),
            guardUsers.getSize(),
            guardUsers.hasNext(),
            guardUsers.getContent()
        );
    }

    // 관리자가 기관 유저 목록을 조회
    public AdminInstitutionsUserResponse findInstitutionsUser(AdminUserRequest request, Pageable pageable) {
        Page<AdminInstitutionsUserDto> institutionsUsers = adminUserRepository.findInstitutionsUsers(pageable, request.getKeyword(), request.getStartDate(), request.getEndDate());

        for (AdminInstitutionsUserDto adminInstitutionsUserDto : institutionsUsers.getContent()) {
            adminInstitutionsUserDto.setTotalTreatmentCount(adminUserRepository.countTotalTreatment(adminInstitutionsUserDto.getUserId()));
        }

        return new AdminInstitutionsUserResponse(
            institutionsUsers.getTotalElements(),
            institutionsUsers.getNumber(),
            institutionsUsers.getSize(),
            institutionsUsers.hasNext(),
            institutionsUsers.getContent()
        );
    }

    // 관리자가 기관 목록을 조회
    public AdminInstitutionResponse findInstitutions(AdminInstitutionsRequest request, Pageable pageable) {
        Page<AdminInstitutionDto> institutions =  adminUserRepository.findInstitutions(pageable, request.getKeyword(), request.getStartDate(), request.getEndDate(), request.getRegion());

        for (AdminInstitutionDto adminInstitutionDto : institutions.getContent()) {
            adminInstitutionDto.setInstitutionUserCount(adminUserRepository.InstitutionsUserCount(adminInstitutionDto.getId()));
        }

        return new AdminInstitutionResponse(
            institutions.getTotalElements(),
            institutions.getNumber(),
            institutions.getSize(),
            institutions.hasNext(),
            institutions.getContent()
        );
    }

    // 피보호자의 아카이브를 조회
    public AdminWardArchiveResponse findWardArchives(FindArchivesRequest request, Pageable pageable) {
        Page<Archive> archives = adminUserRepository.findArchives(pageable, request.getUserId(), request.getStartDate(), request.getEndDate());

        List<AdminWardArchiveDto> archiveList = new ArrayList<>();

        for (Archive archive : archives.getContent()) {
            AdminWardArchiveDto adminWardArchiveDto = new AdminWardArchiveDto(archive.getId(), archive.getTitle(), archive.getArchiveDate());
            archiveList.add(adminWardArchiveDto);
        }

        return new AdminWardArchiveResponse(
            request.getUserId(),
            request.getUserName(),
            archives.getTotalElements(),
            archives.getNumber(),
            archives.getSize(),
            archives.hasNext(),
            archiveList
        );
    }

    // 피보호자의 보호자 조회
    public AdminWardCareResponse findWardCares(FindWardCareRequest request, Pageable pageable) {
        Page<Care> cares = adminUserRepository.findWardCares(pageable, request.getWardUserId(), request.getCareState());

        List<AdminWardCareDto> careList = new ArrayList<>();

        for (Care care : cares.getContent()) {
            careList.add(
                new AdminWardCareDto(
                    care.getId(),
                    care.getGuardUser().getId(),
                    care.getGuardUser().getName(),
                    care.getCreatedAt(),
                    care.getUpdatedAt(),
                    care.getCareState(),
                    care.getMainGuardUser()
                )
            );
        }

        return new AdminWardCareResponse(
            request.getWardUserId(),
            request.getWardUserName(),
            cares.getTotalElements(),
            cares.getNumber(),
            cares.getSize(),
            cares.hasNext(),
            careList
        );
    }

    // 보호자의 피보호자 조회
    public AdminGuardCareResponse findGuardCares(FindGuardCareRequest request, Pageable pageable) {
        Page<Care> cares = adminUserRepository.findGuardCares(pageable, request.getGuardUserId(), request.getCareState());

        List<AdminGuardCareDto> careList = new ArrayList<>();

        for (Care care : cares.getContent()) {
            careList.add(
                new AdminGuardCareDto(
                    care.getId(),
                    care.getWardUser().getId(),
                    care.getWardUser().getName(),
                    care.getCreatedAt(),
                    care.getUpdatedAt(),
                    care.getCareState(),
                    care.getMainGuardUser()
                )
            );
        }

        return new AdminGuardCareResponse(
            request.getGuardUserId(),
            request.getGuardUserName(),
            cares.getTotalElements(),
            cares.getNumber(),
            cares.getSize(),
            cares.hasNext(),
            careList
        );
    }

    // 기관에 등록된 기관 사용자 확인
    public AdminInstitutionUserInIsResponse findInstitutionUserInIs(FindInstitutionUserInIsRequest request, Pageable pageable) {
        Page<InstitutionsUser> institutionsUsers = adminUserRepository.findInstitutionUserInIs(pageable, request.getKeyword(), request.getInstitutionId(), request.getState());

        List<AdminInstitutionUserInIsDto> institutionUserInIsList = new ArrayList<>();

        for (InstitutionsUser institutionsUser : institutionsUsers) {
            institutionUserInIsList.add(
                new AdminInstitutionUserInIsDto(
                    institutionsUser.getId(),
                    institutionsUser.getName(),
                    institutionsUser.getInstitutionState(),
                    institutionsUser.getSendRequestDateTime(),
                    institutionsUser.getJoinDateTime()
                )
            );
        }

        return new AdminInstitutionUserInIsResponse(
            request.getInstitutionId(),
            institutionsUsers.getTotalElements(),
            institutionsUsers.getNumber(),
            institutionsUsers.getSize(),
            institutionsUsers.hasNext(),
            institutionUserInIsList
        );
    }

    // 아카이브 상제 조회
    public SingleArchiveResponse findArchive(SingleArchiveRequest request) {

        Archive findArchive = archiveRepository.findById(request.getArchiveId()).orElseThrow(() -> new IllegalArgumentException("진료기록을 찾을 수 없습니다."));

        return new SingleArchiveResponse(
            findArchive.getId(),
            findArchive.getTitle(),
            findArchive.getArchiveDate(),
            findArchive.getText(),
            findArchive.getAllChatText(),
            findArchive.getMainSymptoms(),
            findArchive.getDoctorOpinion(),
            findArchive.getRemember(),
            findArchive.getQuestionAnswer(),
            findArchive.getDifficultWords()
        );
        
    }

    // 기관 승인 거절을 위한 조회
    public ForApprovalInstitutionResponse forApprovalInstitution(ForApprovalInstitutionRequest request, Pageable pageable) {
        Page<Institution> institutions = adminUserRepository.forApprovalInstitution(pageable, request.getKeyword(), request.getState());

        List<ForApprovalInstitutionDto> institutionList = new ArrayList<>();

        for (Institution institution : institutions) {
            institutionList.add(
                new ForApprovalInstitutionDto(
                    institution.getId(),
                    institution.getInstitutionLoginId(),
                    institution.getInstitutionName(),
                    institution.getInstitutionState(),
                    institution.getRegion(),
                    institution.getRequestDate(),
                    institution.getApprovalDate()
                )
            );
        }

        return new ForApprovalInstitutionResponse(
            institutions.getTotalElements(),
            institutions.getNumber(),
            institutions.getSize(),
            institutions.hasNext(),
            institutionList
        );
    }

    // 관리자가 기관 회원가입 승인
    @Transactional
    public void approvalInstitution(DetermineInstitutionReqeust request) {
        Institution findInstitution = institutionRepository.findById(request.getInstitutionId()).orElseThrow(() -> 
                                                                        new IllegalArgumentException("기관을 찾을 수 없습니다."));

        findInstitution.approve();
        findInstitution.setApprovalDate();
    }

    // 관리자가 기관 회원가입 거절
    @Transactional
    public void rejectInstitution(DetermineInstitutionReqeust request) {
        Institution findInstitution = institutionRepository.findById(request.getInstitutionId()).orElseThrow(() -> 
                                                                        new IllegalArgumentException("기관을 찾을 수 없습니다."));

        findInstitution.reject();
        findInstitution.setApprovalDate();
    }
    // 관리자가 피보호자 탈퇴 시킴
    @Transactional
    public void deleteWardUser(DeleteUserRequest request) {
        WardUser findWardUser = wardUserRepository.findById(request.getUserId()).orElseThrow(() -> 
                                                                new IllegalArgumentException(request.getUserId() + " 회원을 찾을 수 없습니다."));
        wardUserRepository.delete(findWardUser);
    }

    // 관리자가 보호자 탈퇴 시킴
    @Transactional
    public void deleteGuardUser(DeleteUserRequest request) {
        GuardUser findGuardUser = guardUserRepository.findById(request.getUserId()).orElseThrow(() -> 
                                                                new IllegalArgumentException(request.getUserId() + " 회원을 찾을 수 없습니다."));
        guardUserRepository.delete(findGuardUser);
    }

    // 관리자가 기관사용자 탈퇴 시킴
    @Transactional
    public void deleteInstitutionsUser(DeleteUserRequest request) {
        InstitutionsUser findInstitutionsUser = institutionsUserRepository.findById(request.getUserId()).orElseThrow(() -> 
                                                                new IllegalArgumentException(request.getUserId() + " 회원을 찾을 수 없습니다."));
        institutionsUserRepository.delete(findInstitutionsUser);
    }

    // 관리자가 아카이브 삭제함
    @Transactional
    public void deleteArchive(DeleteArchiveRequest request) {
        Archive findArchive = archiveRepository.findById(request.getArchiveId()).orElseThrow(() -> new IllegalArgumentException("진료기록을 찾을 수 없습니다."));

        archiveRepository.delete(findArchive);
    }

    // 관리자가 보호자 <-> 피보호자 삭제
    @Transactional
    public void deleteCare(DeleteCareRequest request) {
        Care findCare = careRepository.findById(request.getCareId()).orElseThrow(() -> new IllegalArgumentException("보호 관계를 찾을 수 없습니다."));

        careRepository.delete(findCare);
    }

    // 관리자가 기관 삭제
    @Transactional
    public void deleteInstitution(DeleteInstitutionRequest request) {
        Institution findInstitution = institutionRepository.findById(request.getInstitutionId()).orElseThrow(() -> new IllegalArgumentException("기관을 찾을 수 없습니다."));

        institutionRepository.delete(findInstitution);
    }
}

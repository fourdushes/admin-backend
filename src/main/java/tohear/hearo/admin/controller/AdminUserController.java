package tohear.hearo.admin.controller;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
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
import tohear.hearo.admin.dto.response.AdminGuardCareResponse;
import tohear.hearo.admin.dto.response.AdminGuardUserResponse;
import tohear.hearo.admin.dto.response.AdminInstitutionResponse;
import tohear.hearo.admin.dto.response.AdminInstitutionUserInIsResponse;
import tohear.hearo.admin.dto.response.AdminInstitutionsUserResponse;
import tohear.hearo.admin.dto.response.AdminLoginResponse;
import tohear.hearo.admin.dto.response.AdminWardArchiveResponse;
import tohear.hearo.admin.dto.response.AdminWardCareResponse;
import tohear.hearo.admin.dto.response.AdminWardUserResponse;
import tohear.hearo.admin.dto.response.ForApprovalInstitutionResponse;
import tohear.hearo.admin.dto.response.SingleArchiveResponse;
import tohear.hearo.admin.service.AdminUserService;
import tohear.hearo.global.response.Result;

@RestController 
@RequiredArgsConstructor 
@RequestMapping("/admin")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @PostMapping("/login")
    public Result login(@Valid @RequestBody AdminLoginRequest request) {
        AdminLoginResponse response = adminUserService.validateLogin(request);

        return new Result<>("200", "관리자 로그인에 성공했습니다.", response);
    }

    @GetMapping("/find-ward-user")
    public Result findWardUser(@Valid @ModelAttribute AdminUserRequest request,
                               @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        
        AdminWardUserResponse response = adminUserService.findWardUser(request, pageable);
        return new Result<>("200", "피보호자 목록을 조회했습니다.", response);
    }

    @GetMapping("/find-guard-user")
    public Result findGuardUser(@Valid @ModelAttribute AdminUserRequest request,
                               @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        
        AdminGuardUserResponse response = adminUserService.findGuardUser(request, pageable);
        return new Result<>("200", "피보호자 목록을 조회했습니다.", response);
    }

    @GetMapping("/find-institutions-user")
    public Result findInstitutionsUser(@Valid @ModelAttribute AdminUserRequest request,
                                       @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        
        AdminInstitutionsUserResponse response = adminUserService.findInstitutionsUser(request, pageable);
        return new Result<>("200", "피보호자 목록을 조회했습니다.", response);
    }

    @GetMapping("/find-institutions")
    public Result findInstitution(@ModelAttribute AdminInstitutionsRequest request,
                                  @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        AdminInstitutionResponse response = adminUserService.findInstitutions(request, pageable);
        return new Result<>("200", "기관 목록을 조회했습니다.", response);
    }

    @GetMapping("/ward-user/archive-list")
    public Result findArchiveList(@Valid @ModelAttribute FindArchivesRequest request,
                                  @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        AdminWardArchiveResponse response = adminUserService.findWardArchives(request, pageable);
        return new Result<>("200", request.getUserId() + "의 아카이브를 조회했습니다.", response);
    }
    
    @GetMapping("/ward-user/care-list")
    public Result findWardCareList(@ModelAttribute FindWardCareRequest request,
                               @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        AdminWardCareResponse response = adminUserService.findWardCares(request, pageable);
        return new Result<>("200", request.getWardUserId() + "의 보호자를 조회했습니다.", response);
    }

    @GetMapping("/guard-user/care-list")
    public Result findGuardCareList(@ModelAttribute FindGuardCareRequest request,
                                    @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        AdminGuardCareResponse response = adminUserService.findGuardCares(request, pageable);
        return new Result<>("200", request.getGuardUserId() + "의 피보호자를 조회했습니다.", response);
    }

    @GetMapping("/institutions/institution-user-list")
    public Result findInstitutionUserInIs(@ModelAttribute FindInstitutionUserInIsRequest request,
                                          @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        AdminInstitutionUserInIsResponse response = adminUserService.findInstitutionUserInIs(request, pageable);
        return new Result<>("200", "기관에 등록된 기관 사용자를 조회했습니다", response);
    }

    @GetMapping("/ward-user/read-single-archive")
    public Result findArchive(@ModelAttribute SingleArchiveRequest request) {

        SingleArchiveResponse response = adminUserService.findArchive(request);

        return new Result<>("200", "아카이브 조회에 성공했습니다.", response);
    }

    @GetMapping("/institution-list/for-determine")
    public Result forApprovalInstitution(@ModelAttribute ForApprovalInstitutionRequest request,
                                         @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        ForApprovalInstitutionResponse response = adminUserService.forApprovalInstitution(request, pageable);
        return new Result<>("200", "기관 요청 목록을 조회했습니다.", response);
    }

    @PostMapping("/institution-approval")
    public Result approvalInstitution(@RequestBody DetermineInstitutionReqeust reqeust) {

        adminUserService.approvalInstitution(reqeust);
        return new Result<>("200", "승인을 완료했습니다", null);
    }

    @PostMapping("/institution-reject")
    public Result rejectInstitution(@RequestBody DetermineInstitutionReqeust reqeust) {

        adminUserService.rejectInstitution(reqeust);
        return new Result<>("200", "거절을 완료했습니다", null);
    }
    
    @PostMapping("/delete/ward-user")
    public Result deleteWardUser(@RequestBody DeleteUserRequest request) {
        
        adminUserService.deleteWardUser(request);
        return new Result<>("200", request.getUserId() + " 유저를 삭제했습니다.", null);
    }

    @PostMapping("/delete/guard-user")
    public Result deleteGuardUser(@RequestBody DeleteUserRequest request) {
        
        adminUserService.deleteGuardUser(request);
        return new Result<>("200", request.getUserId() + " 유저를 삭제했습니다.", null);
    }

    @PostMapping("/delete/Institutions-user")
    public Result deleteInstitutionsUser(@RequestBody DeleteUserRequest request) {
        
        adminUserService.deleteInstitutionsUser(request);
        return new Result<>("200", request.getUserId() + " 유저를 삭제했습니다.", null);
    }

    @PostMapping("/delete/archive")
    public Result deleteArchive(@RequestBody DeleteArchiveRequest request) {
        
        adminUserService.deleteArchive(request);
        return new Result<>("200", "아카이브를 삭제했습니다.", null);
    }

    @PostMapping("/delete/care")
    public Result deleteArchive(@RequestBody DeleteCareRequest request) {
        
        adminUserService.deleteCare(request);
        return new Result<>("200", "보호 관계를 삭제했습니다.", null);
    }

    @PostMapping("/delete/institution")
    public Result deleteInstitution(@RequestBody DeleteInstitutionRequest request) {
        
        adminUserService.deleteInstitution(request);
        return new Result<>("200", "기관을 삭제했습니다.", null);
    }
    
    

}

package tohear.hearo.admin.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import tohear.hearo.admin.dto.response.AdminGuardUserDto;
import tohear.hearo.admin.dto.response.AdminInstitutionDto;
import tohear.hearo.admin.dto.response.AdminInstitutionsUserDto;
import tohear.hearo.admin.dto.response.AdminWardUserDto;
import tohear.hearo.archive.domain.Archive;
import tohear.hearo.archive.domain.DiseaseType;
import tohear.hearo.archive.domain.QArchive;
import tohear.hearo.archive.domain.QArchiveDisease;
import tohear.hearo.care.domain.Care;
import tohear.hearo.care.domain.CareState;
import tohear.hearo.care.domain.QCare;
import tohear.hearo.institution.domain.Institution;
import tohear.hearo.institution.domain.InstitutionApprovalState;
import tohear.hearo.institution.domain.InstitutionRegion;
import tohear.hearo.institution.domain.QInstitution;
import tohear.hearo.medicaltreatment.medicalrequest.domain.MedicalRequestStatus;
import tohear.hearo.medicaltreatment.medicalrequest.domain.QMedicalRequest;
import tohear.hearo.user.guardian.QGuardUser;
import tohear.hearo.user.institution.InstitutionUserState;
import tohear.hearo.user.institution.InstitutionsUser;
import tohear.hearo.user.institution.QInstitutionsUser;
import tohear.hearo.user.ward.QWardUser;

@RequiredArgsConstructor 
public class AdminUserRepositoryImpl implements AdminUserCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AdminWardUserDto> findWardUsers(Pageable pageable, String keyword, LocalDate startDate, LocalDate endDate) {

         List<AdminWardUserDto> wardUserList = queryFactory
            .select(Projections.constructor(AdminWardUserDto.class,
                QWardUser.wardUser.id,
                QWardUser.wardUser.name,

                diseaseCount(QArchiveDisease.archiveDisease, DiseaseType.COLD),
                diseaseCount(QArchiveDisease.archiveDisease, DiseaseType.UNDEFINED_1),
                diseaseCount(QArchiveDisease.archiveDisease, DiseaseType.UNDEFINED_2),
                diseaseCount(QArchiveDisease.archiveDisease, DiseaseType.UNDEFINED_3),
                diseaseCount(QArchiveDisease.archiveDisease, DiseaseType.UNDEFINED_4),
                diseaseCount(QArchiveDisease.archiveDisease, DiseaseType.UNDEFINED_5),
                diseaseCount(QArchiveDisease.archiveDisease, DiseaseType.UNDEFINED_6),
                diseaseCount(QArchiveDisease.archiveDisease, DiseaseType.UNDEFINED_7),
                diseaseCount(QArchiveDisease.archiveDisease, DiseaseType.UNDEFINED_8),
                diseaseCount(QArchiveDisease.archiveDisease, DiseaseType.OTHER),

                QArchive.archive.count(),
                Expressions.constant(0L), // totalGuardCount는 나중에 채우기
                QWardUser.wardUser.joinDateTime,
                QWardUser.wardUser.lastLoginDateTime
            ))
            .from(QWardUser.wardUser)
            .leftJoin(QArchive.archive).on(QWardUser.wardUser.eq(QArchive.archive.wardUser))
            .leftJoin(QArchive.archive.archiveDisease, QArchiveDisease.archiveDisease)
            .where(
                findWardByIdOrName(keyword),
                dateRange(QWardUser.wardUser.joinDateTime, startDate, endDate)
            )
            .groupBy(QWardUser.wardUser.id)
            .orderBy(QWardUser.wardUser.id.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long result = queryFactory
            .select(QWardUser.wardUser.count())
            .from(QWardUser.wardUser)
            .where(
                findWardByIdOrName(keyword),
                dateRange(QWardUser.wardUser.joinDateTime, startDate, endDate)
            )
            .fetchOne();

        long count = result != null ? result : 0L;

        return new PageImpl<>(wardUserList, pageable, count);

    }

    private NumberExpression<Long> diseaseCount(QArchiveDisease disease, DiseaseType type) {

    return new CaseBuilder()
            .when(disease.diseaseType.eq(type))
            .then(1L)
            .otherwise(0L)
            .sum()
            .coalesce(0L);
    }

    // 목록 조회에서는 현재 페이지의 사용자 ID를 모아 카운트를 일괄 집계해 N+1 쿼리를 제거한다.
    @Override
    public Long GuardUserCount(String wardUserId) {

        Long result = queryFactory
            .select(QCare.care.count())
            .from(QCare.care)
            .where(QCare.care.wardUser.id.eq(wardUserId), QCare.care.careState.in(CareState.APPROVED))
            .fetchOne();

        return result != null ? result : 0L;
    }

    private BooleanExpression findWardByIdOrName(String keyword) {
        return StringUtils.hasText(keyword) ? QWardUser.wardUser.id.contains(keyword).or(QWardUser.wardUser.name.contains(keyword)) : null;
    }

    @Override
    public Page<AdminGuardUserDto> findGuardUsers(Pageable pageable, String keyword, LocalDate startDate, LocalDate endDate) {

        List<AdminGuardUserDto> guardUserList = queryFactory
            .select(Projections.constructor(AdminGuardUserDto.class,
                 QGuardUser.guardUser.id,
                 QGuardUser.guardUser.name,
                 Expressions.constant(0L), // totalWardCount는 나중에 채우기
                 QGuardUser.guardUser.joinDateTime,
                 QGuardUser.guardUser.lastLoginDateTime
            )) 
            .from(QGuardUser.guardUser)
            .where(
                findGuardByIdOrName(keyword),
                dateRange(QGuardUser.guardUser.joinDateTime, startDate, endDate)
            )
            .orderBy(QGuardUser.guardUser.id.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long result = queryFactory
            .select(QGuardUser.guardUser.count())
            .from(QGuardUser.guardUser)
            .where(
                findGuardByIdOrName(keyword),
                dateRange(QGuardUser.guardUser.joinDateTime, startDate, endDate)
            )
            .fetchOne();
        
        long count = result != null ? result : 0L;

        return new PageImpl<>(guardUserList, pageable, count);
    }

    // 보호자와 연결된 피보호자의 수
    @Override
    public Long WardUserCount(String guardUserId) {

        Long result = queryFactory
            .select(QCare.care.count())
            .from(QCare.care)
            .where(QCare.care.guardUser.id.eq(guardUserId), QCare.care.careState.in(CareState.APPROVED))
            .fetchOne();

        return result != null ? result : 0L;
    }

    private BooleanExpression findGuardByIdOrName(String keyword) {
        return StringUtils.hasText(keyword) ? QGuardUser.guardUser.id.contains(keyword).or(QGuardUser.guardUser.name.contains(keyword)) : null;
    }


    @Override
    public Page<AdminInstitutionsUserDto> findInstitutionsUsers(Pageable pageable, String keyword, LocalDate startDate, LocalDate endDate) {

        List<AdminInstitutionsUserDto> institutionsUserList = queryFactory
            .select(Projections.constructor(AdminInstitutionsUserDto.class,
                QInstitutionsUser.institutionsUser.id,
                QInstitutionsUser.institutionsUser.name,
                Expressions.constant(0L), // totalTreatmentCount는 나중에 채우기
                QInstitution.institution.institutionName,
                QInstitutionsUser.institutionsUser.joinDateTime,
                QInstitutionsUser.institutionsUser.lastLoginDateTime
            ))
            .from(QInstitutionsUser.institutionsUser)
            .join(QInstitutionsUser.institutionsUser.institution, QInstitution.institution)
            .where(
                findInstitutionUserByIdOrName(keyword),
                dateRange(QInstitutionsUser.institutionsUser.joinDateTime, startDate, endDate)
            )
            .orderBy(QInstitutionsUser.institutionsUser.id.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long result = queryFactory
            .select(QInstitutionsUser.institutionsUser.count())
            .from(QInstitutionsUser.institutionsUser)
            .where(
                findInstitutionUserByIdOrName(keyword),
                dateRange(QInstitutionsUser.institutionsUser.joinDateTime, startDate, endDate)
            )
            .fetchOne();

        long count = result != null ? result : 0L;

        return new PageImpl<>(institutionsUserList, pageable, count);
    }

    @Override
    public Long countTotalTreatment(String institutionUserId) {
        Long result = queryFactory
            .select(QMedicalRequest.medicalRequest.id.count())
            .from(QMedicalRequest.medicalRequest)
            .where(
                QMedicalRequest.medicalRequest.institutionUser.id.eq(institutionUserId),
                QMedicalRequest.medicalRequest.status.eq(MedicalRequestStatus.COMPLETED)
            )
            .fetchOne();

        return result != null ? result : 0L;
    }

    private BooleanExpression findInstitutionUserByIdOrName(String keyword) {
        return StringUtils.hasText(keyword) ? QInstitutionsUser.institutionsUser.id.contains(keyword).or(QInstitutionsUser.institutionsUser.name.contains(keyword)) : null;
    }

    @Override
    public Page<AdminInstitutionDto> findInstitutions(Pageable pageable, String keyword, LocalDate startDate, LocalDate endDate, InstitutionRegion region) {

        List<AdminInstitutionDto> institutionList = queryFactory
            .select(Projections.constructor(AdminInstitutionDto.class,
                 QInstitution.institution.id,
                 QInstitution.institution.institutionName,
                 QInstitution.institution.institutionState,
                 Expressions.constant(0L),
                 QInstitution.institution.region
            ))
            .from(QInstitution.institution)
            .where(
                findInstitutionByIdOrName(keyword),
                dateRange(QInstitution.institution.approvalDate, startDate, endDate),
                regionCheck(region),
                QInstitution.institution.institutionState.eq(InstitutionApprovalState.APPROVED)
            )
            .orderBy(QInstitution.institution.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long result = queryFactory
            .select(QInstitution.institution.count())
            .from(QInstitution.institution)
            .where(
                findInstitutionByIdOrName(keyword),
                dateRange(QInstitution.institution.approvalDate, startDate, endDate),
                regionCheck(region),
                QInstitution.institution.institutionState.eq(InstitutionApprovalState.APPROVED)
            )
            .fetchOne();
        
        long count = result != null ? result : 0;

        return new PageImpl<>(institutionList, pageable, count);
    }

    @Override
    public Long InstitutionsUserCount(Long institutionId) {

        Long result = queryFactory
            .select(QInstitutionsUser.institutionsUser.count())
            .from(QInstitutionsUser.institutionsUser)
            .where(QInstitutionsUser.institutionsUser.institution.id.eq(institutionId))
            .fetchOne();

        return result != null ? result : 0;
    }

    private BooleanExpression findInstitutionByIdOrName(String keyword) {
        return StringUtils.hasText(keyword) ? QInstitution.institution.institutionName.contains(keyword).or(QInstitution.institution.institutionLoginId.eq(keyword)) : null;
    }

    private BooleanExpression regionCheck(InstitutionRegion region) {
        return region != null ? QInstitution.institution.region.eq(region) : null;
    }
    
    @Override
    public Page<Archive> findArchives(Pageable pageable, String userId, LocalDate startDate, LocalDate endDate) {

        List<Archive> archiveList = queryFactory
            .selectFrom(QArchive.archive)
            .where(
                QArchive.archive.wardUser.id.eq(userId),
                dateRange(QArchive.archive.archiveDate, startDate, endDate)
            )
            .orderBy(QArchive.archive.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long result = queryFactory
            .select(QArchive.archive.count())
            .from(QArchive.archive)
            .where(
                QArchive.archive.wardUser.id.eq(userId),
                dateRange(QArchive.archive.archiveDate, startDate, endDate)
            )
            .fetchOne();

        long count = result != null ? result : 0L;

        return new PageImpl<>(archiveList, pageable, count);
    }

    // 피보호자가 연결된 보호자들의 리스트를 뽑기
    @Override
    public Page<Care> findWardCares(Pageable pageable, String wardUserId, CareState careState) {

        List<Care> careList = queryFactory
            .select(QCare.care)
            .from(QCare.care)
            .leftJoin(QCare.care.guardUser, QGuardUser.guardUser)
            .fetchJoin()
            .where(
                QCare.care.wardUser.id.eq(wardUserId),
                QCare.care.careState.eq(careState)
            )
            .orderBy(QCare.care.updatedAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long result = queryFactory
            .select(QCare.care.count())
            .from(QCare.care)
            .where(
                QCare.care.wardUser.id.eq(wardUserId),
                QCare.care.careState.eq(careState)
            )
            .fetchOne();

        long count = result != null ? result : 0L;

        return new PageImpl<>(careList, pageable, count);
    }

    // 보호자가 연결된 피보호자들의 리스트를 뽑기
    @Override
    public Page<Care> findGuardCares(Pageable pageable, String guardUserId, CareState careState) {
        
        List<Care> careList = queryFactory
            .select(QCare.care)
            .from(QCare.care)
            .leftJoin(QCare.care.wardUser, QWardUser.wardUser)
            .fetchJoin()
            .where(
                QCare.care.guardUser.id.eq(guardUserId),
                QCare.care.careState.eq(careState)
            )
            .orderBy(QCare.care.updatedAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long result = queryFactory
            .select(QCare.care.count())
            .from(QCare.care)
            .where(
                QCare.care.guardUser.id.eq(guardUserId),
                QCare.care.careState.eq(careState)
            )
            .fetchOne();

        long count = result != null ? result : 0L;

        return new PageImpl<>(careList, pageable, count);
    }

    @Override
    public Page<InstitutionsUser> findInstitutionUserInIs(Pageable pageable, String keyword, Long institutionId, InstitutionUserState state) {

        List<InstitutionsUser> institutionsUserList = queryFactory
            .selectFrom(QInstitutionsUser.institutionsUser)
            .where(
                findInstitutionUserByIdOrName(keyword),
                QInstitutionsUser.institutionsUser.institution.id.eq(institutionId),
                checkApproval(state)
            )
            .orderBy(QInstitutionsUser.institutionsUser.id.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
        
        Long result = queryFactory
            .select(QInstitutionsUser.institutionsUser.count())
            .from(QInstitutionsUser.institutionsUser)
            .where(
                findInstitutionUserByIdOrName(keyword),
                QInstitutionsUser.institutionsUser.institution.id.eq(institutionId),
                checkApproval(state)
            )
            .fetchOne();

        Long count = result != null ? result : 0;

        return new PageImpl<>(institutionsUserList, pageable, count);
            
    }

    private BooleanExpression checkApproval(InstitutionUserState state) {
        return state != null ? QInstitutionsUser.institutionsUser.institutionState.eq(state) : null;

    }

    @Override
    public Page<Institution> forApprovalInstitution(Pageable pageable, String keyword, InstitutionApprovalState state) {
        
        List<Institution> institutionList = queryFactory
            .select(QInstitution.institution)
            .from(QInstitution.institution)
            .where(
                institutionStateEq(state),
                findInstitutionByIdOrName(keyword)
            )
            .orderBy(QInstitution.institution.id.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long result = queryFactory
            .select(QInstitution.institution.count())
            .from(QInstitution.institution)
            .where(
                institutionStateEq(state),
                findInstitutionByIdOrName(keyword)
            )
            .fetchOne();

        long count = result != null ? result : 0;

        return new PageImpl<>(institutionList, pageable, count);
    }

    private BooleanExpression institutionStateEq(InstitutionApprovalState state) {
        return state != null ? QInstitution.institution.institutionState.eq(state) : null;
    }

    private BooleanExpression dateRange(DateTimePath<LocalDateTime> dateTimePath, LocalDate startDate, LocalDate endDate) {

        if ((startDate == null) || (endDate == null)) {
            throw new IllegalArgumentException("시작일과 종료일을 모두 입력해 주세요.");
        }

        if (startDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작일은 종료일보다 늦을 수 없습니다.");
        }

        BooleanExpression condition = null;

        if (startDate != null) {
            condition = dateTimePath.goe(startDate.atStartOfDay());
        }

        if (endDate != null) {
            LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();
            BooleanExpression endCondition = dateTimePath.lt(endExclusive);

            condition = condition == null ? endCondition : condition.and(endCondition);
        }

        return condition;
    }

    @Override
    public Page<Care> findCare(Pageable pageable, String keyword) {

        List<Care> careList = queryFactory
            .select(QCare.care)
            .from(QCare.care)
            .leftJoin(QCare.care.wardUser, QWardUser.wardUser).fetchJoin()
            .leftJoin(QCare.care.guardUser, QGuardUser.guardUser).fetchJoin()
            .where(
                findCare(keyword),
                QCare.care.careState.eq(CareState.APPROVED)
            )
            .orderBy(QCare.care.id.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long result = queryFactory
            .select(QCare.care.count())
            .from(QCare.care)
            .where(
                findCare(keyword),
                QCare.care.careState.eq(CareState.APPROVED)
            )
            .fetchOne();

        long count = result != null ? result : 0;

        return new PageImpl<>(careList, pageable, count);
    }

    private BooleanExpression findCare(String keyword) {
        return StringUtils.hasText(keyword) ? QCare.care.wardUser.id.contains(keyword).or(QCare.care.guardUser.id.contains(keyword)) : null;
    }


}

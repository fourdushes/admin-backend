package tohear.hearo.admin.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import tohear.hearo.admin.dto.response.AdminGuardUserDto;
import tohear.hearo.admin.dto.response.AdminInstitutionsUserDto;
import tohear.hearo.admin.dto.response.AdminWardUserDto;
import tohear.hearo.archive.domain.Archive;
import tohear.hearo.archive.domain.DiseaseType;
import tohear.hearo.archive.domain.QArchive;
import tohear.hearo.archive.domain.QArchiveDisease;
import tohear.hearo.care.domain.Care;
import tohear.hearo.care.domain.CareState;
import tohear.hearo.care.domain.QCare;
import tohear.hearo.institution.domain.QInstitution;
import tohear.hearo.medicaltreatment.medicalrequest.domain.MedicalRequestStatus;
import tohear.hearo.medicaltreatment.medicalrequest.domain.QMedicalRequest;
import tohear.hearo.user.guardian.QGuardUser;
import tohear.hearo.user.institution.QInstitutionsUser;
import tohear.hearo.user.ward.QWardUser;

@RequiredArgsConstructor 
public class AdminUserRepositoryImpl implements AdminUserCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AdminWardUserDto> findWardUsers(Pageable pageable, String keyword, LocalDateTime startDate, LocalDateTime endDate) {

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
                findWardByDate(startDate, endDate)
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
                findWardByDate(startDate, endDate)
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

    // TODO: 목록 조회에서는 현재 페이지의 사용자 ID를 모아 카운트를 일괄 집계해 N+1 쿼리를 제거한다.
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

    private BooleanExpression findWardByDate(LocalDateTime startDate, LocalDateTime endDate) {
        return startDate != null && endDate != null ? 
            QWardUser.wardUser.joinDateTime.between(startDate, endDate) : null;
    }

    @Override
    public Page<AdminGuardUserDto> findGuardUsers(Pageable pageable, String keyword, LocalDateTime startDate, LocalDateTime endDate) {

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
                findGuardByIdOrName(keyword)
            )
            .orderBy(QGuardUser.guardUser.id.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long result = queryFactory
            .select(QGuardUser.guardUser.count())
            .from(QGuardUser.guardUser)
            .where(
                findGuardByIdOrName(keyword)
            )
            .fetchOne();
        
        long count = result != null ? result : 0L;

        return new PageImpl<>(guardUserList, pageable, count);
    }

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

    private BooleanExpression findGuardByDate(LocalDateTime startDate, LocalDateTime endDate) {
        return startDate != null && endDate != null ? 
            QGuardUser.guardUser.joinDateTime.between(startDate, endDate) : null;
    }

    @Override
    public Page<AdminInstitutionsUserDto> findInstitutionsUsers(Pageable pageable, String keyword, LocalDateTime startDate, LocalDateTime endDate) {

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
                findInstitutionByIdOrName(keyword)
            )
            .orderBy(QInstitutionsUser.institutionsUser.id.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long result = queryFactory
            .select(QInstitutionsUser.institutionsUser.count())
            .from(QInstitutionsUser.institutionsUser)
            .where(
                findInstitutionByIdOrName(keyword)
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

    private BooleanExpression findInstitutionByIdOrName(String keyword) {
        return StringUtils.hasText(keyword) ? QInstitutionsUser.institutionsUser.id.contains(keyword).or(QInstitutionsUser.institutionsUser.name.contains(keyword)) : null;
    }

    private BooleanExpression findInstitutionByDate(LocalDateTime startDate, LocalDateTime endDate) {
        return startDate != null && endDate != null ? 
            QInstitutionsUser.institutionsUser.joinDateTime.between(startDate, endDate) : null;
    }
    
    @Override
    public Page<Archive> findArchives(Pageable pageable, String userId, LocalDateTime startDate, LocalDateTime endDate) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findArchives'");
    }

    @Override
    public Page<Care> findCares(Pageable pageable, String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findCares'");
    }

    

    

    
}

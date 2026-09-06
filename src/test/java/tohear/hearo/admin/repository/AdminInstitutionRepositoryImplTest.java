package tohear.hearo.admin.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Function;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.querydsl.jpa.impl.JPAQueryFactory;

import tohear.hearo.admin.dto.response.AdminInstitutionDto;
import tohear.hearo.institution.domain.Institution;
import tohear.hearo.institution.domain.InstitutionApprovalState;
import tohear.hearo.institution.domain.InstitutionRegion;
import tohear.hearo.user.auth.domain.UserType;
import tohear.hearo.user.institution.InstitutionUserState;
import tohear.hearo.user.institution.InstitutionsUser;

class AdminInstitutionRepositoryImplTest {

    private static StandardServiceRegistry registry;
    private static SessionFactory sessionFactory;

    @BeforeAll
    static void setUpDatabase() {
        registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.driver_class", "org.h2.Driver")
                .applySetting("hibernate.connection.url", "jdbc:h2:mem:admin_institution_repository_test;DB_CLOSE_DELAY=-1")
                .applySetting("hibernate.hbm2ddl.auto", "create-drop")
                .build();

        try {
            sessionFactory = new MetadataSources(registry)
                    .addAnnotatedClass(Institution.class)
                    .addAnnotatedClass(InstitutionsUser.class)
                    .buildMetadata()
                    .buildSessionFactory();
        } catch (RuntimeException e) {
            StandardServiceRegistryBuilder.destroy(registry);
            throw e;
        }
    }

    @AfterAll
    static void closeDatabase() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    @Test
    void findInstitutionsFiltersByApprovalDateRegionAndStateAndPaginates() {
        String keyword = "기관검색-" + UUID.randomUUID();

        InstitutionIds ids = inTransaction(session -> {
            Institution first = institution(
                    keyword + "-이전",
                    InstitutionRegion.SEOUL,
                    InstitutionApprovalState.APPROVED,
                    LocalDateTime.of(2026, 8, 1, 0, 0));
            Institution last = institution(
                    keyword + "-최신",
                    InstitutionRegion.SEOUL,
                    InstitutionApprovalState.APPROVED,
                    LocalDateTime.of(2026, 8, 31, 23, 59, 59));
            Institution outsideEnd = institution(
                    keyword + "-기간밖",
                    InstitutionRegion.SEOUL,
                    InstitutionApprovalState.APPROVED,
                    LocalDateTime.of(2026, 9, 1, 0, 0));
            Institution otherRegion = institution(
                    keyword + "-경기",
                    InstitutionRegion.GYEONGGI,
                    InstitutionApprovalState.APPROVED,
                    LocalDateTime.of(2026, 8, 15, 12, 0));
            Institution pending = institution(
                    keyword + "-대기",
                    InstitutionRegion.SEOUL,
                    InstitutionApprovalState.PENDING,
                    LocalDateTime.of(2026, 8, 15, 12, 0));

            session.persist(first);
            session.persist(last);
            session.persist(outsideEnd);
            session.persist(otherRegion);
            session.persist(pending);

            return new InstitutionIds(first.getId(), last.getId());
        });

        Page<AdminInstitutionDto> firstPage = inTransaction(session -> repository(session).findInstitutions(
                PageRequest.of(0, 1),
                keyword,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31),
                InstitutionRegion.SEOUL));

        assertThat(firstPage.getTotalElements()).isEqualTo(2);
        assertThat(firstPage.getContent()).hasSize(1);
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(firstPage.getContent().getFirst().getId()).isEqualTo(ids.lastId());

        Page<AdminInstitutionDto> secondPage = inTransaction(session -> repository(session).findInstitutions(
                PageRequest.of(1, 1),
                keyword,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31),
                InstitutionRegion.SEOUL));

        assertThat(secondPage.getContent()).hasSize(1);
        assertThat(secondPage.getContent().getFirst().getId()).isEqualTo(ids.firstId());
    }

    @Test
    void findInstitutionsAllowsAllRegionsWhenRegionIsNull() {
        String keyword = "전체지역-" + UUID.randomUUID();

        inTransaction(session -> {
            session.persist(institution(
                    keyword + "-서울",
                    InstitutionRegion.SEOUL,
                    InstitutionApprovalState.APPROVED,
                    LocalDateTime.of(2026, 8, 10, 10, 0)));
            session.persist(institution(
                    keyword + "-경기",
                    InstitutionRegion.GYEONGGI,
                    InstitutionApprovalState.APPROVED,
                    LocalDateTime.of(2026, 8, 11, 10, 0)));
            return null;
        });

        Page<AdminInstitutionDto> result = inTransaction(session -> repository(session).findInstitutions(
                PageRequest.of(0, 10),
                keyword,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31),
                null));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(AdminInstitutionDto::getRegion)
                .containsExactlyInAnyOrder(InstitutionRegion.SEOUL, InstitutionRegion.GYEONGGI);
    }

    @Test
    void institutionsUserCountCountsOnlyUsersOfSelectedInstitution() {
        String suffix = UUID.randomUUID().toString();

        Long selectedInstitutionId = inTransaction(session -> {
            Institution selected = institution(
                    "인원조회기관-" + suffix,
                    InstitutionRegion.SEOUL,
                    InstitutionApprovalState.APPROVED,
                    LocalDateTime.of(2026, 8, 10, 10, 0));
            Institution other = institution(
                    "다른기관-" + suffix,
                    InstitutionRegion.BUSAN,
                    InstitutionApprovalState.APPROVED,
                    LocalDateTime.of(2026, 8, 10, 10, 0));
            session.persist(selected);
            session.persist(other);

            session.persist(institutionUser("selected-1-" + suffix, "선택기관 사용자1", selected));
            session.persist(institutionUser("selected-2-" + suffix, "선택기관 사용자2", selected));
            session.persist(institutionUser("other-1-" + suffix, "다른기관 사용자", other));

            return selected.getId();
        });

        Long count = inTransaction(session -> repository(session).InstitutionsUserCount(selectedInstitutionId));

        assertThat(count).isEqualTo(2L);
    }

    @Test
    void findInstitutionUserInIsFiltersByInstitutionKeywordAndStateAndPaginates() {
        String keyword = "target-" + UUID.randomUUID();

        Long selectedInstitutionId = inTransaction(session -> {
            Institution selected = institution(
                    "사용자조회기관-" + keyword,
                    InstitutionRegion.SEOUL,
                    InstitutionApprovalState.APPROVED,
                    LocalDateTime.of(2026, 8, 10, 10, 0));
            Institution other = institution(
                    "다른사용자조회기관-" + keyword,
                    InstitutionRegion.INCHEON,
                    InstitutionApprovalState.APPROVED,
                    LocalDateTime.of(2026, 8, 10, 10, 0));
            session.persist(selected);
            session.persist(other);

            InstitutionsUser first = institutionUser(keyword + "-001", "승인 사용자1", selected);
            InstitutionsUser second = institutionUser(keyword + "-002", "승인 사용자2", selected);
            InstitutionsUser pending = institutionUser(keyword + "-003", "대기 사용자", selected);
            InstitutionsUser otherInstitution = institutionUser(keyword + "-004", "다른 기관 사용자", other);
            first.approved();
            second.approved();
            otherInstitution.approved();

            session.persist(first);
            session.persist(second);
            session.persist(pending);
            session.persist(otherInstitution);

            return selected.getId();
        });

        Page<InstitutionsUser> result = inTransaction(session -> repository(session).findInstitutionUserInIs(
                PageRequest.of(0, 1),
                keyword,
                selectedInstitutionId,
                InstitutionUserState.APPROVED));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.getContent().getFirst().getId()).isEqualTo(keyword + "-001");
    }

    private Institution institution(
            String name,
            InstitutionRegion region,
            InstitutionApprovalState state,
            LocalDateTime approvalDate) {
        Institution institution = new Institution(
                name,
                UUID.randomUUID() + "@test.com",
                "login-" + UUID.randomUUID(),
                "pw");
        setField(institution, "region", region);
        setField(institution, "approvalDate", approvalDate);

        if (state == InstitutionApprovalState.APPROVED) {
            institution.approve();
        } else if (state == InstitutionApprovalState.REJECTED) {
            institution.reject();
        }

        return institution;
    }

    private InstitutionsUser institutionUser(String id, String name, Institution institution) {
        return new InstitutionsUser(
                id,
                name,
                id + "@test.com",
                "pw",
                UserType.INSTITUTIONS,
                institution);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("테스트 데이터 필드 설정에 실패했습니다: " + fieldName, e);
        }
    }

    private AdminUserRepositoryImpl repository(Session session) {
        return new AdminUserRepositoryImpl(new JPAQueryFactory(session));
    }

    private <T> T inTransaction(Function<Session, T> work) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                T result = work.apply(session);
                transaction.commit();
                return result;
            } catch (RuntimeException | Error e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
        }
    }

    private record InstitutionIds(Long firstId, Long lastId) {
    }
}

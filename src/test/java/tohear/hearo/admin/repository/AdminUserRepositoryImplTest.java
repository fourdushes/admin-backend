package tohear.hearo.admin.repository;

import java.util.UUID;
import java.util.function.Function;

import org.hibernate.Hibernate;
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

import tohear.hearo.care.domain.Care;
import tohear.hearo.care.domain.CareState;
import tohear.hearo.user.auth.domain.UserType;
import tohear.hearo.user.guardian.GuardUser;
import tohear.hearo.user.ward.WardUser;

import static org.assertj.core.api.Assertions.assertThat;

class AdminUserRepositoryImplTest {

    private static StandardServiceRegistry registry;
    private static SessionFactory sessionFactory;

    @BeforeAll
    static void setUpDatabase() {
        registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.driver_class", "org.h2.Driver")
                .applySetting("hibernate.connection.url", "jdbc:h2:mem:admin_user_repository_test;DB_CLOSE_DELAY=-1")
                .applySetting("hibernate.hbm2ddl.auto", "create-drop")
                .applySetting("hibernate.generate_statistics", "true")
                .build();

        try {
            sessionFactory = new MetadataSources(registry)
                    .addAnnotatedClass(WardUser.class)
                    .addAnnotatedClass(GuardUser.class)
                    .addAnnotatedClass(Care.class)
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
    void findWardCaresFetchJoinsApprovedGuardUsers() {
        TestUsers users = persistTestUsers();
        sessionFactory.getStatistics().clear();

        Page<Care> result = inTransaction(session -> repository(session).findWardCares(
                PageRequest.of(0, 10),
                users.wardId(),
                CareState.APPROVED));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);

        Care care = result.getContent().getFirst();
        assertThat(Hibernate.isInitialized(care.getGuardUser())).isTrue();
        assertThat(care.getGuardUser().getId()).isEqualTo(users.approvedGuardId());
        assertThat(care.getGuardUser().getName()).isEqualTo("승인 보호자");
        assertThat(sessionFactory.getStatistics().getPrepareStatementCount()).isEqualTo(2);
    }

    @Test
    void findGuardCaresFetchJoinsApprovedWardUsers() {
        TestUsers users = persistTestUsers();
        sessionFactory.getStatistics().clear();

        Page<Care> result = inTransaction(session -> repository(session).findGuardCares(
                PageRequest.of(0, 10),
                users.approvedGuardId(),
                CareState.APPROVED));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);

        Care care = result.getContent().getFirst();
        assertThat(Hibernate.isInitialized(care.getWardUser())).isTrue();
        assertThat(care.getWardUser().getId()).isEqualTo(users.wardId());
        assertThat(care.getWardUser().getName()).isEqualTo("피보호자");
        assertThat(sessionFactory.getStatistics().getPrepareStatementCount()).isEqualTo(2);
    }

    private TestUsers persistTestUsers() {
        return inTransaction(session -> {
            String suffix = UUID.randomUUID().toString();
            WardUser ward = new WardUser("ward-" + suffix, "피보호자", "ward@test.com", "pw", UserType.WARD);
            GuardUser approvedGuard = new GuardUser(
                    "approved-guard-" + suffix,
                    "승인 보호자",
                    "approved@test.com",
                    "pw",
                    UserType.GUARDIAN);
            GuardUser pendingGuard = new GuardUser(
                    "pending-guard-" + suffix,
                    "대기 보호자",
                    "pending@test.com",
                    "pw",
                    UserType.GUARDIAN);

            session.persist(ward);
            session.persist(approvedGuard);
            session.persist(pendingGuard);

            Care approvedCare = new Care(ward, approvedGuard);
            approvedCare.approve();
            session.persist(approvedCare);
            session.persist(new Care(ward, pendingGuard));

            return new TestUsers(ward.getId(), approvedGuard.getId());
        });
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

    private record TestUsers(String wardId, String approvedGuardId) {
    }
}

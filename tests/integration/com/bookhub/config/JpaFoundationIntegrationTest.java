package com.bookhub.config;

import com.bookhub.entity.Book;
import com.bookhub.entity.Category;
import com.bookhub.entity.RoleCode;
import com.bookhub.entity.User;
import com.bookhub.repository.BookRepository;
import com.bookhub.repository.CategoryRepository;
import com.bookhub.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Live SQL Server 2022 + Hibernate 6.6 (hbm2ddl.auto=validate) integration tests for 01-feat-foundation-jpa.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JpaFoundationIntegrationTest {

    private final UserRepository userRepository = new UserRepository();
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final BookRepository bookRepository = new BookRepository();

    @BeforeAll
    static void bootstrapJpa() {
        JpaUtil.shutdown();
        JpaUtil.init();
    }

    @AfterAll
    static void cleanupJpa() {
        JpaUtil.shutdown();
    }

    // @spec foundation-jpa/AC-01
    @Test
    @Order(1)
    void schemaContainsAllTwelveTablesAndFilteredIsbnIndex() {
        Set<String> expectedTables = new HashSet<>(Arrays.asList(
                "Roles", "Users", "Categories", "Books", "Carts", "CartItems",
                "Orders", "OrderItems", "Reviews", "Questions", "Answers", "PasswordResetTokens"));

        Set<String> actualTables = JpaUtil.executeReadOnly(em -> {
            @SuppressWarnings("unchecked")
            List<String> names = em.createNativeQuery(
                            "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES "
                                    + "WHERE TABLE_SCHEMA = 'dbo' AND TABLE_TYPE = 'BASE TABLE'")
                    .getResultList();
            return new HashSet<>(names);
        });

        assertTrue(actualTables.containsAll(expectedTables), "Missing expected tables: " + expectedTables);

        Number isbnIndexCount = JpaUtil.executeReadOnly(em -> (Number) em.createNativeQuery(
                        "SELECT COUNT(*) FROM sys.indexes "
                                + "WHERE name = 'UX_Books_isbn_not_null' AND is_unique = 1 AND has_filter = 1")
                .getSingleResult());
        assertEquals(1, isbnIndexCount.intValue(), "Filtered unique index UX_Books_isbn_not_null must exist");
    }

    // @spec foundation-jpa/AC-02
    @Test
    @Order(2)
    void seedDataPopulatesConsistentRolesCategoriesBooksBcryptAndCommerceRecords() {
        JpaUtil.executeReadOnly(em -> {
            Long roleCount = em.createQuery("SELECT COUNT(r) FROM Role r", Long.class).getSingleResult();
            assertEquals(2L, roleCount);

            List<Category> activeCategories = categoryRepository.findAllActive(em);
            assertTrue(activeCategories.size() >= 6, "Expected >= 6 active categories");

            long activeBooks = bookRepository.countActive(em);
            assertTrue(activeBooks >= 20, "Expected >= 20 active books");

            Optional<User> adminOpt = userRepository.findByEmail(em, "admin@bookhub.vn");
            assertTrue(adminOpt.isPresent(), "Seeded admin account must exist");
            User admin = adminOpt.get();
            assertEquals(RoleCode.ADMIN, admin.getRole().getRoleCode());
            assertTrue(admin.getPasswordHash().startsWith("$2a$"));
            assertTrue(BCrypt.checkpw("Admin@123", admin.getPasswordHash()));

            Optional<User> customerOpt = userRepository.findByEmail(em, "minhanh@bookhub.vn");
            assertTrue(customerOpt.isPresent(), "Seeded customer account must exist");
            User customer = customerOpt.get();
            assertEquals(RoleCode.CUSTOMER, customer.getRole().getRoleCode());
            assertTrue(BCrypt.checkpw("Customer@123", customer.getPasswordHash()));

            Long cartCount = em.createQuery("SELECT COUNT(c) FROM Cart c", Long.class).getSingleResult();
            Long cartItemCount = em.createQuery("SELECT COUNT(ci) FROM CartItem ci", Long.class).getSingleResult();
            Long orderCount = em.createQuery("SELECT COUNT(o) FROM Order o", Long.class).getSingleResult();
            Long orderItemCount = em.createQuery("SELECT COUNT(oi) FROM OrderItem oi", Long.class).getSingleResult();
            Long reviewCount = em.createQuery("SELECT COUNT(r) FROM Review r", Long.class).getSingleResult();
            Long questionCount = em.createQuery("SELECT COUNT(q) FROM Question q", Long.class).getSingleResult();
            Long answerCount = em.createQuery("SELECT COUNT(a) FROM Answer a", Long.class).getSingleResult();
            Long tokenCount = em.createQuery("SELECT COUNT(t) FROM PasswordResetToken t", Long.class).getSingleResult();
            assertTrue(cartCount >= 2);
            assertTrue(cartItemCount >= 3);
            assertTrue(orderCount >= 5);
            assertTrue(orderItemCount >= 10);
            assertTrue(reviewCount >= 3);
            assertTrue(questionCount >= 3);
            assertTrue(answerCount >= 2);
            assertTrue(tokenCount >= 1);
            return null;
        });
    }

    // @spec foundation-jpa/AC-03
    @Test
    @Order(3)
    void hibernateValidateBootstrapsAndReadsBooksAndCategoriesViaJpa() {
        assertTrue(JpaUtil.isInitialized());
        List<Book> firstPage = JpaUtil.executeReadOnly(em -> bookRepository.findActivePage(em, 0, 12));
        assertEquals(12, firstPage.size(), "Default catalog page size should return 12 active books");
        for (Book book : firstPage) {
            assertNotNull(book.getCategory(), "Category should be eagerly fetched in page query");
            assertNotNull(book.getCategory().getName());
            assertTrue(book.getPrice().compareTo(BigDecimal.ZERO) > 0);
        }
    }

    // @spec foundation-jpa/AC-06
    @Test
    @Order(4)
    void transactionHelperRollsBackAndClosesEntityManagerOnFailure() {
        String tempSlug = "rollback-test-" + UUID.randomUUID().toString().substring(0, 8);
        AtomicReference<EntityManager> capturedEm = new AtomicReference<>();

        assertThrows(IllegalStateException.class, () -> JpaUtil.executeInTransaction(em -> {
            capturedEm.set(em);
            Category cat = new Category();
            cat.setName("Rollback Test " + tempSlug);
            cat.setSlug(tempSlug);
            cat.setActive(true);
            categoryRepository.persist(em, cat);
            em.flush();
            throw new IllegalStateException("Simulated mid-transaction failure");
        }));

        assertNotNull(capturedEm.get());
        assertFalse(capturedEm.get().isOpen(), "EntityManager must be closed in finally after rollback");

        Optional<Category> persisted = JpaUtil.executeReadOnly(em -> categoryRepository.findBySlug(em, tempSlug));
        assertFalse(persisted.isPresent(), "Rolled back Category must not exist in BookHubDB");
    }

    // @spec foundation-jpa/AC-08
    @Test
    @Order(5)
    void filteredUniqueIsbnIndexAllowsMultipleNullsAndRejectsDuplicateNonNull() {
        Category anyCategory = JpaUtil.executeReadOnly(em -> categoryRepository.findAllActive(em).get(0));
        String marker = UUID.randomUUID().toString().substring(0, 8);
        Long nullBook1Id = null;
        Long nullBook2Id = null;

        try {
            nullBook1Id = JpaUtil.executeInTransaction(em -> {
                Category managedCat = em.getReference(Category.class, anyCategory.getCategoryId());
                Book b1 = createTempBook(managedCat, null, "Temp Null ISBN 1 " + marker);
                bookRepository.persist(em, b1);
                em.flush();
                return b1.getBookId();
            });
            nullBook2Id = JpaUtil.executeInTransaction(em -> {
                Category managedCat = em.getReference(Category.class, anyCategory.getCategoryId());
                Book b2 = createTempBook(managedCat, null, "Temp Null ISBN 2 " + marker);
                bookRepository.persist(em, b2);
                em.flush();
                return b2.getBookId();
            });
            assertNotNull(nullBook1Id);
            assertNotNull(nullBook2Id);

            assertThrows(PersistenceException.class, () -> JpaUtil.executeInTransaction(em -> {
                Category managedCat = em.getReference(Category.class, anyCategory.getCategoryId());
                Book duplicate = createTempBook(managedCat, "978-0132350884", "Dup ISBN " + marker);
                bookRepository.persist(em, duplicate);
                em.flush();
                return duplicate.getBookId();
            }));
        } finally {
            Long id1 = nullBook1Id;
            Long id2 = nullBook2Id;
            JpaUtil.executeInTransactionVoid(em -> {
                if (id1 != null) {
                    bookRepository.findById(em, id1).ifPresent(b -> bookRepository.remove(em, b));
                }
                if (id2 != null) {
                    bookRepository.findById(em, id2).ifPresent(b -> bookRepository.remove(em, b));
                }
            });
        }
    }

    // @spec foundation-jpa/AC-04
    @Test
    @Order(6)
    void shutdownClosesEntityManagerFactoryCleanly() {
        assertTrue(JpaUtil.isInitialized());
        JpaUtil.shutdown();
        assertFalse(JpaUtil.isInitialized(), "EntityManagerFactory must be closed after shutdown()");
        assertThrows(IllegalStateException.class, JpaUtil::getEntityManager);
    }

    private static Book createTempBook(Category category, String isbn, String title) {
        Book book = new Book();
        book.setCategory(category);
        book.setIsbn(isbn);
        book.setTitle(title);
        book.setAuthor("Integration Test Author");
        book.setPrice(new BigDecimal("99000.00"));
        book.setStockQty(5);
        book.setActive(false);
        return book;
    }
}

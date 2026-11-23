package com.bookhub.repository;

import com.bookhub.entity.Cart;
import com.bookhub.entity.CartItem;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository for dbo.Carts and dbo.CartItems with strict userId ownership filtering (H-06).
 */
public class CartRepository extends BaseRepository<Cart, Long> {

    public CartRepository() {
        super(Cart.class);
    }

    public Optional<Cart> findCartByUserId(EntityManager em, Long userId) {
        List<Cart> rows = em.createQuery(
                        "SELECT c FROM Cart c WHERE c.user.userId = :uid", Cart.class)
                .setParameter("uid", userId)
                .setMaxResults(1)
                .getResultList();
        return rows.stream().findFirst();
    }

    public List<CartItem> findItemsByUserId(EntityManager em, Long userId) {
        return em.createQuery(
                        "SELECT ci FROM CartItem ci JOIN FETCH ci.book b JOIN FETCH b.category "
                                + "WHERE ci.cart.user.userId = :uid ORDER BY ci.cartItemId ASC",
                        CartItem.class)
                .setParameter("uid", userId)
                .getResultList();
    }

    public Optional<CartItem> findItemOwnedByUser(EntityManager em, Long cartItemId, Long userId) {
        if (cartItemId == null || userId == null) {
            return Optional.empty();
        }
        List<CartItem> rows = em.createQuery(
                        "SELECT ci FROM CartItem ci JOIN FETCH ci.book b JOIN FETCH b.category "
                                + "WHERE ci.cartItemId = :ciId AND ci.cart.user.userId = :uid",
                        CartItem.class)
                .setParameter("ciId", cartItemId)
                .setParameter("uid", userId)
                .setMaxResults(1)
                .getResultList();
        return rows.stream().findFirst();
    }

    public Optional<CartItem> findItemByCartAndBook(EntityManager em, Long cartId, Long bookId) {
        List<CartItem> rows = em.createQuery(
                        "SELECT ci FROM CartItem ci WHERE ci.cart.cartId = :cid AND ci.book.bookId = :bid",
                        CartItem.class)
                .setParameter("cid", cartId)
                .setParameter("bid", bookId)
                .setMaxResults(1)
                .getResultList();
        return rows.stream().findFirst();
    }

    public int clearItemsByUserId(EntityManager em, Long userId) {
        return em.createQuery(
                        "DELETE FROM CartItem ci WHERE ci.cart.cartId IN "
                                + "(SELECT c.cartId FROM Cart c WHERE c.user.userId = :uid)")
                .setParameter("uid", userId)
                .executeUpdate();
    }

    public int countTotalQuantityByUserId(EntityManager em, Long userId) {
        Long sum = em.createQuery(
                        "SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.cart.user.userId = :uid",
                        Long.class)
                .setParameter("uid", userId)
                .getSingleResult();
        return sum != null ? sum.intValue() : 0;
    }
}

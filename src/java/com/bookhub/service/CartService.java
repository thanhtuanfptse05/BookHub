package com.bookhub.service;

import com.bookhub.config.AppConstants;
import com.bookhub.config.JpaUtil;
import com.bookhub.dto.CartItemDTO;
import com.bookhub.dto.CartViewDTO;
import com.bookhub.entity.Book;
import com.bookhub.entity.Cart;
import com.bookhub.entity.CartItem;
import com.bookhub.entity.User;
import com.bookhub.exception.AccessDeniedException;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.NotFoundException;
import com.bookhub.exception.OutOfStockException;
import com.bookhub.repository.BookRepository;
import com.bookhub.repository.CartRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages per-customer persistent Cart with server-side DB pricing and ownership checks (FR-01..03).
 */
public class CartService {

    private final CartRepository cartRepo;
    private final BookRepository bookRepo;

    public CartService() {
        this(new CartRepository(), new BookRepository());
    }

    public CartService(CartRepository cartRepo, BookRepository bookRepo) {
        this.cartRepo = cartRepo;
        this.bookRepo = bookRepo;
    }

    public CartViewDTO getCartView(Long userId) {
        BigDecimal shippingFee = resolveShippingFee();
        return JpaUtil.executeReadOnly(em -> {
            List<CartItem> entities = cartRepo.findItemsByUserId(em, userId);
            List<CartItemDTO> items = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            for (CartItem ci : entities) {
                Book b = ci.getBook();
                boolean available = b.isActive() && b.getCategory().isActive();
                if (!available) {
                    warnings.add("Sách \"" + b.getTitle() + "\" đã ngừng kinh doanh. Vui lòng xóa khỏi giỏ trước khi thanh toán.");
                } else if (b.getStockQty() < ci.getQuantity()) {
                    warnings.add("Sách \"" + b.getTitle() + "\" chỉ còn " + b.getStockQty() + " cuốn trong kho. Hãy giảm số lượng.");
                }
                items.add(new CartItemDTO(
                        ci.getCartItemId(), b.getBookId(), b.getTitle(), b.getAuthor(),
                        b.getCoverUrl(), b.getPrice(), b.getStockQty(), available, ci.getQuantity()));
            }
            return new CartViewDTO(items, shippingFee, warnings);
        });
    }

    public int addToCart(Long userId, Long bookId, int quantity) {
        if (bookId == null || quantity <= 0) {
            throw new BusinessException("Số lượng thêm vào giỏ phải là số nguyên dương.");
        }
        return JpaUtil.executeInTransaction(em -> {
            Book book = bookRepo.findActiveDetailById(em, bookId)
                    .orElseThrow(() -> new NotFoundException("Sách không tồn tại hoặc đã ngừng kinh doanh."));
            if (book.getStockQty() <= 0) {
                throw new OutOfStockException("Sách \"" + book.getTitle() + "\" hiện đã hết hàng.");
            }

            Cart cart = cartRepo.findCartByUserId(em, userId).orElseGet(() -> {
                Cart created = new Cart();
                created.setUser(em.getReference(User.class, userId));
                cartRepo.persist(em, created);
                em.flush();
                return created;
            });

            CartItem existing = cartRepo.findItemByCartAndBook(em, cart.getCartId(), bookId).orElse(null);
            int targetQty = existing == null ? quantity : existing.getQuantity() + quantity;
            if (targetQty > book.getStockQty()) {
                throw new OutOfStockException(
                        "Số lượng vượt quá tồn kho hiện còn (" + book.getStockQty() + " cuốn). Hãy giảm số lượng.");
            }

            if (existing == null) {
                CartItem item = new CartItem();
                item.setCart(cart);
                item.setBook(book);
                item.setQuantity(targetQty);
                em.persist(item);
            } else {
                existing.setQuantity(targetQty);
            }
            em.flush();
            return cartRepo.countTotalQuantityByUserId(em, userId);
        });
    }

    public int updateItemQuantity(Long userId, Long cartItemId, int newQuantity) {
        if (cartItemId == null || newQuantity <= 0) {
            throw new BusinessException("Số lượng phải lớn hơn hoặc bằng 1.");
        }
        return JpaUtil.executeInTransaction(em -> {
            CartItem item = cartRepo.findItemOwnedByUser(em, cartItemId, userId)
                    .orElseThrow(() -> new AccessDeniedException("Không tìm thấy sản phẩm trong giỏ hàng của bạn."));
            Book book = item.getBook();
            if (!book.isActive() || !book.getCategory().isActive()) {
                throw new BusinessException("Sách này đã ngừng kinh doanh. Vui lòng xóa khỏi giỏ hàng.");
            }
            if (newQuantity > book.getStockQty()) {
                throw new OutOfStockException(
                        "Số lượng vượt quá tồn kho hiện còn (" + book.getStockQty() + " cuốn). Hãy giảm số lượng.");
            }
            item.setQuantity(newQuantity);
            em.flush();
            return cartRepo.countTotalQuantityByUserId(em, userId);
        });
    }

    public int removeItem(Long userId, Long cartItemId) {
        return JpaUtil.executeInTransaction(em -> {
            CartItem item = cartRepo.findItemOwnedByUser(em, cartItemId, userId)
                    .orElseThrow(() -> new AccessDeniedException("Không tìm thấy sản phẩm trong giỏ hàng của bạn."));
            em.remove(item);
            em.flush();
            return cartRepo.countTotalQuantityByUserId(em, userId);
        });
    }

    public int clearCart(Long userId) {
        return JpaUtil.executeInTransaction(em -> {
            cartRepo.clearItemsByUserId(em, userId);
            return 0;
        });
    }

    public int countCartQuantity(Long userId) {
        return JpaUtil.executeReadOnly(em -> cartRepo.countTotalQuantityByUserId(em, userId));
    }

    static BigDecimal resolveShippingFee() {
        return JpaUtil.isInitialized()
                ? JpaUtil.getConfig().getShippingFee()
                : AppConstants.DEFAULT_SHIPPING_FEE;
    }
}

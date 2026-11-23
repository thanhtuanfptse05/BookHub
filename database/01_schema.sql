/* ============================================================================
   BookHub — 01_schema.sql
   Microsoft SQL Server 2019/2022 Schema (12 tables, PK/FK, UNIQUE, CHECK, Indexes)
   Spec: .sdd/specs/01-feat-foundation-jpa/SPEC.md (v1.0.0 LOCKED)
   Source: BookHub_PRJ301_Project_Plan_Requirements.md (§17.5)
   ============================================================================ */

IF DB_ID(N'BookHubDB') IS NULL
BEGIN
    CREATE DATABASE BookHubDB;
END
GO

USE BookHubDB;
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

/* 1. Roles (ADMIN, CUSTOMER — Guest là trạng thái chưa xác thực) */
CREATE TABLE dbo.Roles (
    role_id   INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Roles PRIMARY KEY,
    role_code VARCHAR(20)       NOT NULL CONSTRAINT UQ_Roles_role_code UNIQUE,
    role_name NVARCHAR(50)      NOT NULL,
    CONSTRAINT CK_Roles_code CHECK (role_code IN ('ADMIN', 'CUSTOMER'))
);
GO

/* 2. Users */
CREATE TABLE dbo.Users (
    user_id       BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Users PRIMARY KEY,
    role_id       INT                  NOT NULL,
    full_name     NVARCHAR(100)        NOT NULL,
    email         VARCHAR(255)         NOT NULL,
    phone         VARCHAR(20)          NULL,
    password_hash NVARCHAR(255)        NOT NULL,
    status        VARCHAR(20)          NOT NULL CONSTRAINT DF_Users_status  DEFAULT ('ACTIVE'),
    created_at    DATETIME2(0)         NOT NULL CONSTRAINT DF_Users_created DEFAULT (SYSDATETIME()),
    updated_at    DATETIME2(0)         NOT NULL CONSTRAINT DF_Users_updated DEFAULT (SYSDATETIME()),
    CONSTRAINT FK_Users_Roles  FOREIGN KEY (role_id) REFERENCES dbo.Roles(role_id),
    CONSTRAINT UQ_Users_email  UNIQUE (email),
    CONSTRAINT CK_Users_status CHECK (status IN ('ACTIVE', 'LOCKED', 'DISABLED'))
);
GO
CREATE INDEX IX_Users_role_status ON dbo.Users(role_id, status);
GO

/* 3. Categories */
CREATE TABLE dbo.Categories (
    category_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Categories PRIMARY KEY,
    name        NVARCHAR(80)      NOT NULL,
    slug        VARCHAR(100)      NOT NULL,
    description NVARCHAR(300)     NULL,
    is_active   BIT               NOT NULL CONSTRAINT DF_Categories_active  DEFAULT (1),
    created_at  DATETIME2(0)      NOT NULL CONSTRAINT DF_Categories_created DEFAULT (SYSDATETIME()),
    updated_at  DATETIME2(0)      NOT NULL CONSTRAINT DF_Categories_updated DEFAULT (SYSDATETIME()),
    CONSTRAINT UQ_Categories_name UNIQUE (name),
    CONSTRAINT UQ_Categories_slug UNIQUE (slug)
);
GO

/* 4. Books */
CREATE TABLE dbo.Books (
    book_id           BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Books PRIMARY KEY,
    category_id       INT                  NOT NULL,
    isbn              VARCHAR(20)          NULL,
    title             NVARCHAR(200)        NOT NULL,
    author            NVARCHAR(150)        NOT NULL,
    publisher         NVARCHAR(150)        NULL,
    published_year    SMALLINT             NULL,
    price             DECIMAL(18,2)        NOT NULL,
    stock_qty         INT                  NOT NULL CONSTRAINT DF_Books_stock    DEFAULT (0),
    short_description NVARCHAR(500)        NULL,
    description       NVARCHAR(MAX)        NULL,
    cover_url         NVARCHAR(500)        NULL,
    is_featured       BIT                  NOT NULL CONSTRAINT DF_Books_featured DEFAULT (0),
    is_active         BIT                  NOT NULL CONSTRAINT DF_Books_active   DEFAULT (1),
    created_at        DATETIME2(0)         NOT NULL CONSTRAINT DF_Books_created  DEFAULT (SYSDATETIME()),
    updated_at        DATETIME2(0)         NOT NULL CONSTRAINT DF_Books_updated  DEFAULT (SYSDATETIME()),
    CONSTRAINT FK_Books_Categories FOREIGN KEY (category_id) REFERENCES dbo.Categories(category_id),
    CONSTRAINT CK_Books_price      CHECK (price > 0),
    CONSTRAINT CK_Books_stock      CHECK (stock_qty >= 0),
    CONSTRAINT CK_Books_year       CHECK (published_year IS NULL OR (published_year BETWEEN 1000 AND 2200))
);
GO
CREATE UNIQUE INDEX UX_Books_isbn_not_null ON dbo.Books(isbn) WHERE isbn IS NOT NULL;
CREATE INDEX IX_Books_category_active      ON dbo.Books(category_id, is_active);
CREATE INDEX IX_Books_active_created       ON dbo.Books(is_active, created_at DESC);
CREATE INDEX IX_Books_active_price         ON dbo.Books(is_active, price);
CREATE INDEX IX_Books_active_featured      ON dbo.Books(is_active, is_featured, created_at DESC);
GO

/* 5. Carts (1 Customer có tối đa 1 Cart) */
CREATE TABLE dbo.Carts (
    cart_id    BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Carts PRIMARY KEY,
    user_id    BIGINT               NOT NULL,
    created_at DATETIME2(0)         NOT NULL CONSTRAINT DF_Carts_created DEFAULT (SYSDATETIME()),
    updated_at DATETIME2(0)         NOT NULL CONSTRAINT DF_Carts_updated DEFAULT (SYSDATETIME()),
    CONSTRAINT FK_Carts_Users FOREIGN KEY (user_id) REFERENCES dbo.Users(user_id),
    CONSTRAINT UQ_Carts_user  UNIQUE (user_id)
);
GO

/* 6. CartItems (mỗi Book xuất hiện tối đa 1 dòng trong 1 Cart) */
CREATE TABLE dbo.CartItems (
    cart_item_id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_CartItems PRIMARY KEY,
    cart_id      BIGINT               NOT NULL,
    book_id      BIGINT               NOT NULL,
    quantity     INT                  NOT NULL,
    created_at   DATETIME2(0)         NOT NULL CONSTRAINT DF_CartItems_created DEFAULT (SYSDATETIME()),
    updated_at   DATETIME2(0)         NOT NULL CONSTRAINT DF_CartItems_updated DEFAULT (SYSDATETIME()),
    CONSTRAINT FK_CartItems_Carts     FOREIGN KEY (cart_id) REFERENCES dbo.Carts(cart_id),
    CONSTRAINT FK_CartItems_Books     FOREIGN KEY (book_id) REFERENCES dbo.Books(book_id),
    CONSTRAINT UQ_CartItems_cart_book UNIQUE (cart_id, book_id),
    CONSTRAINT CK_CartItems_quantity  CHECK (quantity > 0)
);
GO

/* 7. Orders (COD-only trong MVP, shipping_fee snapshot, 5 trạng thái) */
CREATE TABLE dbo.Orders (
    order_id         BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Orders PRIMARY KEY,
    order_code       VARCHAR(30)          NOT NULL,
    user_id          BIGINT               NOT NULL,
    recipient_name   NVARCHAR(100)        NOT NULL,
    recipient_phone  VARCHAR(20)          NOT NULL,
    shipping_address NVARCHAR(500)        NOT NULL,
    note             NVARCHAR(500)        NULL,
    subtotal         DECIMAL(18,2)        NOT NULL,
    shipping_fee     DECIMAL(18,2)        NOT NULL,
    total_amount     DECIMAL(18,2)        NOT NULL,
    payment_method   VARCHAR(20)          NOT NULL CONSTRAINT DF_Orders_payment DEFAULT ('COD'),
    status           VARCHAR(20)          NOT NULL CONSTRAINT DF_Orders_status  DEFAULT ('PENDING'),
    created_at       DATETIME2(0)         NOT NULL CONSTRAINT DF_Orders_created DEFAULT (SYSDATETIME()),
    updated_at       DATETIME2(0)         NOT NULL CONSTRAINT DF_Orders_updated DEFAULT (SYSDATETIME()),
    cancelled_at     DATETIME2(0)         NULL,
    CONSTRAINT FK_Orders_Users        FOREIGN KEY (user_id) REFERENCES dbo.Users(user_id),
    CONSTRAINT UQ_Orders_order_code   UNIQUE (order_code),
    CONSTRAINT CK_Orders_subtotal     CHECK (subtotal >= 0),
    CONSTRAINT CK_Orders_shipping_fee CHECK (shipping_fee >= 0),
    CONSTRAINT CK_Orders_total        CHECK (total_amount >= 0 AND total_amount = subtotal + shipping_fee),
    CONSTRAINT CK_Orders_payment      CHECK (payment_method = 'COD'),
    CONSTRAINT CK_Orders_status       CHECK (status IN ('PENDING', 'CONFIRMED', 'SHIPPING', 'DELIVERED', 'CANCELLED')),
    CONSTRAINT CK_Orders_cancelled_at CHECK (
        (status = 'CANCELLED' AND cancelled_at IS NOT NULL)
        OR (status <> 'CANCELLED' AND cancelled_at IS NULL)
    )
);
GO
CREATE INDEX IX_Orders_user_created   ON dbo.Orders(user_id, created_at DESC);
CREATE INDEX IX_Orders_status_created ON dbo.Orders(status, created_at DESC);
GO

/* 8. OrderItems (Immutable Snapshot tên sách và đơn giá tại thời điểm mua) */
CREATE TABLE dbo.OrderItems (
    order_item_id       BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_OrderItems PRIMARY KEY,
    order_id            BIGINT               NOT NULL,
    book_id             BIGINT               NOT NULL,
    book_title_snapshot NVARCHAR(200)        NOT NULL,
    unit_price          DECIMAL(18,2)        NOT NULL,
    quantity            INT                  NOT NULL,
    line_total          DECIMAL(18,2)        NOT NULL,
    CONSTRAINT FK_OrderItems_Orders     FOREIGN KEY (order_id) REFERENCES dbo.Orders(order_id),
    CONSTRAINT FK_OrderItems_Books      FOREIGN KEY (book_id)  REFERENCES dbo.Books(book_id),
    CONSTRAINT CK_OrderItems_unit_price CHECK (unit_price > 0),
    CONSTRAINT CK_OrderItems_quantity   CHECK (quantity > 0),
    CONSTRAINT CK_OrderItems_line_total CHECK (line_total = unit_price * quantity)
);
GO
CREATE INDEX IX_OrderItems_order ON dbo.OrderItems(order_id);
CREATE INDEX IX_OrderItems_book  ON dbo.OrderItems(book_id);
GO

/* 9. Reviews (Tối đa 1 review / User / Book; rating 1..5; chỉ public khi APPROVED) */
CREATE TABLE dbo.Reviews (
    review_id  BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Reviews PRIMARY KEY,
    book_id    BIGINT               NOT NULL,
    user_id    BIGINT               NOT NULL,
    rating     TINYINT              NOT NULL,
    title      NVARCHAR(150)        NULL,
    content    NVARCHAR(2000)       NOT NULL,
    status     VARCHAR(20)          NOT NULL CONSTRAINT DF_Reviews_status  DEFAULT ('PENDING'),
    created_at DATETIME2(0)         NOT NULL CONSTRAINT DF_Reviews_created DEFAULT (SYSDATETIME()),
    updated_at DATETIME2(0)         NOT NULL CONSTRAINT DF_Reviews_updated DEFAULT (SYSDATETIME()),
    CONSTRAINT FK_Reviews_Books     FOREIGN KEY (book_id) REFERENCES dbo.Books(book_id),
    CONSTRAINT FK_Reviews_Users     FOREIGN KEY (user_id) REFERENCES dbo.Users(user_id),
    CONSTRAINT UQ_Reviews_user_book UNIQUE (user_id, book_id),
    CONSTRAINT CK_Reviews_rating    CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT CK_Reviews_status    CHECK (status IN ('PENDING', 'APPROVED', 'HIDDEN'))
);
GO
CREATE INDEX IX_Reviews_book_status ON dbo.Reviews(book_id, status, created_at DESC);
GO

/* 10. Questions (Customer hỏi theo sách; mặc định OPEN) */
CREATE TABLE dbo.Questions (
    question_id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Questions PRIMARY KEY,
    book_id     BIGINT               NOT NULL,
    user_id     BIGINT               NOT NULL,
    subject     NVARCHAR(150)        NOT NULL,
    content     NVARCHAR(1500)       NOT NULL,
    status      VARCHAR(20)          NOT NULL CONSTRAINT DF_Questions_status  DEFAULT ('OPEN'),
    created_at  DATETIME2(0)         NOT NULL CONSTRAINT DF_Questions_created DEFAULT (SYSDATETIME()),
    updated_at  DATETIME2(0)         NOT NULL CONSTRAINT DF_Questions_updated DEFAULT (SYSDATETIME()),
    CONSTRAINT FK_Questions_Books  FOREIGN KEY (book_id) REFERENCES dbo.Books(book_id),
    CONSTRAINT FK_Questions_Users  FOREIGN KEY (user_id) REFERENCES dbo.Users(user_id),
    CONSTRAINT CK_Questions_status CHECK (status IN ('OPEN', 'ANSWERED', 'HIDDEN'))
);
GO
CREATE INDEX IX_Questions_book_status    ON dbo.Questions(book_id, status, created_at DESC);
CREATE INDEX IX_Questions_status_created ON dbo.Questions(status, created_at DESC);
GO

/* 11. Answers (Tối đa 1 Answer / Question; chỉ Admin phản hồi — ADR-006) */
CREATE TABLE dbo.Answers (
    answer_id         BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Answers PRIMARY KEY,
    question_id       BIGINT               NOT NULL,
    responder_user_id BIGINT               NOT NULL,
    content           NVARCHAR(2000)       NOT NULL,
    created_at        DATETIME2(0)         NOT NULL CONSTRAINT DF_Answers_created DEFAULT (SYSDATETIME()),
    updated_at        DATETIME2(0)         NOT NULL CONSTRAINT DF_Answers_updated DEFAULT (SYSDATETIME()),
    CONSTRAINT FK_Answers_Questions FOREIGN KEY (question_id)       REFERENCES dbo.Questions(question_id),
    CONSTRAINT FK_Answers_Users     FOREIGN KEY (responder_user_id) REFERENCES dbo.Users(user_id),
    CONSTRAINT UQ_Answers_question  UNIQUE (question_id)
);
GO

/* 12. PasswordResetTokens (chỉ lưu SHA-256 hash; TTL 15 phút; dùng 1 lần) */
CREATE TABLE dbo.PasswordResetTokens (
    reset_id   BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_PasswordResetTokens PRIMARY KEY,
    user_id    BIGINT               NOT NULL,
    token_hash VARCHAR(128)         NOT NULL,
    expires_at DATETIME2(0)         NOT NULL,
    used_at    DATETIME2(0)         NULL,
    created_at DATETIME2(0)         NOT NULL CONSTRAINT DF_PasswordResetTokens_created DEFAULT (SYSDATETIME()),
    CONSTRAINT FK_PasswordResetTokens_Users FOREIGN KEY (user_id) REFERENCES dbo.Users(user_id),
    CONSTRAINT UQ_PasswordResetTokens_hash  UNIQUE (token_hash)
);
GO
CREATE INDEX IX_PasswordResetTokens_user_expiry ON dbo.PasswordResetTokens(user_id, expires_at);
GO

PRINT N'✔ BookHubDB schema created successfully (12 tables).';
GO

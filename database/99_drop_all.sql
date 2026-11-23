/* ============================================================================
   BookHub — 99_drop_all.sql
   Script dọn sạch bảng theo thứ tự ngược khóa ngoại (CHỈ DÙNG Ở LOCAL/TEST DB)
   Để dựng lại DB sạch: chạy 99_drop_all.sql → 01_schema.sql → 02_seed.sql
   ============================================================================ */

USE BookHubDB;
GO

DROP TABLE IF EXISTS dbo.PasswordResetTokens;
DROP TABLE IF EXISTS dbo.Answers;
DROP TABLE IF EXISTS dbo.Questions;
DROP TABLE IF EXISTS dbo.Reviews;
DROP TABLE IF EXISTS dbo.OrderItems;
DROP TABLE IF EXISTS dbo.Orders;
DROP TABLE IF EXISTS dbo.CartItems;
DROP TABLE IF EXISTS dbo.Carts;
DROP TABLE IF EXISTS dbo.Books;
DROP TABLE IF EXISTS dbo.Categories;
DROP TABLE IF EXISTS dbo.Users;
DROP TABLE IF EXISTS dbo.Roles;
GO

PRINT N'✔ Dropped all 12 BookHub tables (local reset complete).';
GO

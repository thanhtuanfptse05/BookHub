/* ============================================================================
   BookHub — 02_seed.sql
   Dữ liệu mẫu chuẩn hóa 100% logic nghiệp vụ (PRJ301 MVP)
   Chạy trên BookHubDB sạch sau 01_schema.sql.
   - Mật khẩu đều là BCrypt ($2a$10$) thật có salt riêng (tương thích jBCrypt 0.4 & Spring Crypto).
   - Chi tiết tài khoản demo xem database/README_DATABASE.md (không lưu plaintext trong bảng Users).
   ============================================================================ */

USE BookHubDB;
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

BEGIN TRANSACTION;

/* ---------------------------------------------------------------------------
   1. ROLES (2 rows)
   --------------------------------------------------------------------------- */
INSERT INTO dbo.Roles (role_code, role_name) VALUES
    ('ADMIN',    N'Quản trị viên'),
    ('CUSTOMER', N'Khách hàng');

DECLARE @role_admin    INT = (SELECT role_id FROM dbo.Roles WHERE role_code = 'ADMIN');
DECLARE @role_customer INT = (SELECT role_id FROM dbo.Roles WHERE role_code = 'CUSTOMER');

/* ---------------------------------------------------------------------------
   2. USERS (6 rows: 2 Admin ACTIVE, 3 Customer ACTIVE, 1 Customer LOCKED)
   BCrypt $2a$10$ hashes generated & verified (distinct random salt per user).
   --------------------------------------------------------------------------- */
INSERT INTO dbo.Users (role_id, full_name, email, phone, password_hash, status, created_at, updated_at) VALUES
    (@role_admin,    N'Quản trị viên BookHub', 'admin@bookhub.vn',       '0901000001', N'$2a$10$53PBw4DjBqLv5b2A2mv3R.IpReey5AMegxA/9br8Ou8dt09YFEssS', 'ACTIVE', '2026-10-01 08:00:00', '2026-10-01 08:00:00'),
    (@role_admin,    N'Trần Quốc Bảo',         'admin2@bookhub.vn',      '0901000002', N'$2a$10$ilrWQa8O4KuTAOjJZbz54.1.oyceoruinavj/coXDf3xwj8mfFUPa', 'ACTIVE', '2026-10-02 09:00:00', '2026-10-02 09:00:00'),
    (@role_customer, N'Nguyễn Minh Anh',       'minhanh@bookhub.vn',     '0912345678', N'$2a$10$/h6BSixOYoWkmD6jRZD35.Hy8tnfCCliZeIhgXzMZDVUeouP6Ixh6', 'ACTIVE', '2026-10-10 10:15:00', '2026-11-20 10:00:00'),
    (@role_customer, N'Lê Hoàng Nam',          'hoangnam@bookhub.vn',    '0987654321', N'$2a$10$FScRogFU19jlq42yCx87CeFIQ6d3jFgd3DuBMRcS6pwR/./uLsYOe', 'ACTIVE', '2026-10-12 14:20:00', '2026-11-18 10:15:00'),
    (@role_customer, N'Phạm Thu Hương',        'thuhuong@bookhub.vn',    '0933221100', N'$2a$10$MJpfN0kZBW9olEBVkz3hW.5.zussbfekUiqQkdTBKjCixHGhWqNmO', 'ACTIVE', '2026-11-01 09:30:00', '2026-11-01 09:30:00'),
    (@role_customer, N'Vũ Văn Khóa',           'locked.user@bookhub.vn', '0944556677', N'$2a$10$QjmVgO9lPN5OYIz0oNNy0u25XuyujZbEWZ3W12gtw5S1JsMMmgNZW', 'LOCKED', '2026-10-15 11:00:00', '2026-11-10 16:00:00');

DECLARE @u_admin1   BIGINT = (SELECT user_id FROM dbo.Users WHERE email = 'admin@bookhub.vn');
DECLARE @u_admin2   BIGINT = (SELECT user_id FROM dbo.Users WHERE email = 'admin2@bookhub.vn');
DECLARE @u_minhanh  BIGINT = (SELECT user_id FROM dbo.Users WHERE email = 'minhanh@bookhub.vn');
DECLARE @u_hoangnam BIGINT = (SELECT user_id FROM dbo.Users WHERE email = 'hoangnam@bookhub.vn');
DECLARE @u_thuhuong BIGINT = (SELECT user_id FROM dbo.Users WHERE email = 'thuhuong@bookhub.vn');

/* ---------------------------------------------------------------------------
   3. CATEGORIES (7 rows: 6 active khớp Figma + 1 inactive để test lọc/CRUD)
   --------------------------------------------------------------------------- */
INSERT INTO dbo.Categories (name, slug, description, is_active, created_at, updated_at) VALUES
    (N'Công nghệ',        'cong-nghe',       N'Sách lập trình, kiến trúc phần mềm, cơ sở dữ liệu và kỹ thuật máy tính.', 1, '2026-10-01 08:30:00', '2026-10-01 08:30:00'),
    (N'Kinh tế',          'kinh-te',         N'Tài chính cá nhân, đầu tư, khởi nghiệp và quản trị kinh doanh.',        1, '2026-10-01 08:31:00', '2026-10-01 08:31:00'),
    (N'Văn học',          'van-hoc',         N'Tiểu thuyết kinh điển và văn học Việt Nam, thế giới chọn lọc.',         1, '2026-10-01 08:32:00', '2026-10-01 08:32:00'),
    (N'Kỹ năng',          'ky-nang',         N'Phát triển bản thân, tư duy làm việc hiệu quả và giao tiếp.',           1, '2026-10-01 08:33:00', '2026-10-01 08:33:00'),
    (N'Ngoại ngữ',        'ngoai-ngu',       N'Tiếng Anh học thuật, luyện thi IELTS và tiếng Nhật sơ cấp.',            1, '2026-10-01 08:34:00', '2026-10-01 08:34:00'),
    (N'Thiếu nhi',        'thieu-nhi',       N'Truyện dài, truyện ngắn và tác phẩm kinh điển dành cho tuổi thơ.',      1, '2026-10-01 08:35:00', '2026-10-01 08:35:00'),
    (N'Sách lưu trữ cũ',  'sach-luu-tru-cu', N'Danh mục đã ngừng hoạt động dùng kiểm thử BR-CAT-001 & BR-CAT-002.',    0, '2026-10-01 08:36:00', '2026-10-20 09:00:00');

DECLARE @cat_tech     INT = (SELECT category_id FROM dbo.Categories WHERE slug = 'cong-nghe');
DECLARE @cat_econ     INT = (SELECT category_id FROM dbo.Categories WHERE slug = 'kinh-te');
DECLARE @cat_lit      INT = (SELECT category_id FROM dbo.Categories WHERE slug = 'van-hoc');
DECLARE @cat_skill    INT = (SELECT category_id FROM dbo.Categories WHERE slug = 'ky-nang');
DECLARE @cat_lang     INT = (SELECT category_id FROM dbo.Categories WHERE slug = 'ngoai-ngu');
DECLARE @cat_kids     INT = (SELECT category_id FROM dbo.Categories WHERE slug = 'thieu-nhi');
DECLARE @cat_archived INT = (SELECT category_id FROM dbo.Categories WHERE slug = 'sach-luu-tru-cu');

/* ---------------------------------------------------------------------------
   4. BOOKS (26 rows)
   - 24 sách active trong 6 danh mục active (4 cuốn/danh mục = đủ 2 trang × 12)
   - 8 sách is_featured = 1 (đủ 8 ô "Sách nổi bật" ở SCR-01)
   - Giá active trải từ 45.000 ₫ (MIN) đến 320.000 ₫ (MAX)
   - 1 sách hết hàng stock_qty = 0 (bk4 — test SCR-03b & TC-CAT-07)
   - 1 sách tồn kho đúng 1 bản (bk3 — test double-checkout concurrency)
   - 2 sách ISBN = NULL (bk8, bk12 — test UX_Books_isbn_not_null)
   - 1 sách is_active = 0 (bk25 — giá 15.000 ₫, đã bán trong đơn cũ: test TC-ADM-04, TC-CAT-06, TC-REP-03)
   - 1 sách active nằm trong danh mục inactive (bk26 — test lọc danh mục active)
   --------------------------------------------------------------------------- */
INSERT INTO dbo.Books (
    category_id, isbn, title, author, publisher, published_year,
    price, stock_qty, short_description, description, cover_url,
    is_featured, is_active, created_at, updated_at
) VALUES
    /* Công nghệ (4 active + 1 inactive) */
    (@cat_tech, '978-0132350884', N'Clean Code: Mã sạch và Con đường trở thành Lập trình viên giỏi', N'Robert C. Martin', N'NXB Dân Trí', 2022,
     189000.00, 14, N'Cẩm nang viết mã nguồn rõ ràng, dễ bảo trì và tái cấu trúc hiệu quả cho kỹ sư phần mềm.',
     N'Cuốn sách trình bày nguyên tắc đặt tên, thiết kế hàm nhỏ gọn, xử lý ngoại lệ, kiểm thử đơn vị và nhận diện code smell kèm nhiều ví dụ Java thực tế.',
     '/assets/images/books/bk-01.jpg', 1, 1, '2026-11-22 10:00:00', '2026-11-22 10:00:00'),

    (@cat_tech, '978-1449373320', N'Designing Data-Intensive Applications', N'Martin Kleppmann', N'O''Reilly Media', 2021,
     320000.00, 8, N'Kiến trúc hệ thống dữ liệu tin cậy, mở rộng tốt và dễ bảo trì.',
     N'Phân tích sâu mô hình lưu trữ, chỉ mục, sao chép dữ liệu, phân mảnh, transaction và xử lý luồng trong các hệ thống phân tán hiện đại.',
     '/assets/images/books/bk-02.jpg', 1, 1, '2026-11-21 10:00:00', '2026-11-21 10:00:00'),

    (@cat_tech, '978-0134685991', N'Effective Java (Ấn bản 3)', N'Joshua Bloch', N'Addison-Wesley', 2020,
     245000.00, 1, N'90 quy tắc thực chiến giúp viết mã Java an toàn, rõ ràng và tối ưu hiệu năng.',
     N'Tổng hợp các mẫu thiết kế, generics, lambda, concurrency và serialization dành cho lập trình viên Java 17 trở lên. (Tồn kho 1 cuốn để kiểm thử đặt hàng đồng thời.)',
     '/assets/images/books/bk-03.jpg', 0, 1, '2026-11-20 10:00:00', '2026-11-20 10:00:00'),

    (@cat_tech, '978-1492078005', N'Head First Design Patterns', N'Eric Freeman & Elisabeth Robson', N'O''Reilly Media', 2021,
     215000.00, 0, N'Học mẫu thiết kế hướng đối tượng trực quan bằng ví dụ sinh động và Java.',
     N'Giải thích Strategy, Observer, Decorator, Factory, Singleton, Command và MVC qua tình huống thực tế dễ nhớ. (Hiện tạm hết hàng để kiểm thử SCR-03b.)',
     '/assets/images/books/bk-04.jpg', 0, 1, '2026-11-19 10:00:00', '2026-11-19 10:00:00'),

    /* Kinh tế (4 active) */
    (@cat_econ, '978-0857197689', N'Tâm lý học về tiền', N'Morgan Housel', N'NXB Dân Trí', 2023,
     125000.00, 22, N'19 câu chuyện ngắn giải thích cách con người suy nghĩ và ra quyết định tài chính.',
     N'Quản lý tài chính cá nhân không chỉ phụ thuộc vào kiến thức toán học mà phần lớn đến từ hành vi, kiên nhẫn và cách nhìn nhận rủi ro dài hạn.',
     '/assets/images/books/bk-05.jpg', 1, 1, '2026-11-18 10:00:00', '2026-11-18 10:00:00'),

    (@cat_econ, '978-0060555665', N'Nhà đầu tư thông minh', N'Benjamin Graham', N'NXB Thế Giới', 2022,
     210000.00, 11, N'Tác phẩm nền tảng về đầu tư giá trị và biên độ an toàn.',
     N'Hướng dẫn nhà đầu tư phân biệt đầu tư với đầu cơ, quản trị cảm xúc trước biến động thị trường và xây dựng danh mục bền vững.',
     '/assets/images/books/bk-06.jpg', 0, 1, '2026-11-17 10:00:00', '2026-11-17 10:00:00'),

    (@cat_econ, '978-0307887894', N'Khởi nghiệp tinh gọn', N'Eric Ries', N'NXB Tổng Hợp TP.HCM', 2021,
     148000.00, 16, N'Phương pháp xây dựng MVP, đo lường nhanh và học hỏi có kiểm chứng.',
     N'Cung cấp vòng lặp Xây dựng – Đo lường – Học hỏi giúp đội ngũ sản phẩm giảm lãng phí và điều chỉnh hướng đi kịp thời.',
     '/assets/images/books/bk-07.jpg', 1, 1, '2026-11-16 10:00:00', '2026-11-16 10:00:00'),

    (@cat_econ, NULL, N'Kinh tế học hài hước', N'Steven D. Levitt & Stephen J. Dubner', N'NXB Lao Động', 2020,
     115000.00, 19, N'Góc nhìn kinh tế học bất ngờ đằng sau các hiện tượng đời thường.',
     N'Sử dụng dữ liệu thực nghiệm để phân tích động lực ẩn giấu trong giáo dục, kinh doanh và đời sống xã hội. (ISBN NULL để kiểm thử filtered index.)',
     '/assets/images/books/bk-08.jpg', 0, 1, '2026-11-15 10:00:00', '2026-11-15 10:00:00'),

    /* Văn học (4 active) */
    (@cat_lit, '978-6043651591', N'Nhà giả kim', N'Paulo Coelho', N'NXB Hội Nhà Văn', 2023,
     79000.00, 35, N'Hành trình theo đuổi vận mệnh và lắng nghe trái tim của chàng chăn cừu Santiago.',
     N'Tác phẩm văn học giàu chất triết lý nhẹ nhàng, truyền cảm hứng về lòng can đảm bước ra khỏi vùng an toàn để thực hiện ước mơ.',
     '/assets/images/books/bk-09.jpg', 1, 1, '2026-11-14 10:00:00', '2026-11-14 10:00:00'),

    (@cat_lit, '978-6041142756', N'Cho tôi xin một vé đi tuổi thơ', N'Nguyễn Nhật Ánh', N'NXB Trẻ', 2022,
     85000.00, 28, N'Những trang văn trong trẻo gợi nhớ kỷ niệm hồn nhiên của tuổi nhỏ.',
     N'Qua lời kể hóm hỉnh của cậu bé Mùi, cuốn sách đưa người đọc trở lại thế giới bạn bè, gia đình và những suy nghĩ ngộ nghĩnh thuở thơ ấu.',
     '/assets/images/books/bk-10.jpg', 1, 1, '2026-11-13 10:00:00', '2026-11-13 10:00:00'),

    (@cat_lit, '978-6043440126', N'Rừng Na Uy', N'Haruki Murakami', N'NXB Hội Nhà Văn', 2021,
     135000.00, 12, N'Bản tình ca trầm lắng về tuổi trẻ, mất mát và sự trưởng thành tại Tokyo.',
     N'Câu chuyện hồi tưởng đầy cảm xúc của nhân vật Watanabe với nhịp kể tinh tế và âm hưởng cổ điển đặc trưng của Murakami.',
     '/assets/images/books/bk-11.jpg', 0, 1, '2026-11-12 10:00:00', '2026-11-12 10:00:00'),

    (@cat_lit, NULL, N'Số đỏ', N'Vũ Trọng Phụng', N'NXB Văn Học', 2020,
     62000.00, 20, N'Kiệt tác tiểu thuyết trào phúng bậc nhất của văn học Việt Nam hiện đại.',
     N'Chân dung xã hội thành thị đầu thế kỷ XX qua nhân vật Xuân Tóc Đỏ với ngòi bút châm biếm sắc sảo. (ISBN NULL #2 để kiểm thử filtered index.)',
     '/assets/images/books/bk-12.jpg', 0, 1, '2026-11-11 10:00:00', '2026-11-11 10:00:00'),

    /* Kỹ năng (4 active) */
    (@cat_skill, '978-6043354829', N'Đắc Nhân Tâm', N'Dale Carnegie', N'NXB Tổng Hợp TP.HCM', 2023,
     86000.00, 40, N'Nghệ thuật ứng xử, lắng nghe và tạo thiện cảm bền vững trong giao tiếp.',
     N'Tổng hợp những nguyên tắc ứng xử kinh điển giúp cải thiện mối quan hệ gia đình, học tập và công việc.',
     '/assets/images/books/bk-13.jpg', 1, 1, '2026-11-10 10:00:00', '2026-11-10 10:00:00'),

    (@cat_skill, '978-0735211292', N'Thói quen nguyên tử (Atomic Habits)', N'James Clear', N'NXB Thế Giới', 2023,
     165000.00, 25, N'Thay đổi nhỏ 1% mỗi ngày tạo nên kết quả vượt trội lâu dài.',
     N'Trình bày khung 4 quy luật xây dựng thói quen tốt và loại bỏ thói quen xấu dựa trên tâm lý học hành vi. (Giá mới 165.000 ₫; đơn cũ giữ snapshot 150.000 ₫.)',
     '/assets/images/books/bk-14.jpg', 1, 1, '2026-11-09 10:00:00', '2026-11-15 09:00:00'),

    (@cat_skill, '978-1455586691', N'Deep Work: Làm ra làm, chơi ra chơi', N'Cal Newport', N'NXB Thế Giới', 2022,
     139000.00, 18, N'Rèn luyện khả năng tập trung sâu trong thế giới đầy xao nhãng.',
     N'Đưa ra chiến lược sắp xếp lịch làm việc, quản lý công cụ số và tạo ra giá trị chuyên môn cao trong thời gian ngắn hơn.',
     '/assets/images/books/bk-15.jpg', 0, 1, '2026-11-08 10:00:00', '2026-11-08 10:00:00'),

    (@cat_skill, '978-1982137274', N'7 Thói quen hiệu quả', N'Stephen R. Covey', N'NXB Tổng Hợp TP.HCM', 2021,
     175000.00, 15, N'Bảy nguyên tắc phát triển bản thân và lãnh đạo dựa trên giá trị cốt lõi.',
     N'Từ chủ động, bắt đầu bằng mục tiêu cuối cùng đến tư duy cùng thắng và rèn mới bản thân mỗi ngày.',
     '/assets/images/books/bk-16.jpg', 0, 1, '2026-11-07 10:00:00', '2026-11-07 10:00:00'),

    /* Ngoại ngữ (4 active) */
    (@cat_lang, '978-1108457651', N'English Grammar in Use (5th Edition)', N'Raymond Murphy', N'Cambridge University Press', 2022,
     195000.00, 21, N'Giáo trình tự học ngữ pháp tiếng Anh trình độ trung cấp kèm bài tập thực hành.',
     N'Mỗi bài gồm trang lý thuyết súc tích bên trái và bài tập vận dụng bên phải, phù hợp sinh viên đại học và người đi làm.',
     '/assets/images/books/bk-17.jpg', 0, 1, '2026-11-06 10:00:00', '2026-11-06 10:00:00'),

    (@cat_lang, '978-6043728910', N'Hackers IELTS Reading', N'David Cho', N'NXB Thế Giới', 2023,
     168000.00, 13, N'Chiến thuật đọc hiểu theo từng dạng câu hỏi IELTS Academic.',
     N'Cung cấp lộ trình luyện từ cơ bản đến nâng cao kèm phân tích chi tiết bẫy thường gặp và bảng từ vựng học thuật.',
     '/assets/images/books/bk-18.jpg', 0, 1, '2026-11-05 10:00:00', '2026-11-05 10:00:00'),

    (@cat_lang, '978-4883196036', N'Minna no Nihongo Sơ cấp 1', N'3A Corporation', N'NXB Trẻ', 2021,
     98000.00, 24, N'Bộ giáo trình tiếng Nhật sơ cấp tiêu chuẩn N5 với hội thoại và bài tập mẫu.',
     N'Xây dựng nền tảng từ vựng, mẫu câu cơ bản và kỹ năng giao tiếp tiếng Nhật hằng ngày.',
     '/assets/images/books/bk-19.jpg', 0, 1, '2026-11-04 10:00:00', '2026-11-04 10:00:00'),

    (@cat_lang, '978-6043182040', N'Luyện phát âm tiếng Anh chuẩn Mỹ', N'Ann Cook', N'NXB Hồng Đức', 2020,
     112000.00, 17, N'Hướng dẫn khẩu hình, trọng âm, nối âm và ngữ điệu tự nhiên.',
     N'Giúp người học khắc phục lỗi phát âm phổ biến của người Việt và nâng cao kỹ năng nghe nói rõ rệt.',
     '/assets/images/books/bk-20.jpg', 0, 1, '2026-11-03 10:00:00', '2026-11-03 10:00:00'),

    /* Thiếu nhi (4 active) */
    (@cat_kids, '978-6042231091', N'Dế Mèn phiêu lưu ký', N'Tô Hoài', N'NXB Kim Đồng', 2022,
     55000.00, 30, N'Chuyến phiêu lưu kỳ thú qua thế giới loài vật của chú Dế Mèn.',
     N'Tác phẩm kinh điển giàu bài học về tình bạn, lòng nhân ái và tinh thần khám phá thế giới rộng lớn.',
     '/assets/images/books/bk-21.jpg', 0, 1, '2026-11-02 10:00:00', '2026-11-02 10:00:00'),

    (@cat_kids, '978-6043409154', N'Hoàng tử bé', N'Antoine de Saint-Exupéry', N'NXB Hội Nhà Văn', 2023,
     68000.00, 26, N'Câu chuyện diệu kỳ về tình bạn giữa phi công và cậu bé đến từ tiểu hành tinh B-612.',
     N'Tác phẩm minh họa tinh tế dành cho thiếu nhi và cả người lớn từng là trẻ con.',
     '/assets/images/books/bk-22.jpg', 0, 1, '2026-11-01 10:00:00', '2026-11-01 10:00:00'),

    (@cat_kids, '978-6042219808', N'Kính vạn hoa — Tập 1', N'Nguyễn Nhật Ánh', N'NXB Kim Đồng', 2021,
     95000.00, 18, N'Những câu chuyện học trò vui nhộn của bộ ba Quý Ròm, Tiểu Long và nhỏ Hạnh.',
     N'Tập mở đầu bộ truyện thiếu nhi kinh điển tái hiện sinh động đời sống học sinh Việt Nam.',
     '/assets/images/books/bk-23.jpg', 0, 1, '2026-10-30 10:00:00', '2026-10-30 10:00:00'),

    (@cat_kids, '978-6043521108', N'Chuyện con mèo dạy hải âu bay', N'Luis Sepúlveda', N'NXB Hội Nhà Văn', 2022,
     45000.00, 22, N'Lời hứa cảm động của chú mèo mập Zorba dành cho cô hải âu nhỏ Lucky.',
     N'Câu chuyện ấm áp về trách nhiệm, tình yêu thương vượt qua sự khác biệt và bảo vệ thiên nhiên.',
     '/assets/images/books/bk-24.jpg', 0, 1, '2026-10-28 10:00:00', '2026-10-28 10:00:00'),

    /* Sách ngừng bán (is_active = 0, đã có đơn DELIVERED lịch sử, giá 15.000 ₫ để test TC-REP-03 / TC-ADM-04 / TC-CAT-06) */
    (@cat_tech, '978-0000000025', N'Lập trình Servlet 2.5 (Ấn bản cũ đã ngừng bán)', N'Tác giả Lưu trữ', N'NXB Kỹ Thuật', 2015,
     15000.00, 5, N'Tài liệu cũ đã ngừng kinh doanh (dùng kiểm thử soft-deactivate và lọc public).',
     N'Bản ghi dùng kiểm thử BR-CAT-001, BR-CAT-005 (is_featured=1 nhưng is_active=0 vẫn ẩn), TC-ADM-04 và TC-REP-03.',
     '/assets/images/books/bk-25.jpg', 1, 0, '2026-10-05 10:00:00', '2026-11-13 10:00:00'),

    /* Sách active nằm trong danh mục inactive (test BR-CAT-001 lọc cả Categories.is_active = 1) */
    (@cat_archived, '978-0000000026', N'Tuyển tập tài liệu tham khảo cũ', N'Ban Biên Tập', N'NXB Lưu Trữ', 2018,
     70000.00, 9, N'Sách thuộc danh mục đã ngừng hoạt động (phải ẩn trên storefront public).',
     N'Dùng kiểm thử quy tắc sách chỉ hiển thị public khi cả Books.is_active = 1 và Categories.is_active = 1.',
     '/assets/images/books/bk-26.jpg', 0, 1, '2026-10-06 10:00:00', '2026-10-20 09:00:00');

DECLARE @bk1  BIGINT = (SELECT book_id FROM dbo.Books WHERE isbn = '978-0132350884');
DECLARE @bk2  BIGINT = (SELECT book_id FROM dbo.Books WHERE isbn = '978-1449373320');
DECLARE @bk5  BIGINT = (SELECT book_id FROM dbo.Books WHERE isbn = '978-0857197689');
DECLARE @bk6  BIGINT = (SELECT book_id FROM dbo.Books WHERE isbn = '978-0060555665');
DECLARE @bk7  BIGINT = (SELECT book_id FROM dbo.Books WHERE isbn = '978-0307887894');
DECLARE @bk9  BIGINT = (SELECT book_id FROM dbo.Books WHERE isbn = '978-6043651591');
DECLARE @bk10 BIGINT = (SELECT book_id FROM dbo.Books WHERE isbn = '978-6041142756');
DECLARE @bk11 BIGINT = (SELECT book_id FROM dbo.Books WHERE isbn = '978-6043440126');
DECLARE @bk13 BIGINT = (SELECT book_id FROM dbo.Books WHERE isbn = '978-6043354829');
DECLARE @bk14 BIGINT = (SELECT book_id FROM dbo.Books WHERE isbn = '978-0735211292');
DECLARE @bk15 BIGINT = (SELECT book_id FROM dbo.Books WHERE isbn = '978-1455586691');
DECLARE @bk16 BIGINT = (SELECT book_id FROM dbo.Books WHERE isbn = '978-1982137274');
DECLARE @bk17 BIGINT = (SELECT book_id FROM dbo.Books WHERE isbn = '978-1108457651');
DECLARE @bk18 BIGINT = (SELECT book_id FROM dbo.Books WHERE isbn = '978-6043728910');
DECLARE @bk22 BIGINT = (SELECT book_id FROM dbo.Books WHERE isbn = '978-6043409154');
DECLARE @bk25 BIGINT = (SELECT book_id FROM dbo.Books WHERE isbn = '978-0000000025');

/* ---------------------------------------------------------------------------
   5. CARTS & CARTITEMS
   - Minh Anh: 1 giỏ với 3 dòng (4 cuốn, khớp badge Figma = 3 dòng / 4 sản phẩm)
   - Hoàng Nam: 1 giỏ với 1 dòng (dùng test IDOR TC-CART-06)
   - Thu Hương: chưa có giỏ (dùng test Empty Cart & tạo cart mới)
   --------------------------------------------------------------------------- */
INSERT INTO dbo.Carts (user_id, created_at, updated_at) VALUES
    (@u_minhanh,  '2026-11-20 09:00:00', '2026-11-22 18:30:00'),
    (@u_hoangnam, '2026-11-21 10:00:00', '2026-11-21 10:00:00');

DECLARE @cart_minhanh  BIGINT = (SELECT cart_id FROM dbo.Carts WHERE user_id = @u_minhanh);
DECLARE @cart_hoangnam BIGINT = (SELECT cart_id FROM dbo.Carts WHERE user_id = @u_hoangnam);

INSERT INTO dbo.CartItems (cart_id, book_id, quantity, created_at, updated_at) VALUES
    (@cart_minhanh,  @bk1,  1, '2026-11-22 18:10:00', '2026-11-22 18:10:00'),
    (@cart_minhanh,  @bk5,  2, '2026-11-22 18:20:00', '2026-11-22 18:20:00'),
    (@cart_minhanh,  @bk15, 1, '2026-11-22 18:30:00', '2026-11-22 18:30:00'),
    (@cart_hoangnam, @bk6,  1, '2026-11-21 10:05:00', '2026-11-21 10:05:00');

/* ---------------------------------------------------------------------------
   6. ORDERS (8 đơn hàng phủ ĐỦ 5 trạng thái) & ORDERITEMS (Immutable Snapshots)
   Phí ship cố định 30.000 ₫ · COD · total_amount = subtotal + shipping_fee
   Doanh thu 4 đơn DELIVERED = 824.000 + 650.000 + 249.000 + 723.000 = 2.446.000 ₫
   AOV (DELIVERED) = 2.446.000 / 4 = 611.500 ₫
   --------------------------------------------------------------------------- */
INSERT INTO dbo.Orders (
    order_code, user_id, recipient_name, recipient_phone, shipping_address, note,
    subtotal, shipping_fee, total_amount, payment_method, status,
    created_at, updated_at, cancelled_at
) VALUES
    ('BH-20261105-00001', @u_minhanh,  N'Nguyễn Minh Anh', '0912345678', N'Số 12 Phố Duy Tân, Cầu Giấy, Hà Nội',        N'Giao giờ hành chính',
     794000.00, 30000.00, 824000.00, 'COD', 'DELIVERED', '2026-11-05 09:15:00', '2026-11-08 15:00:00', NULL),

    ('BH-20261110-00002', @u_minhanh,  N'Nguyễn Minh Anh', '0912345678', N'Số 12 Phố Duy Tân, Cầu Giấy, Hà Nội',        N'Gọi trước 15 phút khi giao',
     620000.00, 30000.00, 650000.00, 'COD', 'DELIVERED', '2026-11-10 14:30:00', '2026-11-13 11:20:00', NULL),

    ('BH-20261112-00003', @u_hoangnam, N'Lê Hoàng Nam',    '0987654321', N'245 Nguyễn Thị Minh Khai, Quận 1, TP.HCM',   NULL,
     219000.00, 30000.00, 249000.00, 'COD', 'DELIVERED', '2026-11-12 10:05:00', '2026-11-15 16:45:00', NULL),

    ('BH-20261115-00004', @u_hoangnam, N'Lê Hoàng Nam',    '0987654321', N'245 Nguyễn Thị Minh Khai, Quận 1, TP.HCM',   N'Gói chống sốc cẩn thận',
     693000.00, 30000.00, 723000.00, 'COD', 'DELIVERED', '2026-11-15 16:40:00', '2026-11-18 14:10:00', NULL),

    ('BH-20261119-00005', @u_minhanh,  N'Nguyễn Minh Anh', '0912345678', N'Số 12 Phố Duy Tân, Cầu Giấy, Hà Nội',        NULL,
     195000.00, 30000.00, 225000.00, 'COD', 'SHIPPING',  '2026-11-19 11:20:00', '2026-11-21 09:00:00', NULL),

    ('BH-20261121-00006', @u_minhanh,  N'Nguyễn Minh Anh', '0912345678', N'Số 12 Phố Duy Tân, Cầu Giấy, Hà Nội',        N'Giao cuối tuần càng tốt',
     271000.00, 30000.00, 301000.00, 'COD', 'CONFIRMED', '2026-11-21 15:10:00', '2026-11-22 08:30:00', NULL),

    ('BH-20261122-00007', @u_minhanh,  N'Nguyễn Minh Anh', '0912345678', N'Số 12 Phố Duy Tân, Cầu Giấy, Hà Nội',        NULL,
     175000.00, 30000.00, 205000.00, 'COD', 'PENDING',   '2026-11-22 20:05:00', '2026-11-22 20:05:00', NULL),

    ('BH-20261118-00008', @u_hoangnam, N'Lê Hoàng Nam',    '0987654321', N'245 Nguyễn Thị Minh Khai, Quận 1, TP.HCM',   N'Đặt nhầm sách, khách yêu cầu hủy',
     336000.00, 30000.00, 366000.00, 'COD', 'CANCELLED', '2026-11-18 08:50:00', '2026-11-18 10:15:00', '2026-11-18 10:15:00');

DECLARE @ord1 BIGINT = (SELECT order_id FROM dbo.Orders WHERE order_code = 'BH-20261105-00001');
DECLARE @ord2 BIGINT = (SELECT order_id FROM dbo.Orders WHERE order_code = 'BH-20261110-00002');
DECLARE @ord3 BIGINT = (SELECT order_id FROM dbo.Orders WHERE order_code = 'BH-20261112-00003');
DECLARE @ord4 BIGINT = (SELECT order_id FROM dbo.Orders WHERE order_code = 'BH-20261115-00004');
DECLARE @ord5 BIGINT = (SELECT order_id FROM dbo.Orders WHERE order_code = 'BH-20261119-00005');
DECLARE @ord6 BIGINT = (SELECT order_id FROM dbo.Orders WHERE order_code = 'BH-20261121-00006');
DECLARE @ord7 BIGINT = (SELECT order_id FROM dbo.Orders WHERE order_code = 'BH-20261122-00007');
DECLARE @ord8 BIGINT = (SELECT order_id FROM dbo.Orders WHERE order_code = 'BH-20261118-00008');

INSERT INTO dbo.OrderItems (order_id, book_id, book_title_snapshot, unit_price, quantity, line_total) VALUES
    /* Đơn 1 — DELIVERED (subtotal = 794.000) */
    (@ord1, @bk1,  N'Clean Code: Mã sạch và Con đường trở thành Lập trình viên giỏi', 189000.00, 2, 378000.00),
    (@ord1, @bk13, N'Đắc Nhân Tâm',                                                    86000.00, 3, 258000.00),
    (@ord1, @bk9,  N'Nhà giả kim',                                                     79000.00, 2, 158000.00),

    /* Đơn 2 — DELIVERED (subtotal = 620.000; bk14 giữ snapshot giá cũ 150.000 ≠ 165.000 hiện tại!) */
    (@ord2, @bk14, N'Thói quen nguyên tử (Atomic Habits)',                            150000.00, 2, 300000.00),
    (@ord2, @bk2,  N'Designing Data-Intensive Applications',                          320000.00, 1, 320000.00),

    /* Đơn 3 — DELIVERED (subtotal = 219.000; gồm bk25 đã soft-deactivate để test TC-ADM-04) */
    (@ord3, @bk1,  N'Clean Code: Mã sạch và Con đường trở thành Lập trình viên giỏi', 189000.00, 1, 189000.00),
    (@ord3, @bk25, N'Lập trình Servlet 2.5 (Ấn bản cũ đã ngừng bán)',                  15000.00, 2,  30000.00),

    /* Đơn 4 — DELIVERED (subtotal = 693.000) */
    (@ord4, @bk5,  N'Tâm lý học về tiền',                                             125000.00, 3, 375000.00),
    (@ord4, @bk10, N'Cho tôi xin một vé đi tuổi thơ',                                  85000.00, 2, 170000.00),
    (@ord4, @bk7,  N'Khởi nghiệp tinh gọn',                                           148000.00, 1, 148000.00),

    /* Đơn 5 — SHIPPING (subtotal = 195.000) */
    (@ord5, @bk17, N'English Grammar in Use (5th Edition)',                           195000.00, 1, 195000.00),

    /* Đơn 6 — CONFIRMED (subtotal = 271.000) */
    (@ord6, @bk11, N'Rừng Na Uy',                                                     135000.00, 1, 135000.00),
    (@ord6, @bk22, N'Hoàng tử bé',                                                     68000.00, 2, 136000.00),

    /* Đơn 7 — PENDING (subtotal = 175.000) */
    (@ord7, @bk16, N'7 Thói quen hiệu quả',                                           175000.00, 1, 175000.00),

    /* Đơn 8 — CANCELLED (subtotal = 336.000) */
    (@ord8, @bk18, N'Hackers IELTS Reading',                                          168000.00, 2, 336000.00);

/* ---------------------------------------------------------------------------
   7. REVIEWS (6 rows — 100% thỏa BR-REV-001: đều nằm trong đơn DELIVERED của đúng user!)
   - 3 APPROVED (Clean Code: 5★ + 4★ = TB 4.5★; Đắc Nhân Tâm: 5★)
   - 2 PENDING  (hiện hàng chờ duyệt Admin SCR-14 / SCR-20)
   - 1 HIDDEN   (bị ẩn, không tính vào rating TB)
   - Chừa sẵn (minhanh, bk2) & (hoangnam, bk10/bk7) đã DELIVERED chưa review để test TC-REV-01!
   --------------------------------------------------------------------------- */
INSERT INTO dbo.Reviews (book_id, user_id, rating, title, content, status, created_at, updated_at) VALUES
    (@bk1,  @u_minhanh,  5, N'Rất đáng đọc cho sinh viên CNTT',
     N'Sách in sắc nét, ví dụ Java rõ ràng giúp mình sửa được rất nhiều thói quen đặt tên và tách hàm gọn hơn hẳn.',
     'APPROVED', '2026-11-09 10:00:00', '2026-11-09 14:00:00'),

    (@bk1,  @u_hoangnam, 4, N'Nội dung chuẩn mực, giao hàng nhanh',
     N'Đóng gói chống sốc tốt, nội dung kinh điển nên có trên kệ sách lập trình. Một vài chương hơi dài nhưng rất thực tế.',
     'APPROVED', '2026-11-16 09:30:00', '2026-11-16 11:00:00'),

    (@bk13, @u_minhanh,  5, N'Sách kinh điển dễ áp dụng',
     N'Đọc lại vẫn thấy nhiều bài học hữu ích trong giao tiếp hằng ngày và làm việc nhóm.',
     'APPROVED', '2026-11-09 10:15:00', '2026-11-09 14:05:00'),

    (@bk14, @u_minhanh,  4, N'Khung xây thói quen dễ thực hành',
     N'Mình áp dụng quy tắc 2 phút thấy tiến bộ rõ rệt sau hai tuần. Đang chờ quản trị viên duyệt hiển thị.',
     'PENDING',  '2026-11-14 19:00:00', '2026-11-14 19:00:00'),

    (@bk5,  @u_hoangnam, 5, N'Góc nhìn tài chính rất gần gũi',
     N'Văn phong dễ hiểu, không khô khan công thức. Rất phù hợp người mới tìm hiểu quản lý tài chính cá nhân.',
     'PENDING',  '2026-11-19 20:10:00', '2026-11-19 20:10:00'),

    (@bk9,  @u_minhanh,  2, N'Nội dung spam thử nghiệm ẩn',
     N'Đánh giá mẫu trạng thái HIDDEN để kiểm thử tính năng ẩn đánh giá vi phạm của quản trị viên.',
     'HIDDEN',   '2026-11-09 10:25:00', '2026-11-10 08:00:00');

/* ---------------------------------------------------------------------------
   8. QUESTIONS (6 rows) & ANSWERS (3 rows — 100% thỏa BR-QA)
   - 3 ANSWERED ↔ đúng 3 Answers do ADMIN phản hồi
   - 2 OPEN     ↔ 0 Answers (hiện hàng chờ SCR-14 / SCR-21)
   - 1 HIDDEN   ↔ 0 Answers (ẩn khỏi public SCR-03)
   --------------------------------------------------------------------------- */
INSERT INTO dbo.Questions (book_id, user_id, subject, content, status, created_at, updated_at) VALUES
    (@bk1,  @u_minhanh,  N'Bản này có phải bản dịch tiếng Việt đầy đủ không?',
     N'Cho mình hỏi cuốn Clean Code này là bản tiếng Việt trọn bộ 17 chương và có bìa mềm hay bìa cứng ạ?',
     'ANSWERED', '2026-11-04 08:20:00', '2026-11-04 09:10:00'),

    (@bk1,  @u_hoangnam, N'Giao hàng vào TP.HCM mất khoảng mấy ngày?',
     N'Mình ở Quận 1 TP.HCM đặt COD thì thường bao lâu nhận được sách và có được kiểm tra bìa trước khi thanh toán không?',
     'ANSWERED', '2026-11-11 13:00:00', '2026-11-11 14:15:00'),

    (@bk14, @u_thuhuong, N'Sách có kèm bảng theo dõi thói quen không?',
     N'Shop cho mình hỏi ấn bản năm 2023 này có tặng kèm bookmark hoặc tờ mẫu theo dõi thói quen cuối sách không ạ?',
     'ANSWERED', '2026-11-16 15:40:00', '2026-11-16 17:00:00'),

    (@bk2,  @u_minhanh,  N'Sách là bản tiếng Anh gốc hay bản dịch?',
     N'Cho mình hỏi cuốn Designing Data-Intensive Applications là bản nhập khẩu tiếng Anh hay bản tiếng Việt ạ?',
     'OPEN',     '2026-11-22 09:00:00', '2026-11-22 09:00:00'),

    (@bk5,  @u_hoangnam, N'Mua số lượng 5 cuốn có sẵn kho giao ngay không?',
     N'Nhóm mình muốn đặt 5 cuốn Tâm lý học về tiền cho câu lạc bộ đọc sách, shop có sẵn hàng giao trong tuần này không ạ?',
     'OPEN',     '2026-11-22 14:45:00', '2026-11-22 14:45:00'),

    (@bk9,  @u_hoangnam, N'Câu hỏi quảng cáo không phù hợp (mẫu HIDDEN)',
     N'Nội dung thử nghiệm trạng thái HIDDEN — không được hiển thị công khai trên trang chi tiết sách.',
     'HIDDEN',   '2026-11-17 10:00:00', '2026-11-17 11:30:00');

DECLARE @q1 BIGINT = (SELECT question_id FROM dbo.Questions WHERE subject = N'Bản này có phải bản dịch tiếng Việt đầy đủ không?');
DECLARE @q2 BIGINT = (SELECT question_id FROM dbo.Questions WHERE subject = N'Giao hàng vào TP.HCM mất khoảng mấy ngày?');
DECLARE @q3 BIGINT = (SELECT question_id FROM dbo.Questions WHERE subject = N'Sách có kèm bảng theo dõi thói quen không?');

INSERT INTO dbo.Answers (question_id, responder_user_id, content, created_at, updated_at) VALUES
    (@q1, @u_admin1, N'Chào bạn, đây là bản dịch tiếng Việt chính thức của NXB Dân Trí (bìa mềm, trọn bộ 17 chương + phụ lục). Sách hiện có sẵn tại kho BookHub.', '2026-11-04 09:10:00', '2026-11-04 09:10:00'),
    (@q2, @u_admin1, N'Chào bạn, đơn giao nội thành TP.HCM thường mất 2–3 ngày làm việc với phí vận chuyển cố định 30.000 ₫ (COD). Bạn được đồng kiểm ngoại quan sách trước khi thanh toán ạ.', '2026-11-11 14:15:00', '2026-11-11 14:15:00'),
    (@q3, @u_admin2, N'Chào bạn, ấn bản 2023 có phần phụ lục hướng dẫn lập bảng theo dõi thói quen ở cuối sách và đi kèm 1 bookmark của BookHub ạ.', '2026-11-16 17:00:00', '2026-11-16 17:00:00');

/* ---------------------------------------------------------------------------
   9. PASSWORD RESET TOKENS (2 rows demo: 1 đã dùng + 1 đã hết hạn — test TC-AUTH-10)
   Chỉ lưu SHA-256 hash, không mở sẵn token còn hiệu lực trong seed tĩnh.
   --------------------------------------------------------------------------- */
INSERT INTO dbo.PasswordResetTokens (user_id, token_hash, expires_at, used_at, created_at) VALUES
    (@u_minhanh,  'd4547ad4eb8421c5942723e218e8a7ebdb2b0bc7af64d7f136aef7041cd69048', '2026-11-01 10:15:00', '2026-11-01 10:06:00', '2026-11-01 10:00:00'),
    (@u_hoangnam, '2ac25937b6d7a0e9f6cb7aa68e0875265d3c9956c894cf8357a1c1161afe4a8d', '2026-11-02 15:15:00', NULL,                  '2026-11-02 15:00:00');

COMMIT TRANSACTION;
PRINT N'✔ BookHubDB seed data inserted cleanly and consistently.';
GO

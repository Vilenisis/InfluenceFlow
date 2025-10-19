INSERT INTO tg_user (telegram_id, username, is_admin) VALUES
    (1001, 'admin_manager', TRUE),
    (2001, 'creator_anna', FALSE),
    (2002, 'creator_oleg', FALSE),
    (2003, 'creator_mila', FALSE),
    (2004, 'creator_dan', FALSE),
    (2005, 'creator_lena', FALSE);

INSERT INTO creator (tg_user_id, full_name, email, niche, platform_handle) VALUES
    (2, 'Анна Петрова', 'anna@example.com', 'lifestyle', '@annagram'),
    (3, 'Олег Смирнов', 'oleg@example.com', 'tech', '@techoleg'),
    (4, 'Мила Иванова', 'mila@example.com', 'travel', '@milatravel'),
    (5, 'Данил Ким', 'dan@example.com', 'gaming', '@dangame'),
    (6, 'Елена Ли', 'lena@example.com', 'food', '@lenacooks');

INSERT INTO campaign (name, description, start_date, end_date, reward_per_submission) VALUES
    ('Весенний запуск', 'Промо весенней коллекции аксессуаров', '2024-03-01', '2024-06-01', 3500),
    ('Tech Boost', 'Обзор новых гаджетов бренда', '2024-02-10', '2024-05-15', 5000),
    ('Taste It', 'Продвижение серии соусов', '2024-01-15', '2024-04-20', 2800),
    ('Travel Dream', 'Конкурс путешествий по Европе', '2024-03-20', '2024-07-31', 4000),
    ('Game Fest', 'Партнёрка с турниром по киберспорту', '2024-04-01', '2024-08-01', 4500);

INSERT INTO task (campaign_id, title, brief, platform, payout_amount, deadline) VALUES
    (1, 'Reels про аксессуары', 'Показать 3 любимых образа', 'instagram', 3500, '2024-05-01'),
    (1, 'TikTok челлендж', 'Снять участие в танцевальном челлендже', 'tiktok', 3600, '2024-05-10'),
    (2, 'Обзор смартфона', 'Рассказать про флагманский смартфон', 'youtube', 5200, '2024-04-30'),
    (3, 'Домашний рецепт', 'Приготовить блюдо с использованием соусов', 'instagram', 3000, '2024-04-10'),
    (4, 'Путеводитель по городу', 'Поделиться маршрутом выходного дня', 'instagram', 4000, '2024-06-15'),
    (5, 'Стрим матча', 'Поделиться впечатлениями о турнире', 'tiktok', 4700, '2024-07-20');

INSERT INTO submission (task_id, creator_id, url, status, submitted_at) VALUES
    (1, 1, 'https://www.instagram.com/reel/ABC123xyz/', 'APPROVED', '2024-03-21 10:00:00'),
    (2, 2, 'https://www.tiktok.com/@techoleg/video/1234567890/', 'APPROVED', '2024-03-22 14:30:00'),
    (3, 2, 'https://www.youtube.com/shorts/hGf456AbC/', 'PENDING', '2024-03-25 09:45:00'),
    (4, 5, 'https://www.instagram.com/reel/COOK987654/', 'REJECTED', '2024-03-18 12:10:00'),
    (5, 3, 'https://www.instagram.com/reel/TRAVEL777/', 'APPROVED', '2024-03-24 08:20:00');

INSERT INTO post_metric (submission_id, views, likes, comments, reported_at) VALUES
    (1, 12000, 560, 45, '2024-03-22 10:00:00'),
    (2, 25000, 1230, 130, '2024-03-23 12:00:00'),
    (3, 9000, 430, 50, '2024-03-26 15:00:00'),
    (4, 3000, 120, 15, '2024-03-19 10:00:00'),
    (5, 15000, 780, 90, '2024-03-25 11:30:00');

INSERT INTO payout (creator_id, total_amount, created_at, status) VALUES
    (1, 3500, '2024-03-30 09:00:00', 'PAID'),
    (2, 8600, '2024-03-31 09:00:00', 'PAID'),
    (3, 4000, '2024-04-01 09:00:00', 'PLANNED'),
    (4, 0, '2024-03-20 09:00:00', 'PLANNED'),
    (5, 3000, '2024-03-27 09:00:00', 'PAID');

INSERT INTO payout_item (payout_id, submission_id, amount) VALUES
    (1, 1, 3500),
    (2, 2, 3600),
    (2, 3, 5000),
    (3, 5, 4000),
    (5, 4, 3000);

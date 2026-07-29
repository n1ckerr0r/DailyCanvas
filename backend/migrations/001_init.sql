CREATE TABLE IF NOT EXISTS users (
    id TEXT PRIMARY KEY,
    email TEXT NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS user_settings (
    user_id TEXT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    selected_collections TEXT[] NOT NULL,
    notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    notification_time TEXT NOT NULL DEFAULT '20:00',
    notification_time_zone TEXT NOT NULL DEFAULT 'Europe/Moscow'
);

CREATE TABLE IF NOT EXISTS artworks (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    artist_id TEXT NOT NULL,
    artist_name TEXT NOT NULL,
    country TEXT NOT NULL,
    collection TEXT NOT NULL,
    year_value INTEGER NOT NULL,
    year_label TEXT NOT NULL,
    image_path TEXT NOT NULL,
    description TEXT NOT NULL,
    facts TEXT[] NOT NULL DEFAULT '{}',
    published_on DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS artwork_tags (
    artwork_id TEXT NOT NULL REFERENCES artworks(id) ON DELETE CASCADE,
    tag_id TEXT NOT NULL,
    tag_name TEXT NOT NULL,
    tag_type TEXT NOT NULL,
    PRIMARY KEY (artwork_id, tag_id)
);

CREATE TABLE IF NOT EXISTS favorites (
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    artwork_id TEXT NOT NULL REFERENCES artworks(id) ON DELETE CASCADE,
    added_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, artwork_id)
);

CREATE TABLE IF NOT EXISTS artwork_views (
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    artwork_id TEXT NOT NULL REFERENCES artworks(id) ON DELETE CASCADE,
    viewed_on DATE NOT NULL,
    viewed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, artwork_id, viewed_on)
);

INSERT INTO users (id, email, created_at)
VALUES ('user-1', 'user@example.com', '2026-07-02T12:30:00Z')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_settings (user_id, selected_collections, notifications_enabled, notification_time, notification_time_zone)
VALUES ('user-1', ARRAY['russian', 'world'], TRUE, '20:00', 'Europe/Moscow')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO artworks (id, title, artist_id, artist_name, country, collection, year_value, year_label, image_path, description, facts, published_on) VALUES
('the-scream', 'Крик', 'edvard-munch', 'Эдвард Мунк', 'Норвегия', 'world', 1893, '1893', 'the-scream.jpg', $s$
Картина стала одним из самых узнаваемых образов тревоги в европейском искусстве. На полотне Мунк превращает личный страх и ощущение катастрофы в универсальный символ человеческого состояния.
$s$, ARRAY[
    'Первоначальное название работы - "Крик природы".',
    'Мунк создал четыре версии картины: две маслом и две пастелью.',
    'Одна из версий была продана в 2012 году примерно за 120 миллионов долларов США.'
], '2026-07-24'),
('holy-rus', 'Святая Русь', 'mikhail-nesterov', 'Михаил Нестеров', 'Россия', 'russian', 1901, '1901-1905', 'holy-rus.jpg', $s$
Монументальное религиозно-философское полотно Нестерова соединяет северный пейзаж, народную тему и евангельский мотив. Художник показывал в нем духовный поиск, который связывал с образом русской земли.
$s$, ARRAY[
    'У картины есть второе название: "Придите ко Мне все труждающиеся и обремененные, и Я успокою вас".',
    'Впервые работа была показана на персональной выставке художника 5 января 1907 года в Санкт-Петербурге.',
    'Сегодня картина хранится в Государственном Русском музее.'
], '2026-07-23'),
('nighthawks', 'Полуночники', 'edward-hopper', 'Эдвард Хоппер', 'США', 'world', 1942, '1942', 'nighthawks.jpg', $s$
На картине изображены ночные посетители закусочной, собранные в холодном и почти театральном свете. Работа часто читается как образ одиночества большого города и тревожного состояния Америки военного времени.
$s$, ARRAY[
    'Прототипом закусочной стало кафе возле дома художника в районе Гринвич-Виллидж.',
    'Хоппер начал работу над полотном после атаки на Перл-Харбор.',
    'Картина находится в Институте искусств Чикаго.'
], '2026-07-22'),
('blame-escape', 'Бегство от критики', 'pere-borrell-del-caso', 'Пере Боррель дель Казо', 'Испания', 'world', 1874, '1874', 'blame-escape.png', $s$
Известный пример живописного trompe-l''oeil, в котором мальчик буквально вырывается за пределы холста и рамы. Работа часто используется как образ столкновения искусства и зрительской оценки.
$s$, ARRAY[
    'Это одна из самых известных работ художника.',
    'Полотно регулярно участвует в выставках, посвященных обманчивой перспективе.'
], '2026-07-21'),
('american-gothic', 'Американская готика', 'grant-wood', 'Грант Вуд', 'США', 'world', 1930, '1930', 'american-gothic.jpg', $s$
Вуд написал дом в Элдоне и персонажей, которые, по его представлению, могли бы в нем жить. Со временем картина превратилась из спорного образа американской провинции в символ стойкости и характера эпохи Великой депрессии.
$s$, ARRAY[
    'Для образа дочери позировала сестра художника.',
    'Для фигуры фермера позировал стоматолог Байрон Маккиби.',
    'Картина хранится в Чикагском институте искусств.'
], '2026-07-20'),
('the-laundress', 'Прачка', 'henri-de-toulouse-lautrec', 'Анри де Тулуз-Лотрек', 'Франция', 'world', 1889, '1889', 'the-laundress.jpg', $s$
Относительно спокойный портрет в творчестве Лотрека, более известного сценами кабаре и городской ночной жизни. В центре внимания - живая модель и бытовой, почти немедийный мотив.
$s$, ARRAY[
    'Моделью стала Кармен Гаудин, одна из любимых моделей художника.',
    'Такие сдержанные портреты редки для Лотрека.',
    'Травмы юности сильно повлияли на внешний облик и биографию художника.'
], '2026-07-19'),
('christinas-world', 'Мир Кристины', 'andrew-wyeth', 'Эндрю Уайет', 'США', 'world', 1948, '1948', 'christinas-world.jpg', $s$
Уайет написал свою соседку Кристину Олсон, которая смотрит на дом и преодолевает пространство без привычной физической опоры. Картина стала одной из самых известных работ американского искусства XX века.
$s$, ARRAY[
    'Реальной героиней была Кристина Олсон, страдавшая тяжелым неврологическим заболеванием.',
    'Фигуру для картины позировала жена художника.',
    'Работа хранится в Музее современного искусства в Нью-Йорке.'
], '2026-07-18'),
('young-decadent', 'Юная декадентка', 'ramon-casas', 'Рамон Касас', 'Испания', 'world', 1899, '1899', 'young-decadent.jpg', $s$
Касас строит образ уединенного размышления после светского вечера: зритель видит девушку в расслабленной позе и как будто вмешивается в ее личную паузу. Полотно известно и под названием "После танца".
$s$, ARRAY[
    'Моделью стала Мадлен Буагильом.',
    'Картина также известна как "После танца".',
    'Работа находится в художественном музее аббатства Монсеррат.'
], '2026-07-17'),
('moonlit-night-on-the-dnieper', 'Лунная ночь на Днепре', 'arkhip-kuindzhi', 'Архип Куинджи', 'Россия', 'russian', 1880, '1880', 'moonlit-night-on-the-dnieper.jpg', $s$
Полотно прославилось почти иллюзорным свечением луны и сильным впечатлением, которое производило на современников еще в мастерской художника. Это одна из ключевых работ Куинджи на тему света и пространства.
$s$, ARRAY[
    'Картину купили еще до завершения работы.',
    'Во время морского путешествия полотно пострадало от соленого воздуха.',
    'Оригинал хранится в Государственном Русском музее.'
], '2026-07-16'),
('impression-sunrise', 'Впечатление. Восходящее солнце', 'claude-monet', 'Клод Моне', 'Франция', 'world', 1872, '1872', 'impression-sunrise.jpg', $s$
Картина была написана в порту Гавра и дала название всему направлению импрессионизма. Сначала полотно упрекали в незавершенности, но именно это впечатление стало отправной точкой для нового художественного языка.
$s$, ARRAY[
    'Работа была показана на выставке 1874 года.',
    'Название для каталога художник придумал в спешке.',
    'Сегодня картина хранится в музее Мармоттан-Моне в Париже.'
], '2026-07-15')
ON CONFLICT (id) DO NOTHING;

INSERT INTO artwork_tags (artwork_id, tag_id, tag_name, tag_type) VALUES
('the-scream', 'expressionism', 'Экспрессионизм', 'style'),
('the-scream', 'symbolism', 'Символизм', 'style'),
('the-scream', 'norway', 'Норвегия', 'collection'),
('holy-rus', 'religious-painting', 'Религиозная живопись', 'genre'),
('holy-rus', 'russia', 'Россия', 'collection'),
('holy-rus', 'symbolism', 'Символизм', 'style'),
('nighthawks', 'urban-scene', 'Городская сцена', 'genre'),
('nighthawks', 'usa', 'США', 'collection'),
('nighthawks', 'xx-century', 'XX век', 'period'),
('blame-escape', 'illusion', 'Обманчивая перспектива', 'style'),
('blame-escape', 'spain', 'Испания', 'collection'),
('american-gothic', 'portrait', 'Портрет', 'genre'),
('american-gothic', 'usa', 'США', 'collection'),
('the-laundress', 'portrait', 'Портрет', 'genre'),
('the-laundress', 'france', 'Франция', 'collection'),
('christinas-world', 'realism', 'Реализм', 'style'),
('christinas-world', 'usa', 'США', 'collection'),
('young-decadent', 'portrait', 'Портрет', 'genre'),
('young-decadent', 'spain', 'Испания', 'collection'),
('moonlit-night-on-the-dnieper', 'landscape', 'Пейзаж', 'genre'),
('moonlit-night-on-the-dnieper', 'russia', 'Россия', 'collection'),
('impression-sunrise', 'impressionism', 'Импрессионизм', 'style'),
('impression-sunrise', 'france', 'Франция', 'collection')
ON CONFLICT (artwork_id, tag_id) DO NOTHING;

INSERT INTO favorites (user_id, artwork_id, added_at) VALUES
('user-1', 'the-scream', NOW() - INTERVAL '1 day'),
('user-1', 'holy-rus', NOW() - INTERVAL '2 day'),
('user-1', 'nighthawks', NOW() - INTERVAL '3 day')
ON CONFLICT (user_id, artwork_id) DO NOTHING;

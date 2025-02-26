-- Таблица категорий
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

-- Таблица событий
CREATE TABLE IF NOT EXISTS events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(5000) NOT NULL,
    annotation VARCHAR(255) NOT NULL,
    lat DOUBLE NOT NULL CHECK (lat BETWEEN -90.0 AND 90.0),
    lon DOUBLE NOT NULL CHECK (lon BETWEEN -180.0 AND 180.0),
    event_date TIMESTAMP NOT NULL,
    category_id BIGINT NOT NULL,
    initiator_id BIGINT NOT NULL,
    state VARCHAR(20) NOT NULL CHECK (state IN ('PENDING', 'PUBLISHED', 'CANCELED')),
    paid BOOLEAN NOT NULL,
    created_on TIMESTAMP,
    published_on TIMESTAMP,
    participant_limit INT DEFAULT 0
    );

--Таблица подборок
CREATE TABLE IF NOT EXISTS compilations (
    compilation_id INT AUTO_INCREMENT PRIMARY KEY,
    pinned BOOLEAN NOT NULL,
    title VARCHAR(255) NOT NULL
);

-- Таблица дя связи события и подборок
CREATE TABLE IF NOT EXISTS compilations_events (
    compilation_id INT NOT NULL,
    event_id BIGINT NOT NULL,
    PRIMARY KEY (compilation_id, event_id),
    FOREIGN KEY (compilation_id) REFERENCES compilations(compilation_id),
    FOREIGN KEY (event_id) REFERENCES events(id)
);

CREATE TABLE IF NOT EXISTS participation_requests (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    requester_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,       -- Статус заявки ("PENDING", "CONFIRMED" и т. д.)
    created TIMESTAMP NOT NULL,
    CONSTRAINT fk_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_requester FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE
);

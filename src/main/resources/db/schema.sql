CREATE TABLE tg_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    is_admin BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE creator (
    id BIGSERIAL PRIMARY KEY,
    tg_user_id BIGINT NOT NULL UNIQUE REFERENCES tg_user(id) ON DELETE CASCADE,
    full_name VARCHAR(128) NOT NULL,
    email VARCHAR(128) NOT NULL UNIQUE,
    niche VARCHAR(128) NOT NULL,
    platform_handle VARCHAR(128) NOT NULL
);

CREATE TABLE campaign (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    description TEXT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reward_per_submission NUMERIC(10,2) NOT NULL CHECK (reward_per_submission >= 0)
);

CREATE TABLE task (
    id BIGSERIAL PRIMARY KEY,
    campaign_id BIGINT NOT NULL REFERENCES campaign(id) ON DELETE CASCADE,
    title VARCHAR(160) NOT NULL,
    brief TEXT NOT NULL,
    platform VARCHAR(64) NOT NULL,
    payout_amount NUMERIC(10,2) NOT NULL CHECK (payout_amount >= 0),
    deadline DATE NOT NULL
);

CREATE TYPE submission_status AS ENUM ('PENDING','APPROVED','REJECTED');

CREATE TABLE submission (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES task(id) ON DELETE CASCADE,
    creator_id BIGINT NOT NULL REFERENCES creator(id) ON DELETE CASCADE,
    url TEXT NOT NULL,
    status submission_status NOT NULL DEFAULT 'PENDING',
    submitted_at TIMESTAMP NOT NULL DEFAULT NOW(),
    reviewed_at TIMESTAMP,
    CONSTRAINT submission_unique UNIQUE (task_id, creator_id)
);

CREATE TABLE post_metric (
    id BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL REFERENCES submission(id) ON DELETE CASCADE,
    views INTEGER NOT NULL CHECK (views >= 0),
    likes INTEGER NOT NULL CHECK (likes >= 0),
    comments INTEGER NOT NULL CHECK (comments >= 0),
    reported_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE payout (
    id BIGSERIAL PRIMARY KEY,
    creator_id BIGINT NOT NULL REFERENCES creator(id) ON DELETE CASCADE,
    total_amount NUMERIC(12,2) NOT NULL CHECK (total_amount >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    status VARCHAR(32) NOT NULL CHECK (status IN ('PLANNED','PAID'))
);

CREATE TABLE payout_item (
    id BIGSERIAL PRIMARY KEY,
    payout_id BIGINT NOT NULL REFERENCES payout(id) ON DELETE CASCADE,
    submission_id BIGINT NOT NULL REFERENCES submission(id) ON DELETE CASCADE,
    amount NUMERIC(12,2) NOT NULL CHECK (amount >= 0)
);

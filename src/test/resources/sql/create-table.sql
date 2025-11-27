CREATE TABLE IF NOT EXISTS restaurant (
  id            BIGSERIAL PRIMARY KEY,
  name          VARCHAR(200) NOT NULL,
  address       VARCHAR(500),
  phone         VARCHAR(50),
  open_time     TIME NOT NULL,
  close_time    TIME NOT NULL,
  total_tables  INT NOT NULL,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS customer (
  id            BIGSERIAL PRIMARY KEY,
  name          VARCHAR(200) NOT NULL,
  phone         VARCHAR(50) UNIQUE NOT NULL,
  email         VARCHAR(200) UNIQUE NOT NULL,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TYPE reservation_status AS ENUM (
'PENDING',
'CONFIRMED',
'CANCELLED',
'COMPLETED'
);

CREATE TABLE IF NOT EXISTS reservation (
  id              BIGSERIAL PRIMARY KEY,
  restaurant_id   BIGINT NOT NULL REFERENCES restaurant(id),
  customer_id     BIGINT NOT NULL REFERENCES customer(id),
  table_count     INT NOT NULL,
  starts_at       TIMESTAMPTZ NOT NULL,
  status          reservation_status NOT NULL DEFAULT 'PENDING',
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_reservation_starts_at
  ON reservation(restaurant_id, starts_at);
# Postgres Serializable TX Demo


## How to Demo


```bash
$ docker run --name stx-postgres -e POSTGRES_PASSWORD=pgtest -d postgres
... (installs postgres)
$ docker run -it --rm --link stx-postgres:postgres postgres psql -h postgres -U postgres
... (runs psql within postgres image)
```

Initialization script (run once):

```sql
CREATE TABLE orders (
  order_id        INTEGER NOT NULL,
  friendly_name   VARCHAR(32) NOT NULL,
  total_amount    INTEGER NOT NULL,
  updated         INTEGER NOT NULL,
  CONSTRAINT pk_orders PRIMARY KEY (order_id)
);

INSERT INTO orders (order_id, friendly_name, total_amount, updated) VALUES (1, 'test', 20, 1045);
INSERT INTO orders (order_id, friendly_name, total_amount, updated) VALUES (2, 'test2', 40, 1310);
```

Then open two psql sessions and run sequentially:

```sql
START TRANSACTION ISOLATION LEVEL SERIALIZABLE;
-- Don't proceed further until this line executed in both sessions

-- Limit lock timeout (120 milliseconds - this allows transaction to fail sooner rather than later):
--SET LOCAL lock_timeout = '120ms';

UPDATE orders SET total_amount = total_amount + 5 WHERE order_id = 1;
-- Note, that second update blocks until other transaction commited or rolled back.

COMMIT;
-- or - ROLLBACK;
```


## Links

* [Postgres Image on Docker Hub](https://hub.docker.com/_/postgres/)


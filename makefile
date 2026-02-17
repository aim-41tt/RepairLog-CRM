start:
	docker-compose up -d

down:
	docker-compose down

down-v:
	docker-compose down -v

restart:
	docker-compose down
	make start

restart-v:
	docker-compose down -v
	make start

build:
	docker-compose up -d --build

rebuild:
	make down-v
	docker-compose up -d --build

status:
	docker-compose ps

dump_db:
	docker exec -i repairlog-postgres pg_dump -U postgres --data-only --disable-triggers repairlog_db > dump_repairlog_db.sql

set_dump_db:
	cat dump_repairlog_db.sql | docker exec -i repairlog-postgres psql -U postgres repairlog_db
.PHONY: help gen clean lint run-backend run-frontend run stop restart

.DEFAULT_GOAL := help

help:
	@echo "Available targets:"
	@echo "  make help         - Show this help message"
	@echo "  make gen          - Generate protobuf files"
	@echo "  make lint         - Lint protobuf files"
	@echo "  make clean        - Remove generated files"
	@echo "  make run-backend  - Run the Kotlin backend"
	@echo "  make run-frontend - Run the Angular frontend"
	@echo "  make run          - Run both backend and frontend concurrently"
	@echo "  make stop         - Stop running backend and frontend services"
	@echo "  make restart      - Stop services and run them again"

gen: lint
	buf generate

lint:
	buf lint

clean:
	rm -rf backend/src/main/gen
	rm -rf frontend/src/proto-gen

run-backend:
	cd backend && ./gradlew run

run-frontend:
	cd frontend && npm run start

run:
	$(MAKE) -j2 run-backend run-frontend

stop:
	@echo "Stopping backend (port 8080) and frontend (port 4200)..."
	-fuser -k 8080/tcp 2>/dev/null || true
	-fuser -k 4200/tcp 2>/dev/null || true
	-lsof -t -i:8080 | xargs kill -9 2>/dev/null || true
	-lsof -t -i:4200 | xargs kill -9 2>/dev/null || true
	-pkill -f "ng serve" 2>/dev/null || true
	-pkill -f "gradlew run" 2>/dev/null || true
	@echo "Services stopped."

restart: stop run

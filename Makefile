.PHONY: help gen clean lint run-backend run-frontend run

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

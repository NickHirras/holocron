.PHONY: gen clean lint run-backend

gen: lint
	buf generate

lint:
	buf lint

clean:
	rm -rf backend/src/main/gen
	rm -rf frontend/src/proto-gen

run-backend:
	cd backend && ./gradlew run


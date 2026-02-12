# Contributing

## Coding Standards
*   **Style Guide:** Google Java Style Guide.
*   **Quality:** Robust, secure, performant. "Google-scale" thinking even for a small app.

## Build & Run
*   **Build:** `./gradlew build`
*   **Dev Mode:** `./gradlew quarkusDev`

## Agent Development Guidelines
*   **Adding Features:** Start with a Java Entity + Flyway script, then a Qute template, then a Resource controller.
*   **Clean Templates:** Prioritize semantic tags (`<article>`, `<nav>`, `<header>`) over `<div>` soup to leverage Pico CSS.
*   **Type Safety:** Use `@CheckedTemplate` in Java controllers to ensure templates have valid data parameters at compile time.

# AGENTS.md

Project instructions for coding agents working in this repository.

## Project Context

This project is a Minecraft mod built with the NeoForge mod loader.

- Use Java 21.
- Use Gradle for builds and tests.
- Always read version values from `gradle.properties`, including the NeoForge version, Minecraft version, and Minecraft version range.
- Treat `README.md` as the source of truth for the mod description and expected player-facing functionality.

## Product Documentation

- Keep `README.md` up to date when functionality, player mechanics, usage, or rules change.
- Keep README wording focused on player-facing behavior.
- Avoid exposing NeoForge, Minecraft internals, or implementation details in player-facing README text unless they directly affect usage.

## Development Workflow

- Practice test-driven development.
- Write tests before implementation changes whenever adding or changing behavior.
- Do not change tests only to make them pass.
- Change tests only when they no longer match the expected functionality described in `README.md` or an approved requirement change.
- When possible, run the relevant Gradle tests before presenting changes for review.
- Explain every change made and present the diff for review and approval.

## Testing Standards

- Use JUnit for tests.
- Use Mockito where necessary for Minecraft or NeoForge classes that cannot be used reliably outside a running Minecraft game.
- Keep as much logic as possible testable in regular JUnit tests.
- Prefer small, focused classes and methods that can be tested without bootstrapping Minecraft, NeoForge, registries, or a full game environment.
- Isolate game integration code from pure logic so behavior can be verified independently.

## Java Organization

- Organize Java packages around features.
- Keep classes supporting the same feature in the same package, including related blocks, block entities, block items, items, menus, recipes, data components, and helpers.
- When creating a class, use a suffix that describes its primary scope.
- When extending a Minecraft or NeoForge class, prefer a suffix matching the extended concept, such as `Block`, `BlockEntity`, `BlockItem`, or `Item`.

## Documentation Standards

- Add clear Javadocs above every new class.
- Add clear Javadocs above every new method.
- Keep Javadocs useful and behavior-focused. Avoid restating obvious implementation details.

## Review Expectations

- Keep changes scoped to the requested feature or fix.
- Preserve unrelated user changes.
- Before asking for review, summarize what changed, list tests run, and show or reference the diff.

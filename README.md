# catalogue-common

The code that **IngredientCatalogue** and **RecipeCatalogue** genuinely share,
in one place instead of copy-pasted twice.

![build](https://img.shields.io/badge/build-Gradle-blue)
![type](https://img.shields.io/badge/artifact-java--library-lightgrey)
![java](https://img.shields.io/badge/Java-17-orange)

## What's in it

| Package | Classes |
|---|---|
| `com.example.cataloguecommon` | `PageResponse<T>` (stable page envelope), `ApiExceptionHandler` (RFC-9457 `ProblemDetail`), `NotFoundException`, `Csv` (small RFC-4180-ish reader), `TracingSenderConfig` (the URLConnection Zipkin sender) |
| `com.example.cataloguecommon.tag` | `Tag` (`@Entity`), `TagRepository`, `TagService` + `TagServiceImpl` (search / CRUD / merge / name-resolution — `list(nameContains, …)` is a case-insensitive **substring** match), `TagOwnerCleanup` (SPI — see below), `TagRequest` / `TagResponse` / `TagNamesRequest` / `MergeRequest` |

### Limits it enforces (finding N4)

Both catalogues inherit these, so a CSV import can't bypass what the JSON API
enforces:

- `TagService.resolve` refuses more than **50 tags** per request
  (`MAX_TAGS_PER_REQUEST`) or a name over **100 chars** (`MAX_TAG_LENGTH`,
  `tag.name varchar(100)`); `TagNamesRequest` / `MergeRequest` carry matching
  `@Size` annotations so the HTTP answer is a clean 400 naming the field.
- `Csv.parse` refuses a file with more than **5 000 data rows** (`MAX_ROWS`) or a
  row with more than **100 fields** (`MAX_FIELDS_PER_ROW`) — the 5 MB upload cap
  alone would allow ~1 M tiny rows. It fails fast, so a run of millions of commas
  never builds millions of fields.

### The one per-service bit: `TagOwnerCleanup`

Deleting or merging a tag has to first clear it off every entry that carries it —
there's no DB cascade on the `*_tag` join table. That entry is `Ingredient` in
one service and `Recipe` in the other, so `TagServiceImpl` depends on the
`TagOwnerCleanup` interface and each service supplies a one-class `@Component`
(`IngredientTagOwnerCleanup` / `RecipeTagOwnerCleanup`).

## How the services consume it

A **Gradle composite build** — no publish step, always compiled from source:

```kotlin
// IngredientCatalogue/settings.gradle.kts
includeBuild("../catalogue-common")
```
```kotlin
// IngredientCatalogue/build.gradle.kts
implementation("com.example:catalogue-common")
```

The shared `Tag` entity, its repository and the `@Component`s live outside the
service's own package, so each app class widens the scan:

```java
@SpringBootApplication(scanBasePackages = { "com.example.<service>", "com.example.cataloguecommon" })
@EntityScan(basePackages          = { "com.example.<service>", "com.example.cataloguecommon.tag" })
@EnableJpaRepositories(basePackages = { "com.example.<service>.Repositories", "com.example.cataloguecommon.tag" })
```

Each service keeps its own `tag` table (own schema, own Flyway migration) — the
shared entity just maps to whichever one is in scope.

## Build

```bash
./gradlew build      # compile + the small unit tests (Csv parsing + limits, tag
                     # normalisation + limits, internal-auth signing)
```

It's driven automatically by each catalogue's build (and Dockerfile — those set
their build context to `Projects/` so `../catalogue-common` is present).

## Versioning

Boot BOM `4.1.1`, pinned in `build.gradle.kts` to match both consumers. Bump all
three together.

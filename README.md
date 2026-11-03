# Информационная система библиотеки

Единое консольное приложение объединяет предметную модель библиотеки, работу с XML и JSON, а также обработку аннотаций через Reflection API. Код организован по назначению (`model`, `service`, `persistence`, `metadata`, `ui`), без пакетов и отдельных точек входа для лабораторных работ.

## Лабораторная работа 6

В проекте устранены четыре архитектурные проблемы:

1. `LibraryService` зависел от конкретных хранилищ. Теперь он принимает интерфейсы `BookRepository`, `ReaderRepository` и `LoanRepository`, а in-memory реализации находятся в пакете `repository.memory`. Это устраняет нарушение DIP.
2. Перегруженный конструктор `LibraryService` скрыто создавал `ReflectionLibraryActionExecutor`. Теперь все обязательные зависимости передаются только через constructor injection.
3. `LibraryService` одновременно выполнял бизнес-сценарии и заполнял демонстрационные данные. Начальные данные вынесены в `InitialLibraryDataSeeder`, что устраняет нарушение SRP.
4. `ConsoleMenu` самостоятельно создавал сервис обмена данными и `Scanner`. Теперь меню получает `LibraryDataExchange` и `Scanner` через конструктор, поэтому его зависимости явны и заменяемы.

Класс `ApplicationCompositionRoot` является единственной точкой сборки основного графа объектов. Тест `InfrastructureReplaceabilityTest` выполняет один сценарий выдачи книги с двумя реализациями `BookRepository` и подтверждает, что бизнес-логика не меняется при замене инфраструктуры.

## Требования

- JDK 17 или новее;
- Maven Wrapper, включённый в проект;
- доступ к Maven Central при первой сборке.

## Сборка и тесты

```powershell
.\mvnw.cmd clean test
```

## Запуск программы

```powershell
.\mvnw.cmd -q -DskipTests package dependency:copy-dependencies
java -cp "target/classes;target/dependency/*" com.sitairis.Main
```

Основное меню содержит все библиотечные операции и дополнительный пункт обработки файлов:

- пункт 14 — XML/XSD, SAX, DOM, XSLT и Jackson JSON;
- пункты 6, 7 и 9 используют метаданные и Reflection API внутри обычных сценариев выдачи, возврата и контроля срока.

## XML и JSON

Исходные данные находятся в `src/main/resources/data`:

- `library.xml` и `library.xsd` — основной документ и схема;
- `invalid-email.xml`, `invalid-reference.xml` — отрицательные сценарии;
- `library.json` — эквивалентное JSON-представление;
- `library-report.xsl` — формирование HTML-отчёта.

Результаты DOM, XSLT и JSON создаются в `target/library-data-output`.

## Аннотации и Reflection API

`@EntityMetadata(name = ...)` описывает сущность, а `@ScenarioAction(...)` связывает метод с выдачей, возвратом или контролем срока. При запуске `Main` класс `LibraryMetadataProcessor` проверяет `Reader`, `Book` и `Loan`, после чего передаёт реестр в `LibraryService`. Поэтому выдача книги, возврат и проверка просрочек выполняют аннотированные методы через `ReflectionLibraryActionExecutor`, а не через отдельный демонстрационный пункт меню. `DirectLibraryActionExecutor` сохранён как решение без reflection для сравнительных тестов.

Тесты охватывают успешные и ошибочные XML/JSON-сценарии, позитивные и негативные проверки метаданных, неверные аргументы reflection и одинаковое поведение рефлексивной и прямой реализаций.

# Solution checker for geometry problems
This project is a DSL. It is used for:
1. creating geometry problems by iteratively constructing them,
2. solving geometry problems,
3. automatically checking written solutions.

# Архитектура

Этот проект - библиотека, которая собирается с помощью команды `gradle publishToMavenLocal`. В `build.gradle.kts` сервера есть ее импорт в dependencies-блоке.

## Ключевые термины
* AST - абстрактное синтаксическое дерево. Структура, создаваемая парсером `GeomGrammar`.
* 
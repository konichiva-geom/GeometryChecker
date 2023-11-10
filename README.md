# Solution checker for geometry problems
This project is a DSL. It is used for:
1. creating geometry problems by iteratively constructing them,
2. solving geometry problems,
3. automatically checking written solutions.

# Architecture

This is a library, which is built with `gradle publishToMavenLocal`. In `build.gradle.kts` of server-module there is an import in dependencies.

## Keywords
* AST - abstract syntax tree. A structure created by the `GeomGrammar` parser.

## Common mistakes

If some strange error happens check that:
1. theorems.txt, primes.txt, inference.txt (and any new parsed file) has `LF` line separators. This should be checked for all used modules (server and site).

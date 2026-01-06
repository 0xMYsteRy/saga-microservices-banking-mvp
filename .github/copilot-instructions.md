**Copilot Contribution Checklist**

1. Use Maven for all Java dependency management.
2. Describe your plan or solution approach before generating any code.
3. Wait for explicit approval before writing or modifying code.
4. Work in very small, incremental steps so reviews stay focused.
5. Target Java 21 and leverage its modern language features and APIs.
6. Only add libraries that exist in Maven Central; otherwise request approval.
7. Follow the current code style, structure, and established patterns.
8. Declare new dependencies in the parent `pom.xml` for centralized versioning.
9. Check whether a dependency already exists in the parent POM before adding a duplicate; ask before introducing new ones.
10. Generate only the code that is necessary—keep implementations simple and readable.
11. Prefer Lombok annotations to minimize boilerplate.
12. Avoid over-engineering; prioritize clarity and maintainability.
13. Use normal import statements instead of fully qualified class names inside code blocks.
14. Always work on a feature branch, open a new pull request, and merge it into `master`; never push directly to `master`. Re-read this checklist every time you run.

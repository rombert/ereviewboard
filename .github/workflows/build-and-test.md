# Build and test

Prerequisites: a JDK and Maven compatible with this older Tycho 0.21.0 build, and access to the archived p2 repositories in [ereviewboard.target](../../ereviewboard.target). The target specifies a J2SE-1.5 execution environment; availability on a modern JDK is not guaranteed. Import the root project and target into an Eclipse PDE workspace when developing interactively. The checked-in [.travis.yml](../../.travis.yml) runs `mvn verify`.

From the repository root:

```sh
mvn verify
```

For an affected module with its reactor dependencies, use `mvn -pl <module-directory> -am verify`; some Eclipse test fragments and the p2 target still need the complete platform. The [connector test fragment](../org.review_board.ereviewboard.tests/overview.md) covers core mappers, REST parsing, queries and model behavior; the [Subclipse test fragment](../org.review_board.ereviewboard.subclipse.tests/overview.md) covers SVN diff generation. Tycho runs these `eclipse-test-plugin` modules in an Eclipse/OSGi runtime, not as ordinary standalone JUnit jars. A dependency-resolution failure does not establish a source or test failure; report it separately.

Check module packaging in the [root POM](../../pom.xml), bundle dependencies in each manifest, and test fixtures under the test module's `testdata/`. Avoid using files in `target/` or `bin/` as source.

# CI

Every gate below blocks a merge. The names in this file are the check names that
branch protection should require; keep them stable.

## Pipeline

```
validate ──┬── lint ─────┐
           ├── format ───┤
           └── jdk[5] ───┴── build[4 OS] ── gui[3 OS] ──┐
                       └── test[4 OS] ────────────────┴── gate ── package
```

`gate` is a fan-in with no work of its own: it exists so branch protection has one
required check instead of one per platform per stage. `package` depends on it, so a
jar is only built for a commit whose tests and GUI runs all passed.

`package.yml` is a thin wrapper around `_package.yml` so `release.yml` and any
external caller keep working.

| Check | Workflow | What it proves |
|---|---|---|
| `Validate wrapper` | ci.yml | the Gradle wrapper jar is untampered |
| `Lint / Workflows` | _lint.yml | actionlint, zizmor, YAML parse, LF-only blobs, no case collisions |
| `Format / Spotless` | _format.yml | palantir-java-format, JSON trailing whitespace, final newlines |
| `JDK matrix / <vendor>` | _jdkmatrix.yml | the sources compile with Oracle OpenJDK, Corretto, Zulu, Oracle JDK and Temurin |
| `Build / <os>` | _build.yml | `-Xlint:all -Werror` and ErrorProne on main sources |
| `Test / <os>` | _test.yml | unit tests, JaCoCo gate, GameTests, dedicated-server smoke |
| `GUI / <os>` | _gui.yml | a real OS click reaches the forge screen and its button (Linux, both Windows images) |
| `Gate` | _gate.yml | fan-in over Test and GUI; the single check to require |
| `Package / Jar` | _package.yml | jar contents, checksums, build provenance attestation |

## Why the matrix looks like this

GitHub hosts no Windows 10 or 11 **desktop** image. `windows-2022` is Server 2022
and `windows-2025` is Server 2025; they approximate the Windows 10 and Windows 11
kernel families and are the closest hosted targets. No self-hosted runner is used.

`macos-15` is arm64. An Intel macOS run is a nightly-only concern, not a PR gate.

## The JDK vendor matrix is not decorative

`java.toolchain.languageVersion = 21` plus the foojay resolver means Gradle will
happily download a Temurin JDK whenever the requested toolchain is missing. Left
alone, a five-vendor matrix would compile with Temurin five times.

`.github/actions/gradle-setup` therefore writes

```
org.gradle.java.installations.auto-detect=false
org.gradle.java.installations.auto-download=false
org.gradle.java.installations.paths=$JAVA_HOME
```

into the Gradle user home, and every vendor job then runs

```
./gradlew verifyToolchain -PexpectedJdkVendor=<vendor>
```

which reads `javaToolchains.launcherFor { 21 }` and fails when the vendor does not
match. Both directions were checked locally: `-PexpectedJdkVendor=Amazon` fails
against a Temurin installation with a message naming the vendor Gradle actually
resolved.

If `oracle-openjdk` stops publishing 21 (jdk.java.net archives old GA builds), the
job fails with a setup-java error rather than silently passing; the fallback is to
drop that entry and rely on `oracle`, which is the same JDK under a different
distribution name.

## Static analysis

| Tool | Scope | Why it is where it is |
|---|---|---|
| `javac -Xlint:all -Werror` | main | `removal`, `deprecation`, `unchecked`, `fallthrough` |
| ErrorProne 2.53.0 | main | real bug patterns, forked compiler |
| CodeQL | main + test + gametest + clienttest | dataflow and workflow injection |
| SpotBugs | **absent** | `spotbugs-gradle-plugin` 3.0.0 is the only release and fails on Gradle 9 with `org/gradle/api/reporting/internal/TaskReportContainer` |

`-Werror` is deliberately main-only: the test and GameTest source sets call
`GameTestHelper#makeMockServerPlayerInLevel`, which 1.21.1 deprecates for removal
and offers no replacement for yet.

ErrorProne needs `options.fork = true` and the jars on the annotation processor
path. ModDevGradle owns that path for the modding classpath, so `build.gradle`
appends a resolvable `errorproneJars` configuration to it. Without the fork flag
the analysis silently does nothing, which was verified by injecting
`int[] a == b` and watching `[ReferenceEquality]` appear.

## GUI test

The click must travel the production path: OS event, GLFW, `MouseHandler`,
`ScreenEvent.MouseButtonPressed.Pre`, `AbstractContainerScreen`, `ForgeButton`,
`clickMenuButton`. The harness refuses to call `screen.mouseClicked()` because
that skips the first three steps.

The harness opens the menu server-side. Aiming a grabbed cursor with synthetic
absolute motion is not reproducible (GLFW receives relative deltas and the camera
spins), and the click under test is the button click.

Renderer availability is the platform risk:

- Linux: Mesa llvmpipe under Xvfb, confirmed working.
- Windows: the hosted image exposes only GDI generic OpenGL 1.1, so Mesa3D's
  llvmpipe `opengl32.dll` is installed from a pinned release.
- macOS: **not run.** A hosted macOS runner has no GPU, and its WindowServer
  rejects every OpenGL profile the client asks for:
  `NSGL: Failed to find a suitable pixel format` from 4.6 down to 3.2. The client
  never creates a window, so no input can reach it. This is a capability of the
  runner, not something the mod can influence, so the matrix covers Linux and the
  two Windows images and macOS is left to the manual checklist. The `CGEvent`
  driver is kept in `tools/gui_driver/drive_macos.swift` for a machine that has a
  real display.

The harness records `GL_RENDERER` in its report, so a run that never had a real
context cannot pass as if it did.

This gate found a real defect on its first run: `ForgeBlockEntity` did not
implement `IMenuProviderExtension#writeClientSideData`, so the open-screen packet
carried no payload and the client threw a `NullPointerException` in
`RegistryFriendlyByteBuf.readBlockPos()`. A server-side GameTest cannot see this,
because the dedicated server never serialises that packet for itself.

## Local commands

```bash
export JAVA_HOME=~/.gradle/jdks/eclipse_adoptium-21-amd64-linux.2   # JDK 21

./gradlew spotlessCheck
./gradlew compileJava compileTestJava compileGametestJava -PwarningsAsErrors
./gradlew check
./gradlew runGameTestServer
./gradlew prepareRunDir serverSmokeTest
./gradlew jar verifyJar checksums
./gradlew verifyToolchain -PexpectedJdkVendor=Temurin \
  "-Dorg.gradle.java.installations.auto-detect=false" \
  "-Dorg.gradle.java.installations.auto-download=false" \
  "-Dorg.gradle.java.installations.paths=$JAVA_HOME"
bash tools/gui_driver/run_linux.sh        # needs Xvfb; Mesa llvmpipe is enough
```

The toolchain properties must be `-D` flags on the command line. `GRADLE_OPTS` is
read by the launcher, not the daemon, so it does not reach the toolchain registry.

## Dependency updates

Dependabot groups minor and patch bumps and waits seven days (`cooldown`) before
opening a PR, because a same-day release has not been built by anyone else. Major
bumps stay separate.

NeoForge and Parchment versions live in `gradle.properties`, which Dependabot does
not manage; bump them by hand and let the CI matrix validate the result.

## Adding a gate

1. Put the logic in a Gradle task or a script under `tools/`, not in inline shell.
   `stat -c%s`, `sha256sum`, `printf` and `timeout` do not exist on every runner.
2. Give the check a `<stage> / <axis>` name and add it to the table above.
3. Do not add `paths-ignore` to a workflow that owns a required check: a skipped
   run leaves the required check pending forever.

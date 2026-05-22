# Staff-Auth
An [Ory Hydra](https://github.com/ory/hydra) authentication and authorization provider.

> [!CAUTION]
> This server implements no rate limiting on its own, make sure rate limiting is configured in your reverse proxy, specifically on `/login`.

## Building
JVM: `./gradlew shadowJar`\
GraalVM Native: `./gradlew nativeCompile`

## Configuration
Copy [application.yml.example](application.yml.example), rename it to whatever you like, fill it in and run the server binary with `MICRONAUT_CONFIG_FILE=path/to/your/application.yml`.

## Initial setup
Run the server binary with `--initialSetup.adminUuid=your-uuid-here`.
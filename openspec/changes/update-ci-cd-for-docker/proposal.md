# Change: Update CI/CD Pipeline to Use Docker

## Why
The current GitHub Actions workflow is failing because it attempts to run Gradle commands directly without a Gradle wrapper. Since the project builds everything in Docker, we should update the CI/CD pipeline to use Docker containers for all build operations instead of trying to run Gradle directly on the GitHub Actions runner.

## What Changes
- Modify GitHub Actions workflow to use Docker instead of direct Gradle commands
- Remove Gradle-specific setup steps from the workflow
- Add Docker build steps to run tests and build APKs
- Update artifact upload paths to match Docker build output locations
- **BREAKING**: Requires Docker image with Android SDK and build tools

## Impact
- Affected specs: ci-cd (existing)
- Affected code: GitHub Actions workflow files
- Removes dependency on Gradle wrapper files
- Changes build environment from GitHub Actions runners to Docker containers
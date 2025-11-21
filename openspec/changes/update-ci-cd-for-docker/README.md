# Update CI/CD for Docker - OpenSpec Change

## Overview
This change proposal updates the GitHub Actions CI/CD pipeline to use Docker-based builds instead of direct Gradle commands, aligning with the project's Docker-first approach.

## Files Created
1. `proposal.md` - Change proposal documentation
2. `tasks.md` - Implementation tasks
3. `specs/ci-cd/spec.md` - Updated CI/CD requirements
4. `findings.md` - Analysis of the current issue
5. `README.md` - This file

## Supporting Files
1. `Dockerfile.simple` - Simple Dockerfile for demonstration
2. `.github/workflows/android-ci-docker.yml` - Updated workflow using Docker

## Key Findings
- Current GitHub Actions workflow fails because it tries to execute Gradle commands directly but missing `gradlew` file
- Project follows Docker-based build approach but workflow was designed for direct execution
- Solution is to update workflow to use Docker containers for all build operations

## Implementation Status
- [x] Created OpenSpec documentation
- [x] Analyzed current workflow issues
- [x] Created Docker-based workflow alternative
- [ ] Update existing workflow file
- [ ] Test Docker-based workflow
- [ ] Remove Gradle-specific steps
- [ ] Verify artifact handling in Docker environment

## Next Steps
1. Replace `.github/workflows/android-ci.yml` with Docker-based version
2. Test the new workflow with a pull request
3. Verify all build steps work correctly in Docker environment
4. Update artifact paths to match Docker build output locations
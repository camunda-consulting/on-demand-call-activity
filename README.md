# On-demand Call Activity
A Camunda BPM plugin that allows a BPMN Call Activity to dynamically call a child process or perform an asynchronous service call.

[![Java CI with Maven](https://github.com/camunda-consulting/on-demand-call-activity/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/camunda-consulting/on-demand-call-activity/actions)

Besides the code in the master branch there are several test branches using the Camunda engine test suite:
- [engine-test-suite-with-child-processes](https://github.com/camunda-consulting/on-demand-call-activity/tree/engine-test-suite-with-child-processes)
- [engine-test-suite-without-child-processes](https://github.com/camunda-consulting/on-demand-call-activity/tree/engine-test-suite-without-child-processes)

## How to update to and test with a new Camunda version?
1. Clone this repository using `git clone git@github.com:camunda-consulting/on-demand-call-activity.git`
1. Add [Camunda BPM Platform](https://github.com/camunda/camunda-bpm-platform/) as remote `git remote add camunda-bpm-platform git@github.com:camunda/camunda-bpm-platform.git`
1. Checkout branch `git checkout engine-test-suite-with-child-processes`
1. Ensure it is up to date `git pull`
1. Create branch for new version `git checkout -b engine-test-suite-with-child-processes-7.13.0` (replace 7.13.0 with the [version](https://github.com/camunda/camunda-bpm-platform/tags) you're updating to)
1. Pull new version `git pull camunda-bpm-platform 7.13.0`
1. If needed edit conflicts `git mergetool` and commit `git commit`
1. Push new branch to GitHub `git push --set-upstream origin engine-test-suite-with-child-processes-7.13.0` (old versions branches should be kept)
1. Switch to master branch `git checkout master`
1. Edit [.github/workflows/maven.yml](https://github.com/camunda-consulting/on-demand-call-activity/edit/master/.github/workflows/maven.yml) and add new branch to the matrix of the `test` job, e.g.
```yaml
  test:
    needs: build
    strategy:
      matrix:
        branch:
        - engine-test-suite-with-child-processes
        - engine-test-suite-without-child-processes
        - engine-test-suite-with-child-processes-7.13.0
 ```

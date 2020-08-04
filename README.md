# On-demand Call Activity
A Camunda BPM plugin that allows a BPMN Call Activity to dynamically call a child process or perform an asynchronous service call.

[![Java CI with Maven](https://github.com/camunda-consulting/on-demand-call-activity/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/camunda-consulting/on-demand-call-activity/actions)

You can find the plugin code on the master branch.

## How to test an engine plugin against the process engine test suite
To check if the plugin has some unexpected side effects to the process engine, you can run the complete engine test suite against a process engine that contains your plugin.

To do this, add the camunda-bpm-platform repository as a remote branch to your repository. Then add your plugin in all variants of `camunda.cfg.xml` to the engine(s) under test. You can find all configurations in subfolders on `src/test/resources` with the pattern `*camunda*.cfg*.xml`.

The steps to do the git branching are listed in details below.

There are several test branches using the Camunda engine test suite:
- [engine-test-suite-with-child-processes](https://github.com/camunda-consulting/on-demand-call-activity/tree/engine-test-suite-with-child-processes)
- [engine-test-suite-without-child-processes](https://github.com/camunda-consulting/on-demand-call-activity/tree/engine-test-suite-without-child-processes)
- [engine-test-suite-with-child-processes-7.13.0](https://github.com/camunda-consulting/on-demand-call-activity/tree/engine-test-suite-with-child-processes-7.13.0)
- [engine-test-suite-without-child-processes-7.13.0](https://github.com/camunda-consulting/on-demand-call-activity/tree/engine-test-suite-without-child-processes-7.13.0)
- [engine-test-suite-with-child-processes-7.14.0-SNAPSHOT](https://github.com/camunda-consulting/on-demand-call-activity/tree/engine-test-suite-with-child-processes-7.14.0-SNAPSHOT)
- [engine-test-suite-without-child-processes-7.14.0-SNAPSHOT](https://github.com/camunda-consulting/on-demand-call-activity/tree/engine-test-suite-without-child-processes-7.14.0-SNAPSHOT)

A GitHub action under [maven.yml](https://github.com/camunda-consulting/on-demand-call-activity/edit/master/.github/workflows/maven.yml) builds the plugin and runs all tests on GitHub after pushing any change.

## How to update to and test with a new Camunda version?
1. Clone this repository using `git clone git@github.com:camunda-consulting/on-demand-call-activity.git`
1. Add [Camunda BPM Platform](https://github.com/camunda/camunda-bpm-platform/) as remote `git remote add camunda-bpm-platform git@github.com:camunda/camunda-bpm-platform.git`
1. Repeat the following steps for all engine test suite branch variations, which are currently `engine-test-suite-with-child-processes` and `engine-test-suite-without-child-processes` (replace the branch name and version numbers in the comands below):
    1. Checkout branch `git checkout engine-test-suite-with-child-processes-7.13.0`
    1. Ensure it is up to date `git pull`
    1. Create branch for new version `git checkout -b engine-test-suite-with-child-processes-7.14.0` (replace 7.14.0 with the [version](https://github.com/camunda/camunda-bpm-platform/tags) you're updating to)
    1. Pull new version `git pull camunda-bpm-platform 7.14.0`
    1. If needed edit conflicts `git mergetool` and commit `git merge --continue`
    1. Push new branch to GitHub `git push --set-upstream origin engine-test-suite-with-child-processes-7.14.0` (old versions branches should be kept; replace 7.13.0 with the [version](https://github.com/camunda/camunda-bpm-platform/tags) you're updating to)
1. Switch to master branch `git checkout master`
1. Ensure it is up to date `git pull`
1. Edit [.github/workflows/maven.yml](https://github.com/camunda-consulting/on-demand-call-activity/edit/master/.github/workflows/maven.yml) and add new branches to the matrix of the `test` job, e.g.
```yaml
  test:
    needs: build
    strategy:
      matrix:
        branch:
        - engine-test-suite-with-child-processes
        - engine-test-suite-without-child-processes
        - engine-test-suite-with-child-processes-7.13.0
        - engine-test-suite-without-child-processes-7.13.0
        - engine-test-suite-with-child-processes-7.14.0
        - engine-test-suite-without-child-processes-7.14.0
 ```
1. Push some change to `master` to trigger the build
1. Check [test results](https://github.com/camunda-consulting/on-demand-call-activity/actions)
1. Fix or `@Ignore` failing tests, push the changes, and re-run tests


## How to modify the engine-test-suite-* branches?
1. Checkout `git checkout engine-test-suite-with-child-processes`
1. Do the change and `git commit && git push`
1. Checkout `git checkout engine-test-suite-without-child-processes`
1. Merge `git merge engine-test-suite-with-child-processes`
1. If needed edit conflicts `git mergetool` and commit `git merge --continue`
1. `git push`
1. Merge the changes into the other versions, e.g.:
    1. engine-test-suite-with-child-processes -> engine-test-suite-with-child-processes-7.13.0
    1. engine-test-suite-with-child-processes-7.13.0 -> engine-test-suite-with-child-processes-7.14.0
    1. engine-test-suite-without-child-processes -> engine-test-suite-without-child-processes-7.13.0
    1. engine-test-suite-without-child-processes-7.13.0 -> engine-test-suite-without-child-processes-7.14.0
1. Push some change to `master` to trigger the build
1. Check [test results](https://github.com/camunda-consulting/on-demand-call-activity/actions)
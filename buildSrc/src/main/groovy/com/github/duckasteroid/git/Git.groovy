package com.github.duckasteroid.git

import groovy.transform.Immutable
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Collectors

/**
 * The result of running a git process
 */
@Immutable
class ProcessResult {
    List<String> output;
    String errorOutput;
    int exitCode;

    static ProcessResult from(Process p) {
        ArrayList<String> output = new ArrayList()
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.add(line)
            }
        }
        String errorOutput = new BufferedReader(new InputStreamReader(p.getErrorStream()))
                .lines()
                .collect(Collectors.joining("\n"));

        int exitCode = p.waitFor()
        return new ProcessResult(output, errorOutput, exitCode)
    }
}

/**
 * Run a set of git args
 * @param args the args for git
 * @return a result if exitCode == 0
 * @throws RuntimeException If git returns an error
 */
static ProcessResult withGit(List<String> args) {
    def command = ["git"]
    command.addAll(args)
    ProcessBuilder pb = new ProcessBuilder(command)
    Process p = pb.start()
    def result = ProcessResult.from(p)
    if (result.exitCode != 0) {
        throw new RuntimeException(result.errorOutput)
    }
    return result
}

interface VersionSource {
    boolean hasVersion()
    String version();
}
/**
 * Represents a git commit as a version source (not preferred)
 */
@Immutable
class Commit implements VersionSource {
    String commitId

    @Override
    boolean hasVersion() {
        return true
    }

    String version() {
        return commitId
    }

    String toString() {
        return "Commit: "+commitId
    }
}
/**
 * Represents data about a git tag
 */
@Immutable
class GitTag implements VersionSource {
    // Regular expression to match "vX.Y.Z" at the end of the string
    private static final Pattern PATTERN = Pattern.compile('v(\\d+\\.\\d+\\.\\d+)$')

    String tag;
    OffsetDateTime commitDate;
    String subject;
    String shortCommit;
    String longCommit;

    boolean hasVersion() {
        Matcher matcher = PATTERN.matcher(tag)
        return matcher.find()
    }

    String version() {
        Matcher matcher = PATTERN.matcher(tag)
        matcher.find()
        return matcher.group(1); // Extract the version number
    }

    Optional<String[]> versionSegments() {
        Optional.ofNullable(version()).map {it.split('.') }
    }

    final static char SEPARATOR = '\u0001';
    final static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss~Z")

    static GitTag parse(String formatted) {
        String[] split = formatted.split("${SEPARATOR}");
        return new GitTag(split[0],
                OffsetDateTime.parse(split[1], DATE_FORMAT),
                split[2],
                split[3],
                split[4]);
    }

    static String formatString() {
        return "%(refname:short)${SEPARATOR}%(committerdate:format:%Y-%m-%d@%H:%M:%S~%z)${SEPARATOR}%(subject)${SEPARATOR}%(objectname:short)${SEPARATOR}%(objectname)";
    }

    String toString() {
        return "Tag ["+tag+"]:"+version()
    }
}

/**
 * Get the git tags in the repo that match a given pattern
 * @param pattern the pattern of the tag (or null)
 * @return a list of tags most recent commit first
 */
static List<GitTag> gitTags(String pattern) {
    def args = ["for-each-ref", "--sort=-committerdate", "--format=\"${GitTag.formatString()}\""]
    if (pattern == null || pattern.isBlank()) {
        args.add("refs/tags")
    }
    else {
        args.add("refs/tags/"+pattern)
    }
    def result = withGit(args.collect { it.toString()})
    return result.output.collect { GitTag.parse(it) }
}

/**
 * Is the repository dirty. Optionally just the given path pattern.
 * @param pattern a pattern to restrict the check to
 * @return true if dirty (e.g. uncommited stuff in that path)
 */
static boolean gitDirty(String pattern) {
    def args = ["status", "--short", "-z"]
    if (pattern != null && !pattern.isBlank()) {
        args.addAll(["--", pattern])
    }
    def result = withGit(args)
    return result.output.count {!it.isBlank() }
}

/**
 * A git change
 */
@Immutable
class Change {
    String status;
    String path;

    static Change from(String s) {
        new Change(s.substring(0,1), s.substring(1).trim())
    }
}

/**
 * Get the changes in the repo since the given tag, optionally restricted to a given path
 * @param tag the tag to base the diff on
 * @param path the optional path to consider
 * @return the changes in the repo (or an empty list)
 */
static List<Change> gitDiff(String tag, String path) {
    def args = ["diff", "--name-status", tag]
    if (path != null && !path.isBlank()) {
        args.addAll(["--", path])
    }
    def result = withGit(args)
    return result.output.collect { Change.from(it)}
}

/**
 * Get the current commit ID (short or long) on the current HEAD
 * @param shortVersion short?
 * @return the commit ID
 */
static String gitCommitID(boolean shortVersion) {
    def args = ["rev-parse", "HEAD"]
    if (shortVersion) {
        args.add(1, "--short")
    }
    def result = withGit(args)
    return result.output[0]
}

/**
 * Gets the current branch name
 * @return the branch
 */
static String gitBranch() {
    return withGit(["rev-parse", "--abbrev-ref", "HEAD"]).output[0].trim()
}

static def gitVersion(Project project) {
    def candidates = taggedVersions(project)
    def versionSource = candidates.find { it.hasVersion() }
    return versionSource.version()
}
/**
 * Retrieve the git version for a project.
 * The following are used in order of preference:
 * <ol>
 *     <li>Git version tags in a "folder" matching the project path</li>
 *     <li>Git version tags (vXXX) with no path (e.g. v1.0.0)</li>
 *     <li>The short form of the last commit ID on the current branch</li>
 * </ol>
 * @param project
 * @return
 */
static def taggedVersions(Project project) {
    // the fallback if we can't find something more specific...
    VersionSource version = new Commit(gitCommitID(true))

    // path is preceded by ':'
    def path = project.path.substring(1).trim()
    List<VersionSource> tags = gitTags("v*")
    if (!path.isBlank()) {
        path += '/'
        // lets try to find some project tags
        tags = gitTags(path) + tags as List<VersionSource>
    }

    tags += version

    return tags
}

private String incrementLastVersionSegment(String version) {
    def parts = version.tokenize('.')
    if (parts.last().isInteger()) {
        parts[-1] = (parts.last() as Integer) + 1
    }
    return parts.join('.')
}


class PrintVersionSourcesTask extends DefaultTask {
    @TaskAction
    def printVersion() {
        println project.path +"@"+project.version
        def versions = Git.taggedVersions(project)
        versions.each { println it.toString() }
    }
}

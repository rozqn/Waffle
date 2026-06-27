package me.dongmyeng.waffle;

import com.destroystokyo.paper.VersionHistoryManager;
import com.destroystokyo.paper.util.VersionFetcher;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import io.papermc.paper.ServerBuildInfo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.apache.logging.log4j.LogManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.slf4j.Logger;

import static io.papermc.paper.ServerBuildInfo.StringRepresentation.VERSION_SIMPLE;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.TextColor.color;

@DefaultQualifier(NonNull.class)
public final class WaffleVersionFetcher implements VersionFetcher {

    private static final Logger LOGGER = LogUtils.getClassLogger();
    private static final ComponentLogger COMPONENT_LOGGER = ComponentLogger.logger(LogManager.getRootLogger().getName());
    private static final int DISTANCE_ERROR = -1;
    private static final int DISTANCE_UNKNOWN = -2;
    private static final String REPOSITORY = "rozqn/Waffle";
    private static final String DOWNLOAD_PAGE = "https://github.com/rozqn/Waffle/releases";
    private static final ServerBuildInfo BUILD_INFO = ServerBuildInfo.buildInfo();
    private static final String USER_AGENT = BUILD_INFO.brandName() + "/" + BUILD_INFO.asString(VERSION_SIMPLE) + " (" + DOWNLOAD_PAGE + ")";
    private static final Gson GSON = new Gson();

    @Override
    public long getCacheTime() {
        return 720000;
    }

    @Override
    public Component getVersionMessage() {
        final Component updateMessage;
        if (BUILD_INFO.buildNumber().isEmpty() && BUILD_INFO.gitCommit().isEmpty()) {
            updateMessage = text("You are running a development version without access to version information", color(0xFF5300));
        } else {
            updateMessage = getUpdateStatusMessage();
        }
        final @Nullable Component history = getHistory();
        return history != null ? Component.textOfChildren(updateMessage, Component.newline(), history) : updateMessage;
    }

    private static Component getUpdateStatusMessage() {
        final int distance = releasesBehind();
        return switch (distance) {
            case DISTANCE_ERROR -> text("Error obtaining version information", NamedTextColor.YELLOW);
            case 0 -> text("You are running the latest version", NamedTextColor.GREEN);
            case DISTANCE_UNKNOWN -> text("Unknown version", NamedTextColor.YELLOW);
            default -> text("You are " + distance + " version(s) behind", NamedTextColor.YELLOW)
                .append(Component.newline())
                .append(text("Download the new version at: ")
                    .append(text(DOWNLOAD_PAGE, NamedTextColor.GOLD)
                        .hoverEvent(text("Click to open", NamedTextColor.WHITE))
                        .clickEvent(ClickEvent.openUrl(DOWNLOAD_PAGE))));
        };
    }

    public static void getUpdateStatusStartupMessage() {
        if (BUILD_INFO.buildNumber().isEmpty() && BUILD_INFO.gitCommit().isEmpty()) {
            COMPONENT_LOGGER.warn(text("*** You are running a development version without access to version information ***"));
            return;
        }
        final int distance = releasesBehind();
        if (distance > 0) {
            COMPONENT_LOGGER.warn(text("*** You are running an outdated version of Waffle, " + distance + " version" + (distance == 1 ? "" : "s") + " behind ***"));
            COMPONENT_LOGGER.warn(text("*** Download the latest build from " + DOWNLOAD_PAGE + " ***"));
        }
    }

    // How many published stable releases the running build is behind the latest, mirroring how Paper counts builds.
    private static int releasesBehind() {
        final Optional<String> gitCommit = BUILD_INFO.gitCommit();
        if (gitCommit.isEmpty()) {
            return DISTANCE_UNKNOWN;
        }
        final @Nullable List<String> releaseTags = fetchReleaseTags();
        if (releaseTags == null) {
            return DISTANCE_ERROR;
        }
        if (releaseTags.isEmpty()) {
            return DISTANCE_UNKNOWN;
        }
        final @Nullable Map<String, String> tagCommits = fetchTagCommits();
        if (tagCommits == null) {
            return DISTANCE_ERROR;
        }
        final String commit = gitCommit.get();
        for (int i = 0; i < releaseTags.size(); i++) {
            final @Nullable String sha = tagCommits.get(releaseTags.get(i));
            if (sha != null && sha.startsWith(commit)) {
                return i;
            }
        }
        return DISTANCE_UNKNOWN; // running a build that is not a published release
    }

    private static @Nullable List<String> fetchReleaseTags() {
        final @Nullable JsonArray releases = getJsonArray("https://api.github.com/repos/" + REPOSITORY + "/releases?per_page=100");
        if (releases == null) {
            return null;
        }
        final List<String> tags = new ArrayList<>();
        for (final var element : releases) {
            final JsonObject release = element.getAsJsonObject();
            if (release.get("draft").getAsBoolean() || release.get("prerelease").getAsBoolean()) {
                continue;
            }
            tags.add(release.get("tag_name").getAsString());
        }
        return tags;
    }

    private static @Nullable Map<String, String> fetchTagCommits() {
        final @Nullable JsonArray tags = getJsonArray("https://api.github.com/repos/" + REPOSITORY + "/tags?per_page=100");
        if (tags == null) {
            return null;
        }
        final Map<String, String> commits = new HashMap<>();
        for (final var element : tags) {
            final JsonObject tag = element.getAsJsonObject();
            commits.put(tag.get("name").getAsString(), tag.getAsJsonObject("commit").get("sha").getAsString());
        }
        return commits;
    }

    private static @Nullable JsonArray getJsonArray(final String url) {
        try {
            final HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept", "application/vnd.github+json");
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                return GSON.fromJson(reader, JsonArray.class);
            }
        } catch (final IOException | JsonSyntaxException e) {
            LOGGER.error("Error querying the GitHub API ({})", url, e);
            return null;
        }
    }

    private static @Nullable Component getHistory() {
        final VersionHistoryManager.@Nullable VersionData data = VersionHistoryManager.INSTANCE.getVersionData();
        if (data == null) {
            return null;
        }
        final @Nullable String oldVersion = data.getOldVersion();
        if (oldVersion == null) {
            return null;
        }
        return text("Previous version: " + oldVersion, NamedTextColor.GRAY, TextDecoration.ITALIC);
    }
}

package me.dongmyeng.waffle;

import com.destroystokyo.paper.VersionHistoryManager;
import com.destroystokyo.paper.util.VersionFetcher;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
            updateMessage = text("You are running a development version without access to version information", NamedTextColor.YELLOW);
        } else {
            updateMessage = getUpdateStatusMessage();
        }
        final @Nullable Component history = getHistory();
        return history != null ? Component.textOfChildren(updateMessage, Component.newline(), history) : updateMessage;
    }

    private static Component getUpdateStatusMessage() {
        final Optional<String> latestTag = fetchLatestReleaseTag();
        if (latestTag.isEmpty()) {
            return text("Could not check for updates", NamedTextColor.YELLOW);
        }
        final Optional<String> gitCommit = BUILD_INFO.gitCommit();
        if (gitCommit.isEmpty()) {
            return text("Unknown version", NamedTextColor.YELLOW);
        }

        final int distance = fetchDistanceFromGitHub(latestTag.get(), gitCommit.get());
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

    private static Optional<String> fetchLatestReleaseTag() {
        try {
            final HttpURLConnection connection = (HttpURLConnection) URI.create("https://api.github.com/repos/" + REPOSITORY + "/releases?per_page=1").toURL().openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept", "application/vnd.github+json");
            if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                return Optional.empty();
            }
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                final JsonArray releases = GSON.fromJson(reader, JsonArray.class);
                if (releases == null || releases.isEmpty()) {
                    return Optional.empty();
                }
                final JsonObject latest = releases.get(0).getAsJsonObject();
                return Optional.ofNullable(latest.get("tag_name")).map(JsonElement::getAsString);
            }
        } catch (final IOException | JsonSyntaxException e) {
            LOGGER.error("Error while fetching the latest release from GitHub", e);
            return Optional.empty();
        }
    }

    // Distance via GitHub's compare API: how many commits the running build is behind the latest release tag.
    private static int fetchDistanceFromGitHub(final String base, final String hash) {
        try {
            final HttpURLConnection connection = (HttpURLConnection) URI.create("https://api.github.com/repos/%s/compare/%s...%s".formatted(REPOSITORY, base, hash)).toURL().openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept", "application/vnd.github+json");
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                return DISTANCE_UNKNOWN;
            }
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                final JsonObject obj = GSON.fromJson(reader, JsonObject.class);
                final String status = obj.get("status").getAsString();
                return switch (status) {
                    case "identical", "ahead" -> 0;
                    case "behind" -> obj.get("behind_by").getAsInt();
                    default -> DISTANCE_ERROR;
                };
            }
        } catch (final IOException | JsonSyntaxException | NumberFormatException e) {
            LOGGER.error("Error while fetching version distance from GitHub", e);
            return DISTANCE_ERROR;
        }
    }

    public static void getUpdateStatusStartupMessage() {
        if (BUILD_INFO.buildNumber().isEmpty() && BUILD_INFO.gitCommit().isEmpty()) {
            COMPONENT_LOGGER.warn(text("*** You are running a development version without access to version information ***"));
            return;
        }
        final Optional<String> latestTag = fetchLatestReleaseTag();
        final Optional<String> gitCommit = BUILD_INFO.gitCommit();
        if (latestTag.isEmpty() || gitCommit.isEmpty()) {
            return;
        }
        final int distance = fetchDistanceFromGitHub(latestTag.get(), gitCommit.get());
        if (distance > 0) {
            COMPONENT_LOGGER.warn(text("*** You are running an outdated version of Waffle, " + distance + " release" + (distance == 1 ? "" : "s") + " behind ***"));
            COMPONENT_LOGGER.warn(text("*** Download the latest build from " + DOWNLOAD_PAGE + " ***"));
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

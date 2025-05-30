package com.dabi.habitv.plugin.youtube;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dabi.habitv.api.plugin.api.PluginProviderInterface;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.framework.plugin.api.BasePluginWithProxy;

public class YoutubePluginManager extends BasePluginWithProxy implements PluginProviderInterface {

    private static final Pattern ID_PATTERN = Pattern.compile(".*v=([^&]*).*");
    private static final Pattern PLAYLIST_ID_PATTERN = Pattern.compile(".*list=([^&]*).*");

    @Override
    public String getName() {
        return YoutubeConf.NAME;
    }

    @Override
    public Set<EpisodeDTO> findEpisode(final CategoryDTO category) {
        final Set<EpisodeDTO> episodes = new LinkedHashSet<>();
        if ("Playlist".equals(category.getName())) {
            findEpisodePlaylist(category, episodes);
        } else if ("Top".equals(category.getName())) {
            findEpisodeTop(category, episodes);
        } else {
            findEpisodeByUrl(category, episodes);
        }
        return episodes;
    }

    private void findEpisodePlaylist(final CategoryDTO category, final Set<EpisodeDTO> episodes) {
        try {
            // Extraire l'ID de la playlist de l'URL
            String playlistUrl = category.getId();
            Matcher matcher = PLAYLIST_ID_PATTERN.matcher(playlistUrl);
            String playlistId = "";

            if (matcher.matches()) {
                playlistId = matcher.group(1);
            } else {
                // Si l'URL ne contient pas list= directement, utiliser l'URL entière
                playlistId = playlistUrl.trim();
                if (playlistId.endsWith("/")) {
                    playlistId = playlistId.substring(0, playlistId.length() - 1);
                }
                // Si c'est une URL complète, extraire la dernière partie
                if (playlistId.contains("/")) {
                    playlistId = playlistId.substring(playlistId.lastIndexOf("/") + 1);
                }
            }

            if (playlistId.isEmpty()) {
                throw new TechnicalException("Impossible d'extraire l'ID de la playlist de l'URL: " + playlistUrl);
            }

            // Utiliser yt-dlp pour récupérer les informations de la playlist
            ProcessBuilder processBuilder = new ProcessBuilder(
                getYtDlpCommand(),
                "--flat-playlist",
                "--print",
                "%(title)s|%(id)s",
                "--no-warnings",
                "https://www.youtube.com/playlist?list=" + playlistId
            );
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|", 2);
                    if (parts.length == 2) {
                        String title = parts[0].trim();
                        String videoId = parts[1].trim();
                        EpisodeDTO episode = new EpisodeDTO(category, title, "https://www.youtube.com/watch?v=" + videoId);
                        episodes.add(episode);
                    }
                }
            }

            // Lire également la sortie d'erreur pour diagnostiquer les problèmes
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                StringBuilder errorOutput = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
                if (errorOutput.length() > 0) {
                    System.err.println("yt-dlp error output: " + errorOutput.toString());
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new TechnicalException("yt-dlp a retourné un code d'erreur: " + exitCode);
            }

            if (episodes.isEmpty()) {
                System.err.println("Aucun épisode trouvé pour la playlist: " + playlistId);
            }
        } catch (Exception e) {
            throw new TechnicalException(e);
        }
    }

    private void findEpisodeTop(final CategoryDTO category, final Set<EpisodeDTO> episodes) {
        try {
            // Utiliser yt-dlp pour récupérer les vidéos tendances
            ProcessBuilder processBuilder = new ProcessBuilder(
                getYtDlpCommand(),
                "--flat-playlist",
                "--print",
                "%(title)s|%(id)s",
                "--no-warnings",
                "https://www.youtube.com/feed/trending"
            );
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|", 2);
                    if (parts.length == 2) {
                        String title = parts[0].trim();
                        String videoId = parts[1].trim();
                        EpisodeDTO episode = new EpisodeDTO(category, title, "https://www.youtube.com/watch?v=" + videoId);
                        episodes.add(episode);
                    }
                }
            }

            // Lire également la sortie d'erreur pour diagnostiquer les problèmes
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                StringBuilder errorOutput = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
                if (errorOutput.length() > 0) {
                    System.err.println("yt-dlp error output: " + errorOutput.toString());
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new TechnicalException("yt-dlp a retourné un code d'erreur: " + exitCode);
            }

            if (episodes.isEmpty()) {
                System.err.println("Aucune vidéo trouvée dans les tendances");
            }
        } catch (Exception e) {
            throw new TechnicalException(e);
        }
    }

    private void findEpisodeByUrl(final CategoryDTO category, final Set<EpisodeDTO> episodes) {
        try {
            // Récupérer les informations de la vidéo directement
            ProcessBuilder processBuilder = new ProcessBuilder(
                getYtDlpCommand(),
                "--print",
                "%(title)s",
                "--no-warnings",
                category.getId()
            );
            Process process = processBuilder.start();

            String title = "";
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null) {
                    title = line.trim();
                }
            }

            // Lire également la sortie d'erreur pour diagnostiquer les problèmes
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                StringBuilder errorOutput = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
                if (errorOutput.length() > 0) {
                    System.err.println("yt-dlp error output: " + errorOutput.toString());
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new TechnicalException("yt-dlp a retourné un code d'erreur: " + exitCode);
            }

            if (!title.isEmpty()) {
                EpisodeDTO episode = new EpisodeDTO(category, title, category.getId());
                episodes.add(episode);
            } else {
                System.err.println("Aucun titre trouvé pour la vidéo: " + category.getId());
            }
        } catch (Exception e) {
            throw new TechnicalException(e);
        }
    }

    private String getYtDlpCommand() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return YoutubeConf.DEFAULT_WINDOWS_EXE;
        } else {
            return YoutubeConf.DEFAULT_LINUX_BIN_PATH;
        }
    }

    @Override
    public Set<CategoryDTO> findCategory() {
        final Set<CategoryDTO> categories = new LinkedHashSet<>();

        // Catégories par défaut
        categories.add(new CategoryDTO("Playlist", YoutubeConf.NAME, "Entrez une URL de playlist YouTube", "https://www.youtube.com/playlist?list="));
        categories.add(new CategoryDTO("Top", YoutubeConf.NAME, "Top videos", "https://www.youtube.com/feed/trending"));

        return categories;
    }
}

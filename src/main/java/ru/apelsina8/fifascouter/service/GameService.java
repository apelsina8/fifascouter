package ru.apelsina8.fifascouter.service;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import ru.apelsina8.fifascouter.rtmp.RtmpReader;
import ru.apelsina8.fifascouter.recognizer.ScoreRecognizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

@Service
@Scope("prototype")
public class GameService {

    @Autowired
    private ScoreRecognizer scoreRecognizer;

    private final RtmpReader rtmpReader;

    private int firstTeamScore;
    private int secondTeamScore;
    private String time = "00:00";
    private String lastTime = "00:00";
    private String score = "0-0";
    private final List<String> goalHistory = new ArrayList<>();


    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final ConcurrentHashMap<WebSocketSession, String> sessionMatchMap = new ConcurrentHashMap<>();
    private String currentMatchUrl;


    private final LinkedList<Integer> firstTeamScoreBuffer = new LinkedList<>();
    private final LinkedList<Integer> secondTeamScoreBuffer = new LinkedList<>();
    private final LinkedList<String> timeBuffer = new LinkedList<>();
    private final int bufferSize = 49;  // Количество последних значений для хранения

    @Autowired
    public GameService(RtmpReader rtmpReader) {
        this.rtmpReader = rtmpReader;
    }

    // Добавление данных в буфер
    private void addToBuffer() {
        if (firstTeamScoreBuffer.size() >= bufferSize || secondTeamScoreBuffer.size() >= bufferSize) {
            firstTeamScoreBuffer.poll();  // Удаляем старейший элемент, если буфер переполнен
            secondTeamScoreBuffer.poll();
            timeBuffer.poll();
        }
        firstTeamScoreBuffer.add(firstTeamScore);
        secondTeamScoreBuffer.add(secondTeamScore);
        timeBuffer.add(time);
    }

    // Получение данных с буфером
    public int getBufferedFirstTeamScore() {
        // Отдаем последний элемент из буфера (самые старые данные)
        return firstTeamScoreBuffer.isEmpty() ? 0 : firstTeamScoreBuffer.getFirst();
    }

    public int getBufferedSecondTeamScore() {
        // Отдаем последний элемент из буфера (самые старые данные)
        return secondTeamScoreBuffer.isEmpty() ? 0 : secondTeamScoreBuffer.getFirst();
    }

    public String getBufferedTime() {
        return timeBuffer.isEmpty() ? "00:00" : timeBuffer.getFirst();
    }


    public void start(String url) {
        try {
            this.currentMatchUrl = url;
            rtmpReader.setUrl(url);
            // Запускаем захват кадров в отдельном потоке
            executor.submit(this::captureFrames);

        } catch (FFmpegFrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addSession(WebSocketSession session) {
        sessionMatchMap.put(session, currentMatchUrl);
    }

    private void sendMatchData(int firstTeamScore, int secondTeamScore, String time) {
        String matchData = "Time: " + time + ", Score: " + firstTeamScore + "-" + secondTeamScore;
        String goals = String.join("<br>", goalHistory);

        String fullMessage = matchData + "<br>" + goals;

        sessionMatchMap.forEach((session, subscribedStreamUrl) -> {
            if (subscribedStreamUrl.equals(currentMatchUrl)) {
                try {
                    session.sendMessage(new TextMessage(fullMessage));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }



    // Метод для захвата кадров
    private void captureFrames() {
        try {
            while (true) {
                BufferedImage image = rtmpReader.getFrameImage();
                if (image != null) {
                    // Отправляем кадры на обработку в другом потоке
                    executor.submit(() -> processFrame(image));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Метод для обработки кадра
    private void processFrame(BufferedImage image) {
        try {
            time = scoreRecognizer.recognizeTime(image);
            score = scoreRecognizer.recognizeScore(image);

            if (score != null && score.matches("\\d+-\\d+")) {
                String[] teamsScore = score.split("-");
                int newFirstTeamScore = Integer.parseInt(teamsScore[0]);
                int newSecondTeamScore = Integer.parseInt(teamsScore[1]);

                if (newFirstTeamScore > firstTeamScore) {
                    goalHistory.add("Goal! Team 1 scored at " + time);
                }
                if (newSecondTeamScore > secondTeamScore) {
                    goalHistory.add("Goal! Team 2 scored at " + time);
                }

                firstTeamScore = newFirstTeamScore;
                secondTeamScore = newSecondTeamScore;

                addToBuffer();
                sendMatchData(firstTeamScore, secondTeamScore, time);

                if (!lastTime.equals(time)) {
                    System.out.println("!!!Время: " + time + ", Счёт: " + firstTeamScore + "-" + secondTeamScore);
                    lastTime = time;
                }
            } else {
                System.out.println("Не удалось распознать счёт для кадра. Последний известный счёт: " + firstTeamScore + "-" + secondTeamScore);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void saveScreen(BufferedImage image) {
        String userDir = System.getProperty("user.dir");
        File directory = new File(userDir + "/src/main/resources/screenshots");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, System.currentTimeMillis() + ".png");
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void clearScreenshotsFolder() {
        String userDir = System.getProperty("user.dir");
        File directory = new File(userDir + "/src/main/resources/screenshots");

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        boolean deleted = file.delete();
                        if (deleted) {
                            System.out.println("Удалён файл: " + file.getName());
                        } else {
                            System.out.println("Не удалось удалить файл: " + file.getName());
                        }
                    }
                }
            }
        }
    }

    public int getFirstTeamScore() {
        return firstTeamScore;
    }

    public int getSecondTeamScore() {
        return secondTeamScore;
    }

    public String getTime() {
        return time;
    }

    // Завершаем пул потоков
    public void shutdown() {
        executor.shutdown();
    }
}

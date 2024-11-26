//package ru.apelsina8.fifascouter.controller;
//
//import jakarta.annotation.PostConstruct;
//import jakarta.annotation.PreDestroy;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import ru.apelsina8.fifascouter.dto.ScoreResponseDTO;
//import ru.apelsina8.fifascouter.service.GameService;
//
//@RestController
//@RequestMapping("/scouter")
//public class Controller {
//
//    private final GameService gameService;
//
//    public Controller(GameService gameService) {
//        this.gameService = gameService;
//    }
//
//    @PostConstruct
//    public void init() {
//        new Thread(gameService::start).start();
//    }
//
//    @PreDestroy
//    public void destroy() {
//        GameService.clearScreenshotsFolder();
//    }
//
//    @GetMapping("/score")
//    public ScoreResponseDTO  getScore() {
//        return new ScoreResponseDTO(gameService.getBufferedTime(), gameService.getBufferedFirstTeamScore(), gameService.getBufferedSecondTeamScore());
//    }
//}

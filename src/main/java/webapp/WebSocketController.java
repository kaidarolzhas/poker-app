package webapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.springframework.http.HttpEntity;
import webapp.data.dto.LoginRequestDto;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import poker.Player;
import poker.Turn;
import webapp.data.dto.RegistrationRequestDto;
import webapp.service.AuthService;


import javax.naming.InvalidNameException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("SameReturnValue")
@EnableScheduling
@EnableAsync
@Controller

public class WebSocketController implements Observer {
    private final SimpMessageSendingOperations messagingTemplate;
    private final List<LobbyImpl> lobbies;
    private final UserRepository userRepository;

    private final AuthService service;


    @Autowired
    public WebSocketController(SimpMessageSendingOperations messagingTemplate, UserRepository userRepository, AuthService service) {
        this.messagingTemplate = messagingTemplate;
        this.service = service;
        this.lobbies = new LinkedList<>();
        this.lobbies.add(new LobbyImpl());
        this.lobbies.get(0).addObserver(this);
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void start() {
        lobbies.get(0).start();
    }

    @MessageMapping("/sendTurn")
    public void processTurnFromClient(@Payload String turn) {
        String player = null;
        try {
            JSONObject messageAsJSON = new JSONObject(turn);
            player = (String) messageAsJSON.getJSONObject("message").get("player");
            Turn.PlayerAction action = Turn.PlayerAction.valueOf(((String) messageAsJSON.getJSONObject("message").get("action")).toUpperCase(Locale.ROOT));
            int amount = (int) messageAsJSON.getJSONObject("message").get("betAmount");

            lobbies.get(0).receiveTurn(new Turn(
                    lobbies.get(0).getPlayerFromName(player),
                    action,
                    amount));
        } catch (JSONException ignored) {
        } catch (IllegalArgumentException e) {
            if (player != null)
                messagingTemplate.convertAndSend("/poker/" + player + "/error", "\"" + e.getMessage() + "\"");
        }
        this.refreshAllPlayers();
    }

    private void sendGameData(Player p) {
        while (lobbies.get(0).getControllerState() == TableController.State.PROCESSING) {
            lobbies.get(0).awaitReady();
        }
        GameData state = lobbies.get(0).getState(p);
        try {
            String serializedState = (new ObjectMapper()).writeValueAsString(state);
            messagingTemplate.convertAndSend("/poker/" + p.getName(), serializedState);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedRate = 8000)
    public void refreshAllPlayers() {
        lobbies.get(0).getPlayerSet().forEach(this::sendGameData);
    }

    @MessageExceptionHandler
    public String handleException(Throwable exception) {
        System.out.println(exception.getMessage());
        //messagingTemplate.convertAndSend("/errors", exception.getMessage());
        return exception.getMessage();
    }

    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login.html";
    }
    @PostMapping("/signup")
    public ResponseEntity<?> registration(@RequestBody @Valid RegistrationRequestDto registrationRequest) {
        service.registration(registrationRequest);
        return new ResponseEntity<>(HttpEntity.EMPTY, HttpStatus.OK);
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequest) {
        String username = loginRequest.getUsername();
        if (lobbies.get(0).getPlayerSet().stream().map(Player::getName).collect(Collectors.toSet()).contains(username)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (service.login(loginRequest).getAccessToken() != null) {
            if (userRepository.getBalance(username) >= 250) {
                lobbies.get(0).addPlayer(new Player(username, 250));
                userRepository.withdraw(username, 250);
            } else {
                lobbies.get(0).addPlayer(new Player(username, 250));
            }
            return ResponseEntity.ok(service.login(loginRequest));
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/signUp")
    public String signUp(Model model) {
        model.addAttribute("signUpForm", new SignUpForm());
        return "signUp";
    }

    @PostMapping("/signUp")
    public String signUpSubmit(@ModelAttribute SignUpForm data, Model model) {
        model.addAttribute("signUpForm", new SignUpForm());
        try {
            userRepository.register(data.getName(), data.getPassword());
            System.out.println("registered");
        } catch (InvalidNameException e) {
            model.addAttribute("error", "Error: " + e.getMessage());
        }
        return "signUp";
    }

    @EventListener
    public void onDisconnectEvent(SessionDisconnectEvent event) {
        MessageHeaders headers = event.getMessage().getHeaders();
        String sessionID = (String) headers.get("simpSessionId");
        try {
            Player toBeRemoved = lobbies.get(0).getPlayerFromSessionID(sessionID);
            userRepository.deposit(toBeRemoved.getName(), toBeRemoved.getChips());
            lobbies.get(0).removePlayer(toBeRemoved);
        } catch (NoSuchElementException ignored) {
        }
        this.refreshAllPlayers();
    }

    @EventListener
    public void onSubscribeEvent(SessionSubscribeEvent event) {
        MessageHeaders headers = event.getMessage().getHeaders();
        if (!((String) headers.get("simpDestination")).contains("error")) {
            String sessionID = (String) headers.get("simpSessionId");
            String user = (((String) headers.get("simpDestination")).split("/")[2]);
            lobbies.get(0).getPlayerFromName(user).setWebsocketsSession(sessionID);
        }
        this.refreshAllPlayers();
    }

    @Override
    public void update(Observable o, Object arg) {
        this.refreshAllPlayers();
    }
}

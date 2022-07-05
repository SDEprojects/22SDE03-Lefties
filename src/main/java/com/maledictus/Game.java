package com.maledictus;

import com.maledictus.item.Item;
import com.maledictus.item.ItemType;
import com.maledictus.item.key.Key;
import com.maledictus.music.BattleMusic;
import com.maledictus.music.GameMusic;
import com.maledictus.npc.Ghost;
import com.maledictus.npc.NPC;
import com.maledictus.player.Player;
import com.maledictus.player.PlayerFactory;
import com.maledictus.room.Room;
import com.maledictus.room.RoomFactory;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;

import static com.maledictus.Input.scannerUserInput;
import static com.maledictus.Json.items6;
import static com.maledictus.Json.returnGameText;

public class Game {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    private static final Map<String, Room> roomMap = RoomFactory.getRoomMap();
    private static Map<Integer, NPC> npcMap;
    private static Player playerOne;
    private static ArrayList<Item> roomItems;
    private static Map<String, String> roomDirections;
    private static Room currentRoom;
    private static String errorMsg = null;
    private static String successMsg = null;
    private static boolean inBattle = false;
    private static Battle battle;
    private static int battleEnemy;
    private static Game instance;
    private  final GameMusic gameMusic = new GameMusic();
    private final BattleMusic battleMusic = new BattleMusic();
    public boolean doesPlayerHaveHellBlade= false;
    public boolean hasPlayerWon = false;//boolean to measure win game for getting the sword
    public boolean hasPlayerLost = false;//boolean for loss, that happens when Hp hits 0

    public Game() throws UnsupportedAudioFileException, LineUnavailableException, IOException {
    }


    public static Game getInstance() throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        if(instance == null) {
            instance = new Game();
        }
        return instance;
    }

    private static void displayIntroText() throws IOException, org.json.simple.parser.ParseException {
        Printer.print(returnGameText("1") + "\n" + returnGameText("11")+ "\n");
    }

    public void initiateGame() throws IOException, org.json.simple.parser.ParseException, ParseException, UnsupportedAudioFileException, LineUnavailableException {
        Json.jsonWrite();
//        displaySplash();
//        createCharacter();
        Printer.print(returnGameText("2") + "\n");
        startGame();
        Json.createItems();
        Json.createNPCs();
        Json.createRoomList();
        currentRoom = roomMap.get("Great Hall");
//        gameMusic.playMusic();
//        start();

    }

    public void createCharacter() {
        Printer.print("Enter your character's name:");
        GUI gui = GUI.getInstance();
        gui.getInputtedUser().addActionListener(e -> {
            String text = gui.getInputtedUser().getText();
            gui.getInputtedUser().setText("");
            gui.getInputtedUser().removeActionListener(gui.getInputtedUser().getActionListeners()[0]);

            playerOne = PlayerFactory.createPlayer(text);
            try {
                start();
            } catch (IOException | org.json.simple.parser.ParseException | ParseException | UnsupportedAudioFileException | LineUnavailableException | InterruptedException ex) {
                ex.printStackTrace();
            }
        });

    }

    public void startGame() throws IOException, org.json.simple.parser.ParseException {
        GUI gui = GUI.getInstance();
        System.out.println("---------------- START GAME FUNCTION ---------------");
        Printer.print(returnGameText("3"));
        Printer.print(returnGameText("4"));
        gui.getInputtedUser().addActionListener(e -> {
            String text = gui.getInputtedUser().getText();
            gui.getInputtedUser().setText("");
//            setUserInput(text);
//            Input.setPerformed(true);
            try {
                gui.getInputtedUser().removeActionListener(gui.getInputtedUser().getActionListeners()[0]);
                validatePlayerInput(text);
            } catch (IOException | org.json.simple.parser.ParseException ex) {
                ex.printStackTrace();
            }
        });

    }

    public String validatePlayerInput(String startGame) throws IOException, org.json.simple.parser.ParseException {
//        ---------- VALIDATE PLAYER INPUT ---------
        if (startGame.equals("1")) {
            displayIntroText();
            createCharacter();
        } else if (startGame.equals("2")) {
            Printer.print("Exiting the game...");
            System.exit(0);
        } else {
            errorMsg = ANSI_RED + "Invalid Selection.  Please enter [1] to start game or [2] to quit." + ANSI_RESET;
            startGame();
        }
        return startGame;
    }

//TODO: function may be obsolete given GUI implementation
    public void displayGameMap() {
        boolean invalidSelection = true;
        while (invalidSelection) {
            String gameMap = null;
            Printer.print("Enter [1] for Main floor map, [2] for downstairs map");
            try {
                String displayMap = scannerUserInput();
                if (displayMap.equals("1")) {
                    gameMap = Files.readString(Path.of("resources/data/mainfloor_map.txt"));
                    Printer.print(gameMap);
                    invalidSelection = false;
                } else if (displayMap.equals("2")) {
                    gameMap = Files.readString(Path.of("resources/data/downstairs-map.txt"));
                    Printer.print(gameMap);
                    invalidSelection = false;
                } else {
                    Printer.print(ANSI_RED + "Invalid Selection.  Please enter [1] to display Main floor map, or  [2] to display downstairs map." + ANSI_RESET);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void start() throws IOException, org.json.simple.parser.ParseException, ParseException, UnsupportedAudioFileException, LineUnavailableException, InterruptedException {

        if(doesPlayerHaveTheHellBlade()){
            System.out.println("you actually have the award? ");
            Thread.sleep(2000);
            System.out.println("the spirit of tornomous awakens");
            Thread.sleep(2000);
            System.out.println(" a the hellblade begins to grow larger and larger, with eyes appearing on the hilt \n why thank you...");
            Thread.sleep(2000);
            System.out.println("been a while since I've seen the sun, ever since he took me to die with him... this dark decrepit place");
            Thread.sleep(3000);
            System.out.println("Can you take me to see the sun?");
            Thread.sleep(3000);
            System.out.println("YOU WON GAME OVER");
            System.exit(0);
        }else{
            System.out.println("game ongoing");
        }
        //        -------- START FUNCTION -------
        boolean round = true;
        if (playerOne.getHitPoints() == 0) {
            round = false;
            Printer.print("You're dead, D. E. D.");
        }
//        if (validateIfPlayerWonBasedONIfTheyHveTheHellBlade()){
//            Printer.print("OMG you do have the hell blade");
//            setDoesPlayerHaveHellBlade(true);
//            Printer.print("omg you won");
//            setHasThePlayerWon(true);
////            create timer then exit!
//            System.exit(0);
//                }else{
//            System.out.println("not winning yet love..");
//        }
        boolean test = round && this.inBattle;
        System.out.println("BATLLE " + test);

        if (round && !this.inBattle) {
            displayConsoleCommands();
            // Methods will check if an error or success message needs to be printed
            printSuccessMsg();
            printErrorMsg();
            Printer.print("\n" + ANSI_YELLOW + "Enter a command or enter [options] to see game options: " + ANSI_RESET);

            // Take in user input and run through scanner
//            String userCommand;

            GUI gui = GUI.getInstance();

            gui.getInputtedUser().addActionListener(e -> {
                String userCommand = gui.getInputtedUser().getText();
                gui.getInputtedUser().setText("");
                gui.getInputtedUser().removeActionListener(gui.getInputtedUser().getActionListeners()[0]);

                // Splitting userCommand into two separate strings. (Verb, Noun)
                String[] userInput = userCommand.split(" ", 2);
                try {
                    getUserInput(userInput);
//                    System.out.println("CALL TO START IN START");
//                    start();
                } catch (UnsupportedAudioFileException | LineUnavailableException | IOException | org.json.simple.parser.ParseException | ParseException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            });
        }

        if (round && this.inBattle) {
            displayBattleCommands();
            npcMap.get(battleEnemy).setItem(items6.get(0));
            this.battle.battleStart();
//            fight(tester);
//            boolean fight = this.battle.start();
//            System.out.println("PRE FIGHT CHECK");
//            while(fight) {
//                System.out.println("RESETTING COMBAT TO FALSE");
//                this.battle.setCombat(false);
//                if (!this.battle.isCombat()) {
//                    System.out.println("NOT IN BATTLE");
//                    this.inBattle = false;
//                }
//                this.battleMusic.stopMusic();
////            GameMusic.playMusic();
//                npcMap.remove(battleEnemy);
//                try {
//                    this.start();
//                } catch (IOException | org.json.simple.parser.ParseException | ParseException | UnsupportedAudioFileException | LineUnavailableException ex) {
//                    ex.printStackTrace();
//                }
//            }


        }
//        boolean round = true;
//        if (playerOne.getHitPoints() == 0) {
//            round = false;
//            Printer.print("You're dead, D. E. D.");
//        }
//        while (round && !this.inBattle) {
////            check if they're in the throne room to intitiate winning validation
//
//
//            displayConsoleCommands();
//            // Methods will check if an error or success message needs to be printed
//            printSuccessMsg();
//            printErrorMsg();
//            Printer.print("\n" + ANSI_YELLOW + "Enter a command or enter [options] to see game options: " + ANSI_RESET);
//
//            // Take in user input and run through scanner
//            String userCommand = scannerUserInput();
//
//            // Splitting userCommand into two separate strings. (Verb, Noun)
//            String[] userInput = userCommand.split(" ", 2);
//
//            // Check to see if user input is expected array format
//
//            getUserInput(userInput);
//            if(Objects.equals(currentRoom.getName(), "Throne Room")){
//                if(validateIfPlayerHasZeroHP()){
//                    Printer.print("YOU LOSE");
//                }else{
//                    Printer.print("may you keep being alive");
//                    System.out.println(doesPlayerHaveTheHellBlade()+ " should be true ") ;
//                }
//
//
//                if (validateIfPlayerWonBasedONIfTheyHveTheHellBlade()){
//                    Printer.print("OMG you do have the hell blade");
//                    setDoesPlayerHaveHellBlade(true);
//                    Printer.print("omg you won");
//                    setHasThePlayerWon(true);
////            create timer then exit!
//                }else{
//                    Printer.print("guess you don't have it");
//                }
//            }
//        }
//        while (round && this.inBattle) {
//            displayBattleCommands();
//            this.battle.start();
//            this.battle.setCombat(false);
//            if(this.battle.areThereSpoils()){
//                this.roomItems.add(this.battle.getSpoilsOfWar());
////
//            if(!this.battle.isCombat()) {
//                this.inBattle = false;
//            }
//
////                playerOne.addItem(this.battle.getSpoilsOfWar());
////             the idea is to do validation for if the item is in the player's inventory. if it's there, then the player wins, if it's false, then the player continues playing.
////                for (Item item: playerOne.getInventory()
////                     ) {
////
////                }
//            }
//            this.battleMusic.stopMusic();
//            this.gameMusic.playMusic();
//            Printer.print("is it getting here?");
//            npcMap.remove(battleEnemy);
//            this.start();
//            // Take in user input and run through scanner
//            // String userCommand = scannerUserInput();
//
//            // Splitting userCommand into two separate strings. (Verb, Noun)
//            // String[] userInput = userCommand.split(" ", 2);
//
//            // Check to see if user input is expected array format
//
//            // getUserInput(userInput);
//        }
    }

    public void endfight() {

        this.battle.setCombat(false);
        if (!this.battle.isCombat()) {

            this.inBattle = false;
        }
        if(Objects.equals(npcMap.get(battleEnemy).getName(), "The King")){
            currentRoom.addItem(npcMap.get(battleEnemy).getItem());
            System.out.println("item added to room");
        }
            BattleMusic.stopMusic();
            GameMusic.playMusic();
        npcMap.remove(battleEnemy);

        try {
            this.start();
        } catch (IOException | org.json.simple.parser.ParseException | ParseException | UnsupportedAudioFileException | LineUnavailableException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private void dropItem(String[] userInput) {
        boolean itemFound = false;
        for(Item item : playerOne.getInventory().values()) {
            if(item.getName().equalsIgnoreCase(userInput[1])) {
                itemFound = true;
                playerOne.removeItem(item);
                roomItems.add(item);
                successMsg = item.getName() + " was dropped from your inventory.";
                break;
            }
        }
        if (!itemFound) {
            Printer.print(ANSI_RED+ "INVALID ITEM ERROR: You wrote take '" + userInput[1] + "' that is not a valid item option, please try again. (Example: 'take iron sword')" + ANSI_RESET);
        }
        try {
            start();
        } catch (IOException | org.json.simple.parser.ParseException | ParseException | UnsupportedAudioFileException | LineUnavailableException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private void inspectItem(String[] userInput) {
        boolean itemFound = false;

            // Search for item through player inventory
            for (Map.Entry<String, Item> item : playerOne.getInventory().entrySet()) {
                if(userInput[1].equalsIgnoreCase(item.getKey())) {
                    itemFound = true;
                    successMsg = "Inspect: " +  item.getValue().getDescription();
                    break;
                }
            }
            if (!itemFound) {
                for (Item item : roomItems) {
                    if(userInput[1] != null && item.getName().equalsIgnoreCase(userInput[1])) {
                        itemFound = true;
                        successMsg = "Inspect: " + item.getDescription();
                        break;
                    }
                }
            }

            if (!itemFound) {
                errorMsg = ANSI_RED + "INVALID ITEM ERROR: You wrote inspect '" + userInput[1] + "' that is not a valid item option, please try again. (Example: 'take iron sword')" + ANSI_RESET;
            }
        try {
            start();
        } catch (IOException | org.json.simple.parser.ParseException | ParseException | UnsupportedAudioFileException | LineUnavailableException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private void takeItem(String[] userInput) {
        boolean itemFound = false;
        for (Item item : roomItems) {
            if(userInput[1] != null && item.getName().equalsIgnoreCase(userInput[1])) {
                itemFound = true;
                playerOne.addItem(item);
                roomItems.remove(item);
                successMsg = item.getName() + " was added to your inventory.";
                break;
            }
        }
        if (!itemFound) {
            errorMsg = ANSI_RED+ "INVALID ITEM ERROR: You wrote take '" + userInput[1] + "' that is not a valid item option, please try again. (Example: 'take iron sword')" + ANSI_RESET;
        }
        try {
            start();
        } catch (IOException | org.json.simple.parser.ParseException | ParseException | UnsupportedAudioFileException | LineUnavailableException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private void useItem(String[] userInput) {
        boolean itemFound = false;
        System.out.println("PLAYER HEALTH: " + playerOne.getHitPoints());
        for (Item item : playerOne.getInventory().values()) {
            if(userInput[1] != null && item.getName().equalsIgnoreCase(userInput[1])) {
                itemFound = true;
                item.use();
//                TODO: fix potion
                if(item.getItemType() == ItemType.POTION) {

                }
                playerOne.removeItem(item);
                break;
            }
        }
        if(!itemFound) {
            errorMsg = userInput[1] + " is not in your inventory!";
        }
        try {
            System.out.println("PLAYER HEALTH: " + playerOne.getHitPoints());
            start();
        } catch (IOException | org.json.simple.parser.ParseException | ParseException | UnsupportedAudioFileException | LineUnavailableException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private void moveRoom (String[] userInput) {
        boolean roomFound = false;
            roomDirections = currentRoom.getDirections();
            for (Map.Entry<String, String> direction : roomDirections.entrySet()) {
                Room targetRoom = roomMap.get(direction.getValue());
                String targetDirection = direction.getKey();
                if (userInput[1].equalsIgnoreCase(targetDirection) && targetRoom.isLocked()) {
                    roomFound = true;
                    Key foundKey = playerOne.getDoorKey(targetRoom.getRequiredKeyType());
                    if (foundKey != null) {
                        targetRoom.unlockRoom(foundKey.getKeyType());
                        currentRoom = roomMap.get(direction.getValue());
                        roomItems = currentRoom.getItems();
                        successMsg = "You used the " + foundKey.getName() + " and unlocked the door to the " + targetRoom.getName() + ".\nYou went " + targetDirection + " into the " + targetRoom.getName();
                    } else {
                        successMsg = ANSI_RED + "WARNING: You tried to go to the room located in the " + targetDirection + " direction. The door to this room is locked, you must find the proper key first. \nCome back when you have the right key." + ANSI_RESET;
                        break;
                    }
                } else if (userInput[1].equalsIgnoreCase(targetDirection) && !targetRoom.isLocked()) {
                    roomFound = true;
                    currentRoom = roomMap.get(direction.getValue());
                    roomItems = currentRoom.getItems();
                    successMsg = "You went " + targetDirection + " into the " + targetRoom.getName();
                }
            }
            if (!roomFound) {
                errorMsg = ANSI_RED + "INVALID LOCATION ERROR: You wrote go '" + userInput[1] + "' that is not a valid room option, please try again. (Example: 'go north')" + ANSI_RESET;
            }
        try {
            start();
        } catch (IOException | org.json.simple.parser.ParseException | ParseException | UnsupportedAudioFileException | LineUnavailableException | InterruptedException ex) {
            ex.printStackTrace();
        }
        }


    private void getUserInput(String[] userInput) throws UnsupportedAudioFileException, LineUnavailableException, IOException, ParseException, ParseException, org.json.simple.parser.ParseException, InterruptedException {

        // Making sure the user uses the valid syntax of "verb[word]" + SPACE + "noun[word(s)]" (example: take Iron Sword)
        String input = userInput[0].toLowerCase();
        switch (input) {
            case "go" :
                moveRoom(userInput);
                break;
            case "take" :
                takeItem(userInput);
                break;
            case "inspect" :
                inspectItem(userInput);
                break;
            case "drop":
                dropItem(userInput);
                break;
            case "use":
                useItem(userInput);
                break;
            case "heal":
                useItem(userInput);
                break;
            case "talk" :
                talkToNpc(userInput);
                break;
            case "options" :
                displayOptions();
                break;
            case "battle" :
                Map<Integer, NPC> currentNPCs = currentRoom.getNpcMap();
                for(NPC npc : currentNPCs.values()) {
                    String current = npc.getName();
                    if(current.equalsIgnoreCase(userInput[1]) && npc.getIsHostile()) {
                        this.inBattle = true;
                        this.battleEnemy = npc.getId();
                        battle = new Battle(playerOne, npc);
                        gameMusic.stopMusic();
                        battleMusic.playMusic();
//                        call to start - may not work
                        start();
                    }
                }
                break;
            default:
                errorMsg = ANSI_RED + "INVALID ACTION ERROR: user input of '" + userInput[0] + "' is an invalid action input. (Example: 'go', 'take')" + ANSI_RESET;
                start();
                break;
        }

//            if(userInput[0].equalsIgnoreCase("go")) {
//                moveRoom(userInput);
//            } else if(userInput[0].equalsIgnoreCase("take")) {
//                takeItem(userInput);
//            } else if(userInput[0].equalsIgnoreCase("inspect")) {
//                inspectItem(userInput);
//            } else if(userInput[0].equalsIgnoreCase("drop")) {
//                dropItem(userInput);
//            } else if(userInput[0].equalsIgnoreCase("use")) {
//                useItem(userInput);
//            } else if(userInput[0].equalsIgnoreCase("heal")) {
//                useItem(userInput);
//            } else if(userInput[0].equalsIgnoreCase("talk")) {
//                talkToNpc(userInput);
//            } else if(userInput[0].equalsIgnoreCase("options")) {
//                displayOptions();
//            } else if(userInput[0].equalsIgnoreCase("battle")) {
//                Map<Integer, NPC> currentNPCs = currentRoom.getNpcMap();
//                for(NPC npc : currentNPCs.values()) {
//                    String current = npc.getName();
//                    if(current.equalsIgnoreCase(userInput[1]) && npc.getIsHostile()) {
//                        this.inBattle = true;
//                        this.battleEnemy = npc.getId();
//                        System.out.println("CREATED NEW BATTLE");
//                        battle = new Battle(playerOne, npc);
//                        gameMusic.stopMusic();
//                        battleMusic.playMusic();
////                        call to start - may not work
//                        start();
//                    }
//                }
//            } else {
//                errorMsg = ANSI_RED + "INVALID ACTION ERROR: user input of '" + userInput[0] + "' is an invalid action input. (Example: 'go', 'take')" + ANSI_RESET;
//                start();
//            }
    }

//return boolean value after validating if character has that or not
    public boolean doesPlayerHaveTheHellBlade() {
        return playerOne.getInventory().containsKey("Hell Blade");
    }


    public boolean isKingDead(){
        return npcMap.get(battleEnemy).getName() == "The King";
    }

//    getter
    public boolean returnDoesPlayerHaveHellBlackField(){
        return doesPlayerHaveHellBlade;
    }

//    setter
    public void setDoesPlayerHaveHellBlade(boolean doesPlayerHaveHellBlade) {
        this.doesPlayerHaveHellBlade = doesPlayerHaveHellBlade;
    }

//    validation for if the player won
    public boolean validateIfPlayerWonBasedONIfTheyHveTheHellBlade(){
        return (doesPlayerHaveTheHellBlade() && isKingDead()) ;
    }

//getter
    public boolean hasThePlayerWon() {
        return hasPlayerWon;
    }

///setter throw the validate IfPlayer one into the param.
    public void setHasThePlayerWon(boolean hasPlayerWon) {
        this.hasPlayerWon = hasPlayerWon;
    }

//validate if the player has died
    public boolean validateIfPlayerHasZeroHP(){
        return playerOne.getHitPoints() < 0;
    }

    public boolean returnHasThePlayerDied() {
        return hasPlayerLost;
    }

//    public void main(String[] args) {
//        if(validateIfPlayerHasZeroHP()){
//            Printer.print("YOU LOSE");
//        }else{
//            Printer.print("may keep being alive");
//        }
//
//        if (validateIfPlayerWonBasedONIfTheyHveTheHellBlade()){
//            Printer.print("OMG you do have the hell blade");
//            setDoesPlayerHaveHellBlade(true);
//            Printer.print("omg you won");
//            setHasThePlayerWon(true);
////            create timer then exit!
//        }
//    }
    public void setHasThePlayerDied(boolean hasPlayerLost) {
        this.hasPlayerLost = hasPlayerLost;
    }

    private void talkToNpc(String[] userInput) throws UnsupportedAudioFileException, LineUnavailableException, IOException, org.json.simple.parser.ParseException, ParseException, InterruptedException {
        boolean npcFound = false;
        for (Map.Entry<Integer, NPC> npc : npcMap.entrySet()) {
            if (userInput[1].equalsIgnoreCase(npc.getValue().getName())) {
                NPC targetNpc = npc.getValue();
                chooseDialog(targetNpc);
                npcFound = true;
                break;
            }
        }
        if (!npcFound) {
            errorMsg = ANSI_RED + "INVALID NPC ERROR: You wrote talk '" + userInput[1] + "' that is not a valid NPC option, please try again. (Example: 'talk ghostly soldier')" + ANSI_RESET;
        }
    }

    private void chooseDialog(NPC targetNpc) throws UnsupportedAudioFileException, LineUnavailableException, IOException, org.json.simple.parser.ParseException, ParseException, InterruptedException {
        Ghost npc = (Ghost) targetNpc;
        GUI gui = GUI.getInstance();


        if (npc.getQuest() == null || (!npc.getQuestStatus() && !npc.getQuest().isCompleted())) {

            Printer.print(npc.getName() + ": " + npc.talk(1));
            if (npc.getQuest() != null) {
                Printer.print("Press [1] Accept Quest? \nPress [2] to exit.");
                gui.getInputtedUser().addActionListener(e -> {
                    String questDialogChoice = gui.getInputtedUser().getText();
                    gui.getInputtedUser().setText("");
                    gui.getInputtedUser().removeActionListener(gui.getInputtedUser().getActionListeners()[0]);
                    if (questDialogChoice.equals("1")) {
                        System.out.println("===== ASSIGNING QUEST TO TRUE =====");
                        npc.assignQuest(true);
                        try {
                            start();
                        } catch (IOException | org.json.simple.parser.ParseException | UnsupportedAudioFileException | ParseException | LineUnavailableException | InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
            else {
                start();
            }
        }
        // active quest
        else if (npc.getQuestStatus() && !npc.getQuest().isCompleted()) {
            System.out.println("DEBUG:::: ACTIVE QUEST ::::");
            successMsg = npc.getName() + ": " + npc.questTalk(1);
            Printer.print("Press [1] give " + npc.getQuestWinCondition() + "\nPress [2] to exit.");
            gui.getInputtedUser().addActionListener(e -> {
                String questDialogChoice = gui.getInputtedUser().getText();
                gui.getInputtedUser().setText("");
                gui.getInputtedUser().removeActionListener(gui.getInputtedUser().getActionListeners()[0]);
                if (questDialogChoice.equals("1") && playerOne.getInventory().containsKey(npc.getQuestWinCondition())) {
                    playerOne.removeItem(playerOne.getInventory().get(npc.getQuestWinCondition()));
                    successMsg = npc.getName() + ": " + npc.questTalk(2) + "\nYou received a(n) " + npc.getQuest().getReward().getName() + " from " + npc.getName();
                    npc.setQuestCompleted(true);
                    npc.assignQuest(false);
                    playerOne.addItem(npc.giveQuestReward());
                    Printer.print("\nYou received a(n) " + npc.getQuest().getReward().getName() + " from " + npc.getName());
                    try {
                        start();
                    } catch (IOException | org.json.simple.parser.ParseException | ParseException | UnsupportedAudioFileException | LineUnavailableException | InterruptedException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    successMsg = npc.getName() + ": " + npc.questTalk(3);
                    try {
                        start();
                    } catch (IOException | org.json.simple.parser.ParseException | ParseException | UnsupportedAudioFileException | LineUnavailableException | InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            });

        } else {
            successMsg = npc.getName() + ": " + npc.questTalk(4);
            start();
        }

    }

    private void displayOptions() {
        GUI gui = GUI.getInstance();
        Printer.print("Press [1] to start a new game.\nPress [2] to quit.\nPress [3] for game info.\nPress [4] to stop Music.\nPress [5] to play Music.\nPress [6] to display game maps.\nPress [7] to resume game.\n8 to change game volume");
        gui.getInputtedUser().addActionListener(e -> {
            System.out.println(e.getSource());
            String optionInput = gui.getInputtedUser().getText();
            gui.getInputtedUser().setText("");
            gui.getInputtedUser().removeActionListener(gui.getInputtedUser().getActionListeners()[0]);

            switch (optionInput) {
                case "1":
                    RoomFactory.clearRoomMap();
                    Json.items.clear();
                    Json.items2.clear();
                    try {
                        initiateGame();
                    } catch (IOException | org.json.simple.parser.ParseException | ParseException | UnsupportedAudioFileException | LineUnavailableException ex) {
                        ex.printStackTrace();
                    }
                    break;
                case "2":
                    Printer.print("Exiting game. Thank you for playing.");
                    System.exit(0);
                    break;
                case "3":
                    Printer.print("Maledictus is a console text-adventure game. You are a treasure hunter in seek of riches.  Your goal is to traverse the map, discover what lies within, and make it out alive!\nGame created by team Lefties: Ryan Mosser, Michael Herman, and Nikko Colby\n which was then taken into further production by Marcos Cardoso, Jose Mondragon and Samekh Resh");
                    try {
                        start();
                    } catch (IOException | org.json.simple.parser.ParseException | ParseException | UnsupportedAudioFileException | LineUnavailableException | InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    break;
                case "4":
                    GameMusic.stopMusic();
                    try {
                        start();
                    } catch (IOException | org.json.simple.parser.ParseException | ParseException | UnsupportedAudioFileException | LineUnavailableException | InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    break;
                case "5":
                    GameMusic.playMusic();
                    try {
                        start();
                    } catch (IOException | org.json.simple.parser.ParseException | ParseException | UnsupportedAudioFileException | LineUnavailableException | InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    break;
                case "6":
                    displayGameMap();
                    try {
                        start();
                    } catch (IOException | org.json.simple.parser.ParseException | ParseException | UnsupportedAudioFileException | LineUnavailableException | InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    break;
                case "7":
                    try {
                        start();
                    } catch (IOException | org.json.simple.parser.ParseException | ParseException | UnsupportedAudioFileException | LineUnavailableException | InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    break;
                case "8":
                    changeVolume();
                default:
                    errorMsg = ANSI_RED + "Invalid Selection. Please try again." + ANSI_RESET;
                    try {
                        start();
                    } catch (IOException | org.json.simple.parser.ParseException | ParseException | UnsupportedAudioFileException | LineUnavailableException | InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    break;
            }
        });
    }

    private void changeVolume() {
        Printer.print("1 for low\n2 for med low\n3 for medium\n4 for med-high\n5 for high");
        String choice = scannerUserInput();
        switch (choice) {
            case "1":
                gameMusic.setMusicLow();
                break;
            case "2":
                gameMusic.setMusicMidLow();
                break;
            case "3":
                gameMusic.setMusicMidRange();
                break;
            case "4":
                gameMusic.setMusicMidHigh();
                break;
            case "5":
                gameMusic.setMusicHigh();
                break;
            default:
                Printer.print(ANSI_RED + "INVALID ENTRY: must use the letters 1 through 5 for volume manipulation");
                changeVolume();
                break;
        }
    }

    private void displayCurrentRoomActions() {
        if(currentRoom != null) {
            displayRoomDirections();
            displayRoomItems();
            displayInventoryActions();
            displayAllRoomNpc();
        }
    }

    private void displayRoomItems() {
        List<String> displayList = new ArrayList<>();
        if (currentRoom.getItems() != null) {
            roomItems = currentRoom.getItems();
            for (Item item : roomItems) {
                displayList.add("take/inspect " + item.getName());
            }
        }
        Printer.print("Room Items: " + displayList);
    }

    private void displayInventoryActions() {
        List<String> displayList = new ArrayList<>();
        if (playerOne.getInventory() != null) {
            for (Map.Entry<String, Item> item  : playerOne.getInventory().entrySet()) {
                displayList.add("use/drop/inspect " + item.getValue().getName());
            }
        }
        Printer.print("Inventory Items: " + displayList);
    }

    private void displayRoomDirections() {
        List<String> displayList = new ArrayList<>();
        if (currentRoom.getDirections() != null) {
            roomDirections = currentRoom.getDirections();
            for (Map.Entry<String, String> direction : roomDirections.entrySet()) {
                displayList.add("go " + direction.getKey());
            }
        }
        Printer.print("Directions: " + displayList);
    }

    private void displayAllRoomNpc() {
        List<String> displayList = new ArrayList<>();
        if (currentRoom.getNpcMap() != null) {
            npcMap = currentRoom.getNpcMap();
            for (Map.Entry<Integer, NPC> npc : npcMap.entrySet()) {
                if (!npc.getValue().getIsHostile()) {
                    displayList.add("talk " + npc.getValue().getName());
                } else {
                    displayList.add("battle " + npc.getValue().getName());
                }
            }
        }
        Printer.print("NPCs: " +displayList);
    }

    private void printErrorMsg() {
        if (errorMsg != null) {
            Printer.print("\n" + errorMsg);
            errorMsg = null;
        }
    }

    private void printSuccessMsg() {
        if (successMsg != null) {
            Printer.print("\n" + successMsg);
            successMsg = null;
        }
    }

    private void displayInventory() {
        Printer.print(playerOne.getInventory().keySet());
    }

    private void displayConsoleCommands() {
        Printer.print("-------------");
        Printer.print(ANSI_GREEN + "CURRENT ROOM:" + ANSI_RESET);
        Printer.print("-------------");
        Printer.print(currentRoom.getName());
        Printer.print(currentRoom.getDescription());

        Printer.print("-------------");
        Printer.print(ANSI_BLUE+ "INVENTORY:" + ANSI_RESET);
        Printer.print("-------------");
        displayInventory();

        Printer.print("-------------");
        Printer.print(ANSI_YELLOW+ "COMMANDS:" + ANSI_RESET);
        Printer.print("-------------");
        displayCurrentRoomActions();
        Printer.print("-------------");
    }

    private void displayBattleActions() {
        Printer.print("[attack], [run] or [equip]");
    }

    private void displayBattleCommands() {
        Printer.print("----------");
        Printer.print("IN BATTLE");
        Printer.print("----------");
        Printer.print("INVENTORY:");
        Printer.print("----------");
        displayInventory();

        Printer.print("-------------");
        Printer.print("COMMANDS:");
        Printer.print("-------------");
        displayBattleActions();
        Printer.print("-------------");
    }
}
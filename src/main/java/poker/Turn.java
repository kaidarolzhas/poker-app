package poker;


import static poker.Turn.PlayerAction.*;


public class Turn {
    public enum PlayerAction {FOLD, CHECK, CALL, RAISE, BET, ALLIN}

    private final int betAmount;
    private final PlayerAction action;
    private final Player player;

    public Turn(Player player, PlayerAction action, int betAmount) {
        this.betAmount = betAmount;
        this.action = action;
        this.player = player;
    }

    public int getBetAmount() {
        return betAmount;
    }

    public PlayerAction getAction() {
        return action;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public String toString() {
        String actionString = player.getName() + " (" + player.isAllIn() + ") " + action.toString().toLowerCase();
        if (action == PlayerAction.RAISE) {
            actionString += "d";
        } else {
            actionString += "ed";
        }
        if (action == PlayerAction.RAISE || action == PlayerAction.CALL || action == PlayerAction.BET)
            actionString = actionString.concat(" " + getBetAmount());
        actionString += ".";
        return actionString;
    }

    public boolean isFold() {
        return action == FOLD;
    }
    public boolean isAllIn() {
        return action == ALLIN;
    }
    public boolean isCheck() {
        return action == CHECK;
    }
}

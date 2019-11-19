package NonBlocking_Hangman_Client.view;


public class View {
    public View(){
        System.out.println("Welcome to HangMan!\nGuess a letter or the full word. Type 'quit' to close the game.\n");
    }

    private void printYouWin(GameStateDTO gameState) {
        String view = createLetterGuessView(gameState);
        System.out.println("\nYOU WON! The word was: '" + gameState.previousWord + "'");
        System.out.println("\n======= NEW ROUND ======\n");
        System.out.println(view);
    }

    private void printYouLoose(GameStateDTO gameState) {
        String view = createLetterGuessView(gameState);
        System.out.println("\nYOU LOST... The word was: '" + gameState.previousWord + "'");
        System.out.println("\n======= NEW ROUND ======\n");
        System.out.println(view);
    }

    private String createLetterGuessView(GameStateDTO state) {
        StringBuilder sb = new StringBuilder();
        sb.append("Score: " + state.gameScore + "  Lives: " + state.livesLeft + "/" + state.totalLives +
                "   Correct: "+state.numCorrectGuesses + "  Hidden:"+ state.theHiddenWord + "  Guessed: " + state.guessedLetters);
        return sb.toString();
    }

    public void UpdateGameView(GameStateDTO game) {
        // CHOOSE TEMPLATES depending on GAME STATE
        if (game.state.equals("LETTER_GUESS")){
            String view = createLetterGuessView(game);
            System.out.println(view);
        }
        else if (game.state.equals("LOST"))
            printYouLoose(game);
        else if (game.state.equals("WIN"))
            printYouWin(game);
        else
            System.out.println("Nothing got caught in UpdateGameView");
    }
}

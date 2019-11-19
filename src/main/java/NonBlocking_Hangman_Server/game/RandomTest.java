package NonBlocking_Hangman_Server.game;

public class RandomTest {
    public static void main(String [] args){
        RandomWordGenerator random = new RandomWordGenerator();
        System.out.println(random.pickRandomWord());
    }
}

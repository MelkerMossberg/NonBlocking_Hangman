package NonBlocking_Hangman_Server.game;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Future;

public class RandomWordGenerator {

    Random random;
    BufferedReader br;
    ArrayList<String> list;

    /**
     * Read every word in the text-file and store in an ArrayList when created.
     */
    public RandomWordGenerator(){
        random = new Random();
        list = new ArrayList<>();

        String filename = "words.txt";
        Path path = Paths.get(filename);
        try {
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);

            ByteBuffer buffer = ByteBuffer.allocate(1024*500);
            long position = 0;

            Future<Integer> operation = fileChannel.read(buffer, position);

            while (!operation.isDone());

            buffer.flip();
            byte[] data = new byte[buffer.limit()];
            buffer.get(data);
            System.out.println(new String(data));
            buffer.clear();

            InputStream is = null;
            BufferedReader bfReader = null;
            try {
                is = new ByteArrayInputStream(data);
                bfReader = new BufferedReader(new InputStreamReader(is));
                String temp;
                while((temp = bfReader.readLine()) != null){
                    list.add(temp);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try{
                    if(is != null) is.close();
                } catch (Exception ex){

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
        File file = new File("words.txt");
        FileReader fr = null;
        try {
            fr = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        br = new BufferedReader(fr);
        try {
            String s;
            while ((s = br.readLine()) != null) {
                list.add(s);
            }
            br.close();
            fr.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        */
    }

    public String pickRandomWord(){
        int randomChoice = random.nextInt(list.size());
        String word = list.get(randomChoice).toLowerCase();;
        System.out.println("Random word: " + word);
        return word;

    }
}

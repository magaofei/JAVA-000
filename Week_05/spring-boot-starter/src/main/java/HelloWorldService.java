/**
 * @author mark
 * @date 2020/11/22
 */
public class HelloWorldService {
    private String words;

    public String getWords() {
        return words;
    }

    public void setWords(String words) {
        this.words = words;
    }

    public String sayHello() {
        return "hello, " + words;
    }
}

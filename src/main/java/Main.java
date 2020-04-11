import backend.BackEnd;
import frontend.FrontEnd;
import java.io.IOException;

public final class Main {
  private Main() {}

  /**
   * Main method to run the example.
   *
   * @param args It is not required.
   * @throws IOException Something might go wrong.
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    final BackEnd backEnd = new BackEnd();
    final FrontEnd frontEnd = new FrontEnd();

    Thread.sleep(10000, 0);
    // Gracefully close the servers
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  backEnd.close();
                  frontEnd.close();
                }));
  }
}

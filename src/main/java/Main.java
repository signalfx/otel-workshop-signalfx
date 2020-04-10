import backend.BackEnd;
import frontend.FrontEnd;
import java.io.IOException;

public final class Main {
  /**
   * Main method to run the example.
   *
   * @param args It is not required.
   * @throws IOException Something might go wrong.
   */
  public static void main(String[] args) throws IOException {
    final BackEnd backEnd = new BackEnd();
    final FrontEnd frontEnd = new FrontEnd();
    // Gracefully close the server
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      backEnd.close();
      frontEnd.close();
    }));
  }

  private Main() {}
}

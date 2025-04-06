package cli;

import VoiceChat.VoiceChatClient;
import VoiceChat.VoiceChatServer;
import cli.pages.WelcomePage;

public class MainCLI {
    public static void main(String[] args) {
        // Initialize and display welcome page


        // Launch the client in a separate JVM instance


        WelcomePage welcomePage = new WelcomePage();

        welcomePage.display();


    }
}
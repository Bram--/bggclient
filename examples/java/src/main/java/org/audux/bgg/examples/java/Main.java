package org.audux.bgg.examples.java;

import org.audux.bgg.BggClient;
import org.audux.bgg.BggClient.Severity;
import org.audux.bgg.common.Domains;
import org.audux.bgg.common.Inclusion;
import org.audux.bgg.response.Response;
import org.audux.bgg.response.User;

import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String[] args) throws Exception {
        // Sets the logger level for the client - default WARN.
        BggClient.setLoggerSeverity(Severity.Verbose);

        // Greet & get username.
        System.out.println("Welcome, what is your username?");
        var scanner = new Scanner(System.in);
        var username = scanner.nextLine();

        // Call BggClient#user and hang on to the future.
        // BggClient.user(username).callAsync(collectionResponse -> {}) may also be used.
        CompletableFuture<Response<User>> responseFuture = BggClient.user(
                        /* username= */ username,
                        /* buddies= */ Inclusion.INCLUDE,
                        /* guilds= */ Inclusion.INCLUDE,
                        /* top= */ Inclusion.INCLUDE,
                        /* hot= */ Inclusion.INCLUDE,
                        /* domain= */ Domains.BOARD_GAME_GEEK,
                        /* page= */ 1)
                .paginate(1)
                .callAsync();

        // Wait for the user call to finish
        var collectionResponse = responseFuture.get();

        // Get the data in response (If any).
        var response = collectionResponse.getData();

        // If the user exists and the call was successful display a summary.
        if (collectionResponse.isSuccess() && response != null) {
            System.out.println("""
                    %s profile summary
                    Location: %s / %s
                    No. Buddies: %s
                    No. Guilds: %s
                    No. Top items: %s
                    No. Hot items: %s
                    """
                    .formatted(
                            response.getName(),
                            response.getStateOrProvince() == null
                                    ? ""
                                    : response.getStateOrProvince(),
                            response.getCountry() == null ? "" : response.getCountry(),
                            response.getBuddies() == null ? "0" : response.getBuddies(),
                            response.getGuilds() == null ? "0" : response.getGuilds(),
                            response.getTop() == null ? "0" : response.getTop().getItems().size(),
                            response.getHot() == null ? "0" : response.getHot().getItems().size())
                    .trim());
        } else {
            // Show the erroneous response - raw xml response.
            System.out.println("Whoops something went wrong:\n" + collectionResponse.getError());
        }
    }
}

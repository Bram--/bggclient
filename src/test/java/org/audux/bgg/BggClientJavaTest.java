package org.audux.bgg;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.audux.bgg.common.Inclusion.EXCLUDE;
import static org.audux.bgg.common.Inclusion.INCLUDE;
import static org.audux.bgg.util.TestUtils.setupMockEngine;

import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.audux.bgg.common.Domain;
import org.audux.bgg.common.FamilyType;
import org.audux.bgg.common.ForumListType;
import org.audux.bgg.common.HotListType;
import org.audux.bgg.common.Link;
import org.audux.bgg.common.Name;
import org.audux.bgg.common.PlayThingType;
import org.audux.bgg.common.Rank;
import org.audux.bgg.common.Ratings;
import org.audux.bgg.common.SitemapLocationType;
import org.audux.bgg.common.SubType;
import org.audux.bgg.common.ThingType;
import org.audux.bgg.response.CollectionItem;
import org.audux.bgg.response.CollectionStatistics;
import org.audux.bgg.response.FamilyItem;
import org.audux.bgg.response.ForumSummary;
import org.audux.bgg.response.GeekListComment;
import org.audux.bgg.response.GeekListItem;
import org.audux.bgg.response.GuildMember;
import org.audux.bgg.response.HotListItem;
import org.audux.bgg.response.Location;
import org.audux.bgg.response.PlayItem;
import org.audux.bgg.response.Player;
import org.audux.bgg.response.SearchResult;
import org.audux.bgg.response.SitemapUrl;
import org.audux.bgg.response.Status;
import org.audux.bgg.response.ThreadSummary;
import org.junit.jupiter.api.Test;

public class BggClientJavaTest {

  @Test
  public void collectionRequest()
      throws ExecutionException, InterruptedException, TimeoutException {
    BggClient.setEngine(
        () -> setupMockEngine("collection?username=novaeux&stats=1&subtype=rpgitem"));

    var future = BggClient.collection("Novaeux", ThingType.BOARD_GAME).callAsync();
    var response = future.get(5_000, MILLISECONDS);

    assertThat(response.getError()).isNull();
    assertThat(response.getData()).isNotNull();
    assertThat(response.getData().getTotalItems()).isEqualTo(1);
    var item = response.getData().getItems().get(0);
    assertThat(item)
        .isEqualTo(
            new CollectionItem(
                /* collectionId= */ 113239027,
                /* objectId= */ 311654,
                /* type= */ ThingType.RPG_ITEM,
                /* name= */ "Alice is Missing",
                /* originalName= */ null,
                /* yearPublished= */ 2020,
                /* thumbnail= */ "https://cf.geekdo-images.com/211476emISgQLsa2h3BAYw__thumb/img/g9QMstNAY2uOVSR1rH5Hx7Gxquk=/fit-in/200x150/filters:strip_icc()/pic5625807.png",
                /* image= */ "https://cf.geekdo-images.com/211476emISgQLsa2h3BAYw__original/img/nGOu9LrFF5udRimbJrei6HExde8=/0x0/filters:format(png)/pic5625807.png",
                new Status(
                    /* own= */ false,
                    /* previouslyOwned= */ false,
                    /* forTrade= */ false,
                    /* want= */ false,
                    /* wantToPlay= */ true,
                    /* wantToBuy= */ false,
                    /* wishlist= */ false,
                    /* wishlistPriority= */ null,
                    /* preOrdered= */ false,
                    /* lastModified= */ LocalDateTime.of(2023, 12, 6, 4, 18, 11)),
                /* numPlays= */ 0,
                /* comment= */ null,
                /* conditionText= */ null,
                new CollectionStatistics(
                    /* minimumPlayer= */ null,
                    /* maximumPlayers= */ null,
                    /* minimumPlayTime= */ null,
                    /* maximumPlayTime= */ null,
                    /* playingTime= */ null,
                    /* numOwned= */ 466,
                    new Ratings(
                        /* value= */ "N/A",
                        /* usersRated= */ 59,
                        /* average= */ 8.48729,
                        /* stdDev= */ 1.2776,
                        /* bayesAverage= */ 6.9585,
                        /* median= */ 0.0,
                        /* owned= */ null,
                        /* trading= */ null,
                        /* wanting= */ null,
                        /* wishing= */ null,
                        /* numComments= */ null,
                        /* numWeights= */ null,
                        /* averageWeight= */ null,
                        Lists.newArrayList(
                            new Rank(
                                /* id= */ 16,
                                /* type= */ "subtype",
                                /* name= */ "rpgitem",
                                /* friendlyName= */ "RPG Item Rank",
                                /* value= */ "317",
                                /* bayesAverage= */ "6.9585"))))));
  }

  @Test
  public void familyItemsRequest()
      throws ExecutionException, InterruptedException, TimeoutException {
    BggClient.setEngine(() -> setupMockEngine("family"));

    var future =
        BggClient.familyItems(
                new Integer[] {50152, 50153},
                new FamilyType[] {FamilyType.BOARD_GAME_FAMILY, FamilyType.RPG})
            .callAsync();
    var response = future.get(2_000, MILLISECONDS);

    assertThat(response.getError()).isNull();
    assertThat(response.getData()).isNotNull();
    var items = response.getData().getItems();
    assertThat(items).hasSize(1);
    assertThat(items.get(0))
        .isEqualTo(
            new FamilyItem(
                /* id= */ 50152,
                /* type= */ FamilyType.BOARD_GAME_FAMILY,
                new Name(
                    /* value= */ "History: Industrial Revolution",
                    /* type= */ "primary",
                    /* sortIndex= */ 1),
                /* description= */ "Games (expansions, promos, etc.) featuring the Industrial Revolution in theme or gameplay.&#10;&#10;&#10;The Industrial Revolution period (end of 18th and beginning of 19th centuries) and the development of the industries.&#10;&#10;",
                Lists.newArrayList(
                    new Link(
                        /* id= */ 65901,
                        /* value= */ "Age of Industry",
                        /* type= */ "boardgamefamily",
                        /* inbound= */ true),
                    new Link(
                        /* id= */ 99424,
                        /* value= */ "Age of Industry Expansion #1: Japan and Minnesota",
                        /* type= */ "boardgamefamily",
                        /* inbound= */ true),
                    new Link(
                        /* id= */ 136217,
                        /* value= */ "Age of Industry Expansion: Belgium & USSR",
                        /* type= */ "boardgamefamily",
                        /* inbound= */ true))));
  }

  @Test
  public void forumListRequest() throws ExecutionException, InterruptedException, TimeoutException {
    BggClient.setEngine(() -> setupMockEngine("forumlist"));

    var future = BggClient.forumList(/* id= */ 369671, ForumListType.THING).callAsync();
    var response = future.get(2_000, MILLISECONDS);

    assertThat(response.getError()).isNull();
    assertThat(response.getData()).isNotNull();
    var forums = response.getData().getForums();
    var formatter = DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss Z");
    assertThat(forums).hasSize(10);
    assertThat(forums)
        .containsExactly(
            new ForumSummary(
                /* id= */ 3696791,
                /* groupId= */ 0,
                /* noPosting= */ false,
                /* title= */ "Reviews",
                /* description= */ "Post your game reviews in this forum.  <A href=\"/thread/59278\">Click here for help on writing game reviews.</A>",
                /* numThreads= */ 65,
                /* numPosts= */ 1603,
                /* lastPostDate= */ LocalDateTime.parse(
                    "Tue, 23 Jan 2024 09:13:43 +0000", formatter)),
            new ForumSummary(
                /* id= */ 3696792,
                /* groupId= */ 0,
                /* noPosting= */ false,
                /* title= */ "Sessions",
                /* description= */ "Post your session reports here.",
                /* numThreads= */ 12,
                /* numPosts= */ 99,
                /* lastPostDate= */ LocalDateTime.parse(
                    "Sun, 14 Jan 2024 22:56:08 +0000", formatter)),
            new ForumSummary(
                /* id= */ 3696793,
                /* groupId= */ 0,
                /* noPosting= */ false,
                /* title= */ "General",
                /* description= */ "Post any related article to this game here.",
                /* numThreads= */ 633,
                /* numPosts= */ 9271,
                /* lastPostDate= */ LocalDateTime.parse(
                    "Wed, 24 Jan 2024 17:29:58 +0000", formatter)),
            new ForumSummary(
                /* id= */ 3696794,
                /* groupId= */ 0,
                /* noPosting= */ false,
                /* title= */ "Rules",
                /* description= */ "Post any rules questions you have here.",
                /* numThreads= */ 1096,
                /* numPosts= */ 8191,
                /* lastPostDate= */ LocalDateTime.parse(
                    "Tue, 23 Jan 2024 20:53:33 +0000", formatter)),
            new ForumSummary(
                /* id= */ 3696795,
                /* groupId= */ 0,
                /* noPosting= */ false,
                /* title= */ "Strategy",
                /* description= */ "Post strategy and tactics articles here.",
                /* numThreads= */ 119,
                /* numPosts= */ 1853,
                /* lastPostDate= */ LocalDateTime.parse(
                    "Tue, 23 Jan 2024 18:50:11 +0000", formatter)),
            new ForumSummary(
                /* id= */ 3696796,
                /* groupId= */ 0,
                /* noPosting= */ false,
                /* title= */ "Variants",
                /* description= */ "Post variants to the game rules here.",
                /* numThreads= */ 146,
                /* numPosts= */ 2335,
                /* lastPostDate= */ LocalDateTime.parse(
                    "Tue, 23 Jan 2024 12:33:44 +0000", formatter)),
            new ForumSummary(
                /* id= */ 3696797,
                /* groupId= */ 0,
                /* noPosting= */ false,
                /* title= */ "News",
                /* description= */ "Post time sensitive announcements here.",
                /* numThreads= */ 19,
                /* numPosts= */ 726,
                /* lastPostDate= */ LocalDateTime.parse(
                    "Fri, 19 Jan 2024 15:29:10 +0000", formatter)),
            new ForumSummary(
                /* id= */ 3696798,
                /* groupId= */ 0,
                /* noPosting= */ false,
                /* title= */ "Crowdfunding",
                /* description= */ "Post crowdfunding / preorder content here.",
                /* numThreads= */ 0,
                /* numPosts= */ 0,
                /* lastPostDate= */ null),
            new ForumSummary(
                /* id= */ 3696799,
                /* groupId= */ 0,
                /* noPosting= */ false,
                /* title= */ "Play By Forum",
                /* description= */ "Run Play By Forum (PBF) games here.",
                /* numThreads= */ 0,
                /* numPosts= */ 0,
                /* lastPostDate= */ null),
            new ForumSummary(
                /* id= */ 3696800,
                /* groupId= */ 0,
                /* noPosting= */ false,
                /* title= */ "Find Players",
                /* description= */ "Post here to find local gamers and to promote local events.",
                /* numThreads= */ 12,
                /* numPosts= */ 228,
                /* lastPostDate= */ LocalDateTime.parse(
                    "Mon, 22 Jan 2024 13:22:41 +0000", formatter)));
  }

  @Test
  public void forumRequest()
      throws ExecutionException, InterruptedException, TimeoutException, BggRequestException {
    var engine =
        setupMockEngine("forum?id=3696796", "forum?id=3696796&page=2", "forum?id=3696796&page=3");
    BggClient.setEngine(() -> engine);

    var future = BggClient.forum(/* id= */ 3696796).paginate().callAsync();
    var response = future.get(2_000, MILLISECONDS);

    assertThat(response.getError()).isNull();
    assertThat(response.getData()).isNotNull();
    var forum = response.getData();
    var formatter = DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss Z");
    assertThat(forum.getId()).isEqualTo(3696796);
    assertThat(forum.getNoPosting()).isFalse();
    assertThat(forum.getTitle()).isEqualTo("Variants");
    assertThat(forum.getNumThreads()).isEqualTo(148);
    assertThat(forum.getNumPosts()).isEqualTo(2354);
    assertThat(forum.getLastPostDate())
        .isEqualTo(LocalDateTime.parse("Thu, 01 Jan 1970 00:00:00 +0000", formatter));
    assertThat(forum.getThreads()).hasSize(148);
    assertThat(forum.getThreads().get(0))
        .isEqualTo(
            new ThreadSummary(
                /* id= */ 3244901,
                /* subject= */ "Limited aquarium",
                /* author= */ "Farouke",
                /* numArticles= */ 11,
                /* postDate= */ LocalDateTime.parse("Sun, 11 Feb 2024 13:15:58 +0000", formatter),
                /* lastPostDate= */ LocalDateTime.parse(
                    "Wed, 14 Feb 2024 22:44:14 +0000", formatter)));
  }

  @Test
  public void geekListRequest() throws ExecutionException, InterruptedException, TimeoutException {
    BggClient.setEngine(() -> setupMockEngine("geeklist?id=331520&comments=1"));

    var future = BggClient.geekList(/* id= */ 331520, /* comments */ INCLUDE).callAsync();
    var response = future.get(2_000, MILLISECONDS);

    assertThat(response.getError()).isNull();
    assertThat(response.getData()).isNotNull();
    var geekList = response.getData();
    assertThat(geekList.getId()).isEqualTo(331520);
    assertThat(geekList.getPostDate()).isEqualTo(LocalDateTime.of(2024, 2, 6, 16, 45, 12));
    assertThat(geekList.getEditDate()).isEqualTo(LocalDateTime.of(2024, 2, 6, 17, 5, 7));
    assertThat(geekList.getThumbs()).isEqualTo(63);
    assertThat(geekList.getUsername()).isEqualTo("Rawes6");
    assertThat(geekList.getTitle()).isEqualTo("10 Tips for Gaming with a Brain Injury");
    assertThat(geekList.getDescription()).hasLength(803);
    assertThat(geekList.getNumItems()).isEqualTo(10);
    assertThat(geekList.getItems()).hasSize(10);
    assertThat(geekList.getItems().get(0))
        .isEqualTo(
            new GeekListItem(
                /* id= */ 10493517,
                /* objectType= */ "thing",
                /* subType= */ SubType.BOARD_GAME,
                /* objectName= */ "Memory Madness",
                /* objectId= */ 10119,
                /* username= */ "Rawes6",
                /* postDate= */ LocalDateTime.of(2024, 2, 6, 15, 20, 58),
                /* editDate= */ LocalDateTime.of(2024, 2, 6, 15, 20, 58),
                /* thumbs= */ 6,
                /* imageId= */ 0,
                /* body= */ "Listen, you have got to accept it, your noggin don't work the same as it"
                    + " used to. Your gonna forget rules, what games you've played, what"
                    + " you did on previous turns; hell, your gonna forget if you even"
                    + " liked the games sometimes. Use BGG to help supplement your memory."
                    + " Rank games right after playing, add notes to remember specific"
                    + " things and thoughts, and keep your collection up to date! It"
                    + " only takes a couple of games not added to easily forget you even"
                    + " have a game."));
    var firstComment =
        new GeekListComment(
            /* username= */ "TomandJonna",
            /* date= */ LocalDateTime.of(2024, 2, 7, 14, 47, 55),
            /* postDate= */ LocalDateTime.of(2024, 2, 7, 14, 47, 55),
            /* editDate= */ LocalDateTime.of(2024, 2, 7, 14, 47, 55),
            /* thumbs= */ 2);
    firstComment.setValue("Thank you");
    assertThat(geekList.getComments().get(0)).isEqualTo(firstComment);
  }

  @Test
  public void guildsRequest()
      throws ExecutionException, InterruptedException, TimeoutException, BggRequestException {
    var engine =
        setupMockEngine(
            "guilds?id=2310&members=1&page=1",
            "guilds?id=2310&members=1&page=2",
            "guilds?id=2310&members=1&page=3");
    BggClient.setEngine(() -> engine);

    var future = BggClient.guild(/* id= */ 2310, /* members= */ INCLUDE).paginate().callAsync();
    var response = future.get(2_000, MILLISECONDS);

    assertThat(response.getError()).isNull();
    assertThat(response.getData()).isNotNull();
    var guild = response.getData();
    assertThat(guild.getId()).isEqualTo(2310);
    assertThat(guild.getName()).isEqualTo("St Albans Board Games Club");
    assertThat(guild.getCreatedAt()).isEqualTo(LocalDateTime.of(2015, 7, 27, 13, 45, 48));
    assertThat(guild.getManager()).isEqualTo("montoc1701");
    assertThat(guild.getWebsite()).isEqualTo("https://www.facebook.com/groups/StABoardgamesclub/");
    assertThat(guild.getCategory()).isEqualTo("group");
    assertThat(guild.getDescription()).hasLength(2_149);
    assertThat(guild.getLocation())
        .isEqualTo(
            new Location(
                /* addressLine1= */ "",
                /* addressLine2= */ "",
                /* city= */ "St. Albans",
                /* stateOrProvince= */ "Hertfordshire",
                /* postalCode= */ "AL3",
                /* country= */ "United Kingdom"));

    var members = guild.getMembers();
    assertThat(requireNonNull(members).getCount()).isEqualTo(62);
    assertThat(members.getMembers()).hasSize(62);
    assertThat(members.getMembers().get(0))
        .isEqualTo(
            new GuildMember(
                /* name= */ "Novaeux", /* date= */ LocalDateTime.of(2024, 2, 1, 15, 2, 36)));
  }

  @Test
  public void hotRequest() throws ExecutionException, InterruptedException, TimeoutException {
    BggClient.setEngine(() -> setupMockEngine("hot"));

    var future = BggClient.hotList(HotListType.BOARD_GAME).callAsync();
    var response = future.get(2_000, MILLISECONDS);

    assertThat(response.getError()).isNull();
    assertThat(response.getData()).isNotNull();
    var hotItems = response.getData().getResults();
    assertThat(hotItems).hasSize(50);
    assertThat(hotItems.get(0))
        .isEqualTo(
            new HotListItem(
                /* id= */ 332686,
                /* rank= */ 1,
                /* name= */ "John Company: Second Edition",
                /* thumbnail= */ "https://cf.geekdo-images.com/TAdE4z_bwAAjJlmPrkmKhA__thumb/img/pwgtQn8ArKjwBxk3bnDuIVAPWgU=/fit-in/200x150/filters:strip_icc()/pic6601629.jpg",
                /* yearPublished= */ 2022));
  }

  @Test
  public void playsRequest()
      throws ExecutionException, InterruptedException, TimeoutException, BggRequestException {
    var engine =
        setupMockEngine(
            "plays?username=auser&page=1",
            "plays?username=auser&page=2",
            "plays?username=auser&page=3");
    BggClient.setEngine(() -> engine);

    var future =
        BggClient.plays(
                /* name= */ "auser",
                /* id= */ null,
                /* type= */ PlayThingType.THING,
                /* minDate= */ null,
                /* maxDate= */ null,
                /* subType= */ SubType.BOARD_GAME)
            .paginate()
            .callAsync();
    var response = future.get(2_000, MILLISECONDS);

    assertThat(response.getError()).isNull();
    assertThat(response.getData()).isNotNull();
    var plays = response.getData();
    assertThat(plays.getUsername()).isEqualTo("auser");
    assertThat(plays.getUserid()).isEqualTo(2202051);
    assertThat(plays.getTotal()).isEqualTo(270);
    assertThat(plays.getPage()).isEqualTo(3);
    assertThat(plays.getPlays()).hasSize(270);
    var lastGame = plays.getPlays().get(0);
    assertThat(lastGame.getId()).isEqualTo(81446658);
    assertThat(lastGame.getDate()).isEqualTo(LocalDate.of(2024, 2, 14));
    assertThat(lastGame.getQuantity()).isEqualTo(1);
    assertThat(lastGame.getLengthInMinutes()).isEqualTo(203);
    assertThat(lastGame.getIncomplete()).isFalse();
    assertThat(lastGame.getNoWinStats()).isFalse();
    assertThat(lastGame.getLocation()).isEqualTo("");
    assertThat(lastGame.getComments()).isEqualTo(Lists.newArrayList("#bgstats"));
    assertThat(lastGame.getItem())
        .isEqualTo(
            new PlayItem(
                /* name= */ "Earth",
                /* objectType= */ PlayThingType.THING,
                /* objectId= */ 350184,
                /* subTypes= */ Lists.newArrayList(
                    new org.audux.bgg.response.SubType(SubType.BOARD_GAME))));
    assertThat(lastGame.getPlayers())
        .containsExactly(
            new Player(
                /* username= */ "",
                /* userid= */ 2202051,
                /* name= */ "",
                /* startPosition= */ "",
                /* color= */ "",
                /* new= */ true,
                /* rating= */ 0.0,
                /* win= */ false,
                /* score= */ 272.0),
            new Player(
                /* username= */ "",
                /* userid= */ 0,
                /* name= */ "",
                /* startPosition= */ "",
                /* color= */ "",
                /* new= */ true,
                /* rating= */ 0.0,
                /* win= */ true,
                /* score= */ 284.0),
            new Player(
                /* username= */ "",
                /* userid= */ 0,
                /* name= */ "",
                /* startPosition= */ "",
                /* color= */ "",
                /* new= */ true,
                /* rating= */ 0.0,
                /* win= */ false,
                /* score= */ 209.0),
            new Player(
                /* username= */ "",
                /* userid= */ 0,
                /* name= */ "",
                /* startPosition= */ "",
                /* color= */ "",
                /* new= */ true,
                /* rating= */ 0.0,
                /* win= */ false,
                /* score= */ 257.0));
  }

  @Test
  public void searchRequest() throws ExecutionException, InterruptedException, TimeoutException {
    BggClient.setEngine(() -> setupMockEngine("search?query=my+little"));

    var future =
        BggClient.search(
                /* query= */ "My little",
                new ThingType[] {ThingType.BOARD_GAME},
                /* exactMatch= */ false)
            .callAsync();
    var response = future.get(2_000, MILLISECONDS);

    assertThat(response.getError()).isNull();
    assertThat(response.getData()).isNotNull();
    var searchResults = response.getData();
    assertThat(searchResults.getTotal()).isEqualTo(144);
    assertThat(searchResults.getResults()).hasSize(144);
    assertThat(searchResults.getResults().get(0))
        .isEqualTo(
            new SearchResult(
                /* name= */ new Name(
                    /* value= */ "Connect 4: My Little Pony",
                    /* type= */ "primary",
                    /* sortIndex= */ null),
                /* id= */ 167159,
                /* type= */ ThingType.BOARD_GAME,
                /* yearPublished= */ 2014));
  }

  @Test
  public void thingsRequest()
      throws ExecutionException, InterruptedException, TimeoutException, BggRequestException {
    var engine =
        setupMockEngine(
            "thing?id=396790&comments=1&page=1",
            "thing?id=396790&comments=1&page=2",
            "thing?id=396790&comments=1&page=3");
    BggClient.setEngine(() -> engine);

    var future =
        BggClient.things(
                /* ids= */ new Integer[] { 396790 },
                /* types= */ new ThingType[] {},
                /* stats= */ false,
                /* versions= */ false,
                /* videos= */ false,
                /* marketplace= */ false,
                /* comments= */ true,
                /* ratingComments= */ false)
            .paginate()
            .callAsync();
    var response = future.get(2_000, MILLISECONDS);

    assertThat(response.getError()).isNull();
    assertThat(response.getData()).isNotNull();
    var things = response.getData();
    assertThat(requireNonNull(things).getThings()).hasSize(1);
    var thing = things.getThings().get(0);
    assertThat(thing.getName()).isEqualTo("Nucleum");
    assertThat(thing.getType()).isEqualTo(ThingType.BOARD_GAME);
    assertThat(thing.getNames()).hasSize(3);
    assertThat(thing.getThumbnail())
        .isEqualTo(
            "https://cf.geekdo-images.com/fIVUaMvbfy6GCOgfxt7xaw__thumb/img/jKsO4nKmtNjX5bfH7aCPeK7hsqU=/fit-in/200x150/filters:strip_icc()/pic7647168.jpg");
    assertThat(thing.getImage())
        .isEqualTo(
            "https://cf.geekdo-images.com/fIVUaMvbfy6GCOgfxt7xaw__original/img/dBMnuz3SrgxsDLHT6pwbQFPQBIw=/0x0/filters:format(jpeg)/pic7647168.jpg");
    assertThat(thing.getDescription()).hasLength(1824);
    assertThat(thing.getYearPublished()).isEqualTo(2023);
    assertThat(thing.getMinPlayers()).isEqualTo(1);
    assertThat(thing.getMaxPlayers()).isEqualTo(4.0);
    assertThat(thing.getPlayingTimeInMinutes()).isEqualTo(150);
    assertThat(thing.getMinPlayingTimeInMinutes()).isEqualTo(60);
    assertThat(thing.getMaxPlayingTimeInMinutes()).isEqualTo(150);
    assertThat(thing.getMinAge()).isEqualTo(14);
    assertThat(thing.getPolls()).hasSize(3);
    assertThat(requireNonNull(thing.getComments()).getPage()).isEqualTo(3);
    assertThat(thing.getComments().getTotalItems()).isEqualTo(213);
    assertThat(thing.getComments().getComments()).hasSize(213);
    assertThat(thing.getLinks()).hasSize(35);
  }

  @Test
  public void threadRequest() throws ExecutionException, InterruptedException, TimeoutException {
    BggClient.setEngine(() -> setupMockEngine("thread"));

    var future =
        BggClient.thread(
                /* id= */ 3208373,
                /* minArticleId= */ 0,
                /* minArticleDate= */ LocalDateTime.of(2010, 1, 1, 0, 0),
                /* count= */ 100)
            .callAsync();
    var response = future.get(2_000, MILLISECONDS);

    assertThat(response.getError()).isNull();
    assertThat(response.getData()).isNotNull();
    var thread = response.getData();
    assertThat(thread.getId()).isEqualTo(3208373);
    assertThat(thread.getNumArticles()).isEqualTo(13);
    assertThat(thread.getLink()).isEqualTo("https://boardgamegeek.com/thread/3208373");
    assertThat(thread.getSubject()).isEqualTo("New Maps for Ark Nova + Marine World");
    assertThat(thread.getArticles()).hasSize(13);
    var firstArticle = thread.getArticles().get(0);
    var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssz");
    assertThat(firstArticle.getId()).isEqualTo(43461362);
    assertThat(firstArticle.getUsername()).isEqualTo("darkuss");
    assertThat(firstArticle.getNumEdits()).isEqualTo(4);
    assertThat(firstArticle.getLink())
        .isEqualTo("https://boardgamegeek.com/thread/3208373/article/43461362#43461362");
    assertThat(firstArticle.getPostDate())
        .isEqualTo(LocalDateTime.parse("2023-12-15T13:07:50-06:00", formatter));
    assertThat(firstArticle.getEditDate())
        .isEqualTo(LocalDateTime.parse("2023-12-16T03:19:58-06:00", formatter));
    assertThat(firstArticle.getSubject()).isEqualTo("New Maps for Ark Nova + Marine World");
    assertThat(firstArticle.getBody()).hasLength(6133);
  }

  @Test
  public void userRequest()
      throws ExecutionException, InterruptedException, TimeoutException, BggRequestException {
    var engine =
        setupMockEngine(
            "user?name=Novaeux&buddies=1&guilds=1&page=1",
            "user?name=Novaeux&buddies=1&guilds=1&page=2",
            "user?name=Novaeux&buddies=1&guilds=1&page=3");
    BggClient.setEngine(() -> engine);

    var future =
        BggClient.user(
                /* name= */ "Novaeux",
                /* buddies= */ INCLUDE,
                /* guilds= */ INCLUDE,
                /* top= */ EXCLUDE,
                /* hot= */ EXCLUDE,
                /* domain= */ Domain.BOARD_GAME_GEEK)
            .paginate()
            .callAsync();
    var response = future.get(2_000, MILLISECONDS);

    assertThat(response.getError()).isNull();
    assertThat(response.getData()).isNotNull();
    var user = response.getData();
    assertThat(user.getName()).isEqualTo("Novaeux");
    assertThat(user.getId()).isEqualTo(2639010);
    assertThat(user.getFirstName()).isEqualTo("Bram");
    assertThat(user.getLastName()).isEqualTo("Wijnands");
    assertThat(user.getAvatarLink()).isEqualTo("N/A");
    assertThat(user.getYearRegistered()).isEqualTo(2020);
    assertThat(user.getLastLogin()).isEqualTo(LocalDate.of(2024, 2, 5));
    assertThat(user.getStateOrProvince()).isEqualTo("England");
    assertThat(user.getCountry()).isEqualTo("United Kingdom");
    assertThat(user.getWebAddress()).isEqualTo("");
    assertThat(user.getXBoxAccount()).isEqualTo("");
    assertThat(user.getWiiAccount()).isEqualTo("");
    assertThat(user.getPsnAccount()).isEqualTo("");
    assertThat(user.getBattleNetAccount()).isEqualTo("");
    assertThat(user.getSteamAccount()).isEqualTo("");
    assertThat(user.getTradeRating()).isEqualTo(0);
    assertThat(user.getTop()).isNull();
    assertThat(user.getHot()).isNull();
    assertThat(requireNonNull(user.getBuddies()).getTotal()).isEqualTo(2_200);
    assertThat(user.getBuddies().getBuddies()).hasSize(2_200);
    assertThat(requireNonNull(user.getGuilds()).getTotal()).isEqualTo(1_900);
    assertThat(user.getGuilds().getGuilds()).hasSize(1_900);
  }

  @Test
  public void sitemapIndexRequest()
      throws ExecutionException, InterruptedException, TimeoutException, BggRequestException {
    var engine =
        setupMockEngine(
            "sitemapindex.diffuse",
            "sitemap_boardgame_page1",
            "sitemap_boardgameversion_page1",
            "sitemap_files_page1");
    BggClient.setEngine(() -> engine);

    var future = BggClient.sitemapIndex(Domain.BOARD_GAME_GEEK).diffuse().callAsync();
    var response = future.get(2_000, MILLISECONDS);

    assertThat(response.getError()).isNull();
    assertThat(response.getData()).isNotNull();
    var sitemaps = response.getData();
    assertThat(sitemaps).hasSize(3);
    assertThat(sitemaps.get(SitemapLocationType.BOARD_GAMES)).hasSize(10);
    assertThat(sitemaps.get(SitemapLocationType.BOARD_GAMES).get(0))
        .isEqualTo(
            new SitemapUrl(
                /* location= */ "https://boardgamegeek.com/boardgame/1/die-macher",
                /* changeFrequency= */ "daily",
                /* priority= */ 1.0,
                /* lastModified= */ null));
    assertThat(sitemaps.get(SitemapLocationType.BOARD_GAME_VERSIONS)).hasSize(9);
    assertThat(sitemaps.get(SitemapLocationType.FILES)).hasSize(11);
  }
}

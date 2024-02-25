package org.audux.bgg.common

/**
 * Different types of Sitemap (locations) e.g. board game sitemaps, board game accessory sitemaps
 * etc.
 */
enum class SitemapLocationType(private val urlPart: String) {
    /** Used when none of the URL matches any known URL-part. */
    UNKNOWN(""),

    /**
     * Board game pages pertaining all board game information, IDs can be extracted for usage with
     * the [org.audux.bgg.BggClient.things] API.
     *
     * Domains: [Domain.BOARD_GAME_GEEK]
     */
    BOARD_GAMES("geekitems_boardgame_page"),

    /**
     * Board game accessory pages pertaining information about different board game accessories i.e.
     * A spring-themed board for Agricola. IDs can be extracted for usage with the
     * [org.audux.bgg.BggClient.things] API.
     *
     * Domains: [Domain.BOARD_GAME_GEEK]
     */
    BOARD_GAME_ACCESSORIES("boardgameaccessory_page"),

    /**
     * Board game accessory family page(s) grouping board game accessories together (a family). IDs
     * can be extracted for usage with the [org.audux.bgg.BggClient.familyItems] API.
     *
     * Domains: [Domain.BOARD_GAME_GEEK]
     */
    BOARD_GAME_ACCESSORY_FAMILIES("bgaccessoryfamily_page"),

    /**
     * Board game accessory version pages containing different versions of board game accessories.
     * No API exists to query these but the same information van be found in the
     * [org.audux.bgg.BggClient.things] api using `type=BOARD_GAME_ACCESSORY` and `version=true`.
     *
     * Domains: [Domain.BOARD_GAME_GEEK]
     */
    BOARD_GAME_ACCESSORY_VERSIONS("bgaccessoryversion_page"),

    /**
     * Board game artist pages pertaining information about all Board game artists. No API is
     * available to retrieve more information.
     *
     * Domains: [Domain.BOARD_GAME_GEEK]
     */
    BOARD_GAME_ARTISTS("boardgameartist_page"),

    /**
     * Board game author pages pertaining information about all Board game authors. No API is
     * available to retrieve more information.
     *
     * Domains: [Domain.BOARD_GAME_GEEK]
     */
    BOARD_GAME_AUTHORS("boardgameauthor_page"),

    /**
     * Board game compilation pages containing different compilations e.g. books about board games
     * or a single box containing several games etc. IDs can be extracted for usage with the
     * [org.audux.bgg.BggClient.things] API.
     *
     * Domains: [Domain.BOARD_GAME_GEEK]
     */
    BOARD_GAME_COMPILATIONS("boardgamecompilation_page"),

    /**
     * Board game designer pages pertaining information about all Board game designers. No API is
     * available to retrieve more information.
     *
     * Domains: [Domain.BOARD_GAME_GEEK]
     */
    BOARD_GAME_DESIGNERS("boardgamedesigner_page"),

    /**
     * Board game event pages. No API is available to retrieve more information.
     *
     * Domains: [Domain.BOARD_GAME_GEEK]
     */
    BOARD_GAME_EVENTS("boardgameevent_page"),

    /**
     * Board game expansion pages pertaining information about different board game expansions. IDs
     * can be extracted for usage with the [org.audux.bgg.BggClient.things] API.
     *
     * Domains: [Domain.BOARD_GAME_GEEK]
     */
    BOARD_GAME_EXPANSIONS("boardgameexpansion_page"),

    /**
     * Board game family pages containing different board game families. IDs can be extracted for
     * usage with the [org.audux.bgg.BggClient.familyItems] API.
     *
     * Domains: [Domain.BOARD_GAME_GEEK]
     */
    BOARD_GAME_FAMILIES("boardgamefamily_page"),

    /**
     * Board game implementation pages containing board games that implement other games e.g. `Maki
     * Master` which reimplements Wasabi. IDs can be extracted for usage with the
     * [org.audux.bgg.BggClient.things] API.
     *
     * Domains: [Domain.BOARD_GAME_GEEK]
     */
    BOARD_GAME_IMPLEMENTATIONS("boardgameimplementation_page"),

    /**
     * Board game issue pages containing book series related to/about board games. IDs can be
     * extracted for usage with the [org.audux.bgg.BggClient.things] API
     *
     * Domains: [Domain.BOARD_GAME_GEEK]
     */
    BOARD_GAME_ISSUES("boardgameissue_page"),

    /**
     * Board game issue article pages containing articles of book series related to/about board
     * games. No API exists to find further information.
     *
     * Domains: [Domain.BOARD_GAME_GEEK]
     */
    BOARD_GAME_ISSUE_ARTICLES("boardgameissuearticle_page"),

    /**
     * Board game issue article pages containing articles of magazine/book series related to/about
     * board games. No API exists to find further information.
     *
     * Domains: [Domain.BOARD_GAME_GEEK]
     */
    BOARD_GAME_ISSUE_VERSIONS("boardgameissueversion_page"),

    /**
     * Board game issue pages containing magazines related to/about board games. No API exists to
     * find further information.
     *
     * Domains: [Domain.BOARD_GAME_GEEK]
     */
    BOARD_GAME_PERIODICALS("boardgameperiodical_page"),

    /**
     * Board game publisher pages containing board games publishers (companies). No API exists to
     * find further information.
     *
     * Domains: [Domain.BOARD_GAME_GEEK]
     */
    BOARD_GAME_PUBLISHERS("boardgamepublisher_page"),

    /**
     * Board game sleeve pages containing information about board game sleeves. No API exists to
     * find further information.
     *
     * Domains: [Domain.BOARD_GAME_GEEK]
     */
    BOARD_GAME_SLEEVES("bgsleeve_page"),

    /**
     * Board game sleeve manufacturer pages containing information about board game sleeve
     * manufacturers. No API exists to find further information.
     *
     * Domains: [Domain.BOARD_GAME_GEEK]
     */
    BOARD_GAME_SLEEVE_MANUFACTURERS("bgsleevemfg_page"),

    /**
     * Board game sub-domain pages containing sub domains that group certain board games together
     * e.g. party-games. No API exists to find further information.
     *
     * Domains: [Domain.BOARD_GAME_GEEK]
     */
    BOARD_GAME_SUB_DOMAINS("boardgamesubdomain_page"),

    /**
     * Board game version pages containing different versions of board games. No API exists to find
     * further information, but these versions can be found in the [org.audux.bgg.BggClient.things]
     * API when using `versions=1`.
     *
     * Domains: [Domain.BOARD_GAME_GEEK]
     */
    BOARD_GAME_VERSIONS("boardgameversion_page_"),

    /**
     * Card type pages containing different cards used in different board games. No API exists to
     * find further information.
     *
     * Domains: [Domain.BOARD_GAME_GEEK]
     */
    CARD_TYPES("cardtype_page"),

    /**
     * Card set pages containing different sets/collections of card types. No API exists to find
     * further information.
     *
     * Domains: [Domain.BOARD_GAME_GEEK]
     */
    CARD_SETS("cardset_page"),

    /**
     * Files pages containing files uploaded by publishers/users i.e. rulebooks, player aids etc. No
     * API exists to find further information.
     *
     * Domains: [Domain.BOARD_GAME_GEEK], [Domain.RPG_GEEK], [Domain.VIDEO_GAME_GEEK]
     */
    FILES("files_page"),

    /**
     * Geek list pages containing all geek lists (lists of `things` made by users). IDs can be
     * extracted for usage with the [org.audux.bgg.BggClient.geekList] API
     *
     * Domains: [Domain.BOARD_GAME_GEEK], [Domain.RPG_GEEK], [Domain.VIDEO_GAME_GEEK]
     */
    GEEK_LISTS("geeklists_page"),

    /**
     * Image pages containing all images uploaded by users. No API exists to find further
     * information.
     *
     * Domains: [Domain.BOARD_GAME_GEEK], [Domain.RPG_GEEK], [Domain.VIDEO_GAME_GEEK]
     */
    IMAGES("images_page"),

    /**
     * RPG pages containing RPGs that can make up a collection of [RPG_ITEM] objects. No API exists
     * to find further information.
     *
     * Domains: [Domain.RPG_GEEK]
     */
    RPG("rpg_page"),

    /**
     * RPG artist pages containing artists/illustrators of RPGs. No API exists to find further
     * information.
     *
     * Domains: [Domain.RPG_GEEK]
     */
    RPG_ARTISTS("rpgartist_page"),

    /**
     * RPG category pages containing different categories of RPS i.e. solo adventure, campaign
     * setting etc.. No API exists to find further information.
     *
     * Domains: [Domain.RPG_GEEK]
     */
    RPG_CATEGORIES("rpgcategory_page"),

    /**
     * RPG designer pages containing designers of RPGs. No API exists to find further information.
     *
     * Domains: [Domain.RPG_GEEK]
     */
    RPG_DESIGNERS("rpgdesigner_page"),

    /**
     * RPG family pages containing RPG families e.g. Marvel, Call of Cthulhu. IDs can be extracted
     * for usage with the [org.audux.bgg.BggClient.familyItems] API.
     *
     * Domains: [Domain.RPG_GEEK]
     */
    RPG_FAMILIES("rpgfamily_page"),

    /**
     * RPG genre pages containing different genres of RPGS e.g. Steampunk, Horror etc. No API exists
     * to find further information.
     *
     * Domains: [Domain.RPG_GEEK]
     */
    RPG_GENRES("rpggenre_page"),

    /**
     * RPG issues pages containing RPG issues i.e. Books about RPGs or magazines. IDs can be
     * extracted for usage with the [org.audux.bgg.BggClient.things] API.
     *
     * Domains: [Domain.RPG_GEEK]
     */
    RPG_ISSUE("rpgissue_page"),

    /**
     * RPG issues article pages containing pages/articles that make up an RPG issue. No API exists
     * to find further information.
     *
     * Domains: [Domain.RPG_GEEK]
     */
    RPG_ISSUE_ARTICLE("rpgissuearticle_page"),

    /**
     * RPG issues version pages containing RPG issues versions i.e. German edition of D&D magazine
     * etc. No API exists to find further information.
     *
     * Domains: [Domain.RPG_GEEK]
     */
    RPG_ISSUE_VERSION("rpgissueversion_page"),

    /**
     * RPG item pages containing RPG items i.e. D&D, single scenarios or even single page RPGs. IDs
     * can be extracted for usage with the [org.audux.bgg.BggClient.things] API.
     *
     * Domains: [Domain.RPG_GEEK]
     */
    RPG_ITEM("rpgitem_page"),

    /**
     * RPG item version pages containing different versions of RPG items. No API exists to find
     * further information however these versions can also be found in the
     * [org.audux.bgg.BggClient.things] when using `version=true`.
     *
     * Domains: [Domain.RPG_GEEK]
     */
    RPG_ITEM_VERSION("rpgitemversion_page"),

    /**
     * RPG mechanic pages containing RPG mechanics i.e. dice primarly d20, GM less etc.. No API
     * exists to find further information.
     *
     * Domains: [Domain.RPG_GEEK]
     */
    RPG_MECHANIC("rpgmechanic_page"),

    /**
     * RPG periodical pages containing RPG perdiodicals/magazines. IDs can be extracted for usage
     * with the [org.audux.bgg.BggClient.familyItems] API
     *
     * Domains: [Domain.RPG_GEEK]
     */
    RPG_PERIODICAL("rpgperiodical_page"),

    /**
     * RPG producers pages containing producers of RPGs. No API exists to find further information.
     *
     * Domains: [Domain.RPG_GEEK]
     */
    RPG_PRODUCERS("rpgproducer_page"),

    /**
     * RPG publisher pages containing publishers/companies of RPGs. No API exists to find further
     * information.
     *
     * Domains: [Domain.RPG_GEEK]
     */
    RPG_PUBLISHER("rpgpublisher_page"),

    /**
     * RPG series pages containing series of RPGs. No API exists to find further information.
     *
     * Domains: [Domain.RPG_GEEK]
     */
    RPG_SERIES("rpgseries_page"),

    /**
     * RPG setting pages containing different settings RPGs take place. No API exists to find
     * further information.
     *
     * Domains: [Domain.RPG_GEEK]
     */
    RPG_SETTING("rpgsetting_page"),

    /**
     * RPG system pages containing different systems e.g. Story telling, D20 system etc. No API
     * exists to find further information.
     *
     * Domains: [Domain.RPG_GEEK]
     */
    RPG_SYSTEM("rpgsystem_page"),

    /**
     * Thread pages containing all forum threads created by users. IDs can be extracted for usage
     * with the [org.audux.bgg.BggClient.thread] API
     *
     * Domains: [Domain.BOARD_GAME_GEEK], [Domain.RPG_GEEK], [Domain.VIDEO_GAME_GEEK]
     */
    THREADS("threads_page"),

    /**
     * Video game pages containing video games. IDs can be extracted for usage with the
     * [org.audux.bgg.BggClient.things] API.
     *
     * Domains: [Domain.VIDEO_GAME_GEEK]
     */
    VIDEO_GAMES("videogame_page"),

    /**
     * Video game pages containing all video games related to board games e.g. Starcraft which is
     * both a video game and board game. IDs can be extracted for usage with the
     * [org.audux.bgg.BggClient.things] API
     *
     * Domains: [Domain.BOARD_GAME_GEEK], [Domain.VIDEO_GAME_GEEK]
     */
    VIDEO_GAME_BOARD_GAMES("videogamebg_page"),

    /**
     * Video game character pages containing different characters from/in video games. No API exists
     * to find further information.
     *
     * Domains: [Domain.VIDEO_GAME_GEEK]
     */
    VIDEO_GAME_CHARACTERS("videogamecharacter_page"),

    /**
     * Video game character version pages containing different characters as they might appear in
     * different versions of video games. No API exists to find further information.
     *
     * Domains: [Domain.VIDEO_GAME_GEEK]
     */
    VIDEO_GAME_CHARACTER_VERSIONS("vgcharacterversion_page"),

    /**
     * Video game compilation pages containing different compilations e.g. books about board games
     * or a single box containing several games etc. IDs can be extracted for usage with the
     * [org.audux.bgg.BggClient.things] API.
     *
     * Domains: [Domain.VIDEO_GAME_GEEK]
     */
    VIDEO_GAME_COMPILATION("videogamecompilation_page"),

    /**
     * Video game developer pages containing different developers/companies. No API exists to find
     * further information.
     *
     * Domains: [Domain.VIDEO_GAME_GEEK]
     */
    VIDEO_GAME_DEVELOPER("videogamedeveloper_page"),

    /**
     * Video game expansion pages containing expansions to video games. IDs can be extracted for
     * usage with the [org.audux.bgg.BggClient.things] API.
     *
     * Domains: [Domain.VIDEO_GAME_GEEK]
     */
    VIDEO_GAME_EXPANSION("videogameexpansion_page"),

    /**
     * Video game franchises pages containing different franchises/game series. No API exists to
     * find further information.
     *
     * Domains: [Domain.VIDEO_GAME_GEEK]
     */
    VIDEO_GAME_FRANCHISE("videogamefranchise_page"),

    /**
     * Video game genre pages containing different genre a game might belong to. No API exists to
     * find further information.
     *
     * Domains: [Domain.VIDEO_GAME_GEEK]
     */
    VIDEO_GAME_GENRES("videogamegenre_page"),

    /**
     * Video game hardware pages containing video game hardware e.g. The 400, The Wii etc. IDs can
     * be extracted for usage with the [org.audux.bgg.BggClient.things] API.
     *
     * Domains: [Domain.VIDEO_GAME_GEEK]
     */
    VIDEO_GAME_HARDWARE("videogamehardware_page"),

    /**
     * Video game hardware version pages containing different versions of hardware e.g. Matte Black
     * Xbox etc. No API exists to find further information.
     *
     * Domains: [Domain.VIDEO_GAME_GEEK]
     */
    VIDEO_GAME_HARDWARE_VERSION("videogamehwversion_page"),

    /**
     * Video game platform pages containing different platforms games run e.g. Windows, Sega
     * Dreamcast etc.. No API exists to find further information.
     *
     * Domains: [Domain.VIDEO_GAME_GEEK]
     */
    VIDEO_GAME_PLATFORM("videogameplatform_page"),

    /**
     * Video game publisher pages containing different publishers/companies. No API exists to find
     * further information.
     *
     * Domains: [Domain.VIDEO_GAME_GEEK]
     */
    VIDEO_GAME_PUBLISHER("videogamepublisher_page"),

    /**
     * Video game franchises pages containing different game series. No API exists to find further
     * information.
     *
     * Domains: [Domain.VIDEO_GAME_GEEK]
     */
    VIDEO_GAME_SERIES("videogameseries_page"),

    /**
     * Video game theme pages containing different themes a game might have e.g. Anime, World War 2
     * etc. No API exists to find further information.
     *
     * Domains: [Domain.VIDEO_GAME_GEEK]
     */
    VIDEO_GAME_THEMES("videogametheme_page"),

    /**
     * Video game version pages containing different versions of video games. No API exists to find
     * further information however these versions can also be found in the
     * [org.audux.bgg.BggClient.things] when using `version=true`.
     *
     * Domains: [Domain.VIDEO_GAME_GEEK]
     */
    VIDEO_GAME_VERSION("videogameversion_page"),

    /**
     * Wiki pages containing all available wiki pages on [Domain]. No API exists to find further
     * information.
     *
     * Domains: [Domain.BOARD_GAME_GEEK], [Domain.RPG_GEEK], [Domain.VIDEO_GAME_GEEK]
     */
    WIKI_PAGES("wiki_page");

    companion object {
        /**
         * Given an URL does a partial match as specified by [urlPart] to identify the type of
         * Sitemap.
         */
        fun fromURL(url: String) = entries.find { url.indexOf(it.urlPart) > 0 } ?: UNKNOWN
    }
}

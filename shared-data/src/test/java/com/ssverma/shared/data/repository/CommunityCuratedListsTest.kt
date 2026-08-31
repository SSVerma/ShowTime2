package com.ssverma.shared.data.repository

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.CommunityCuratedList
import com.ssverma.shared.domain.model.community.CommunityCuratedListItem
import com.ssverma.shared.domain.model.community.CommunityListCategories
import com.ssverma.shared.domain.model.community.PublishCustomListParams
import com.ssverma.shared.domain.model.community.ToggleListUpvoteParams
import com.ssverma.shared.domain.model.community.UnpublishCustomListParams
import com.ssverma.shared.domain.model.library.CustomList
import com.ssverma.shared.domain.model.library.CustomListItem
import com.ssverma.shared.domain.utils.ShareMediaUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CommunityCuratedListsTest {

    @Test
    fun communityCuratedList_creationAndDefaults_areCorrect() {
        val list = CommunityCuratedList(
            listId = "list-1",
            title = "Mind-Bending 90s Sci-Fi",
            description = "The most intoxicating cyberpunk and mind-trip cinema.",
            authorId = "user-1",
            authorName = "Neo",
            categoryTag = CommunityListCategories.SCI_FI,
            itemCount = 3,
            items = listOf(
                CommunityCuratedListItem(
                    mediaId = 603,
                    mediaType = MediaType.Movie,
                    title = "The Matrix",
                    posterImageUrl = "/matrix.jpg",
                    voteAvg = 8.2f
                ),
                CommunityCuratedListItem(
                    mediaId = 68,
                    mediaType = MediaType.Movie,
                    title = "12 Monkeys",
                    posterImageUrl = "/monkeys.jpg",
                    voteAvg = 7.7f
                )
            ),
            upvotesCount = 42L,
            clonesCount = 15L,
            isUpvotedByMe = true,
            isClonedByMe = false
        )

        assertEquals("list-1", list.listId)
        assertEquals("Mind-Bending 90s Sci-Fi", list.title)
        assertEquals(CommunityListCategories.SCI_FI, list.categoryTag)
        assertEquals(2, list.items.size)
        assertEquals(42L, list.upvotesCount)
        assertEquals(15L, list.clonesCount)
        assertTrue(list.isUpvotedByMe)
        assertFalse(list.isClonedByMe)
    }

    @Test
    fun communityCuratedList_categoryFiltering_worksCorrectly() {
        val lists = listOf(
            CommunityCuratedList(
                listId = "1",
                title = "Sci-Fi Greats",
                authorId = "u1",
                authorName = "A",
                categoryTag = CommunityListCategories.SCI_FI
            ),
            CommunityCuratedList(
                listId = "2",
                title = "A24 Masterpieces",
                authorId = "u2",
                authorName = "B",
                categoryTag = CommunityListCategories.A24
            ),
            CommunityCuratedList(
                listId = "3",
                title = "Comfort Rain Watch",
                authorId = "u3",
                authorName = "C",
                categoryTag = CommunityListCategories.COMFORT
            )
        )

        val scifiLists = lists.filter { it.categoryTag == CommunityListCategories.SCI_FI }
        val a24Lists = lists.filter { it.categoryTag == CommunityListCategories.A24 }

        assertEquals(1, scifiLists.size)
        assertEquals("Sci-Fi Greats", scifiLists.first().title)
        assertEquals(1, a24Lists.size)
        assertEquals("A24 Masterpieces", a24Lists.first().title)
    }

    @Test
    fun communityCuratedList_rankingSort_ordersByPopularityScore() {
        val listA = CommunityCuratedList(
            listId = "a",
            title = "A",
            authorId = "u1",
            authorName = "A",
            upvotesCount = 10L,
            clonesCount = 2L
        ) // Score: 10*2 + 2 = 22
        val listB = CommunityCuratedList(
            listId = "b",
            title = "B",
            authorId = "u2",
            authorName = "B",
            upvotesCount = 50L,
            clonesCount = 20L
        ) // Score: 50*2 + 20 = 120
        val listC = CommunityCuratedList(
            listId = "c",
            title = "C",
            authorId = "u3",
            authorName = "C",
            upvotesCount = 25L,
            clonesCount = 5L
        ) // Score: 25*2 + 5 = 55

        val sorted =
            listOf(listA, listB, listC).sortedByDescending { it.upvotesCount * 2 + it.clonesCount }

        assertEquals("b", sorted[0].listId)
        assertEquals("c", sorted[1].listId)
        assertEquals("a", sorted[2].listId)
    }

    @Test
    fun shareMediaUtils_buildShareableListText_formatsProperly() {
        val text = ShareMediaUtils.buildShareableListText(
            listTitle = "Best 90s Thrillers",
            listDescription = "Edge of your seat masterworks",
            authorName = "Cinephile Dave",
            itemTitles = listOf(
                "Se7en",
                "The Silence of the Lambs",
                "Fight Club",
                "The Usual Suspects"
            ),
            appPackageName = "com.ssverma.showtime"
        )

        assertNotNull(text)
        assertTrue(text.contains("Best 90s Thrillers"))
        assertTrue(text.contains("Cinephile Dave"))
        assertTrue(text.contains("Se7en"))
        assertTrue(text.contains("The Silence of the Lambs"))
        assertTrue(text.contains("https://play.google.com/store/apps/details?id=com.ssverma.showtime"))

        val textWithListId = ShareMediaUtils.buildShareableListText(
            listTitle = "Best 90s Thrillers",
            listDescription = "Edge of your seat masterworks",
            authorName = "Cinephile Dave",
            itemTitles = listOf("Se7en"),
            appPackageName = "com.ssverma.showtime",
            listId = "list-123"
        )
        assertTrue(textWithListId.contains("https://showtime.ssverma.in/lists/list-123"))
    }

    @Test
    fun publishParams_holdsPayloadCorrectly() {
        val localList = CustomList(
            listId = "local-1",
            title = "My Best Movies",
            description = "Handpicked",
            isPublic = false,
            items = listOf(
                CustomListItem(
                    listId = "local-1",
                    mediaId = 100,
                    mediaType = MediaType.Movie,
                    title = "Inception",
                    posterImageUrl = "/poster.jpg"
                )
            )
        )
        val params = PublishCustomListParams(
            localList = localList,
            categoryTag = CommunityListCategories.MIND_BENDING
        )

        assertEquals("local-1", params.localList.listId)
        assertEquals(CommunityListCategories.MIND_BENDING, params.categoryTag)
        assertEquals(1, params.localList.items.size)
    }
}

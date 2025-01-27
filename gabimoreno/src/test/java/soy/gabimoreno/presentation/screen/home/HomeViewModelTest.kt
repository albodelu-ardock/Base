package soy.gabimoreno.presentation.screen.home

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.amshove.kluent.shouldBe
import org.junit.After
import org.junit.Before
import org.junit.Test
import soy.gabimoreno.core.testing.coVerifyOnce
import soy.gabimoreno.core.testing.relaxedMockk
import soy.gabimoreno.core.testing.verifyOnce
import soy.gabimoreno.data.tracker.Tracker
import soy.gabimoreno.data.tracker.domain.TRACKER_KEY_EPISODE_ID
import soy.gabimoreno.data.tracker.domain.TRACKER_KEY_EPISODE_TITLE
import soy.gabimoreno.data.tracker.main.HomeTrackerEvent
import soy.gabimoreno.domain.repository.PodcastRepository
import soy.gabimoreno.domain.usecase.EncodeUrlUseCase
import soy.gabimoreno.domain.usecase.GetAppVersionNameUseCase

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    private val podcastRepository: PodcastRepository = relaxedMockk()
    private val tracker: Tracker = relaxedMockk()
    private val getAppVersionNameUseCase: GetAppVersionNameUseCase = relaxedMockk()
    private val encodeUrlUseCase: EncodeUrlUseCase = relaxedMockk()
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = HomeViewModel(
            podcastRepository,
            tracker,
            getAppVersionNameUseCase,
            encodeUrlUseCase,
            testDispatcher
        )
    }

    @After
    fun tearDownDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN init THEN appVersionName is loaded`() {
        verifyOnce { getAppVersionNameUseCase() }
        viewModel.appVersionName.isNotBlank()
    }

    @Test
    fun `WHEN init THEN getEpisodes`() = runTest(testDispatcher) {
        viewModel.viewState shouldBe HomeViewModel.ViewState.Loading
        coVerifyOnce { podcastRepository.getEpisodes() }
    }

    @Test
    fun `WHEN onViewScreen THEN track event`() {
        viewModel.onViewScreen()

        verifyOnce { tracker.trackEvent(HomeTrackerEvent.ViewScreen) }
    }

    @Test
    fun `WHEN onEpisodeClicked THEN track event`() {
        val episodeId = "episodeId"
        val episodeTitle = "episodeTitle"
        viewModel.onEpisodeClicked(episodeId, episodeTitle)

        verifyOnce {
            tracker.trackEvent(
                HomeTrackerEvent.ClickEpisode(
                    mapOf(
                        TRACKER_KEY_EPISODE_ID to episodeId,
                        TRACKER_KEY_EPISODE_TITLE to episodeTitle
                    )
                )
            )
        }
    }

    @Test
    fun `WHEN onDeepLinkReceived THEN track event`() {
        val episodeId = "episodeId"
        val episodeTitle = "episodeTitle"
        viewModel.onDeepLinkReceived(episodeId, episodeTitle)

        verifyOnce {
            tracker.trackEvent(
                HomeTrackerEvent.ReceiveDeepLink(
                    mapOf(
                        TRACKER_KEY_EPISODE_ID to episodeId,
                        TRACKER_KEY_EPISODE_TITLE to episodeTitle
                    )
                )
            )
        }
    }
}

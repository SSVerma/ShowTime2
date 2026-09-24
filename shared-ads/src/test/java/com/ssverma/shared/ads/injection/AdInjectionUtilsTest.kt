package com.ssverma.shared.ads.injection

import com.google.android.gms.ads.nativead.NativeAd
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.ui.UiState
import com.ssverma.shared.ads.ui.NativeAdStyle
import io.mockk.mockk
import org.junit.Test

class AdInjectionUtilsTest {

    private val mockNativeAd: NativeAd = mockk(relaxed = true)

    @Test
    fun `injectAds inserts ads at expected fixed positions`() {
        val items = listOf("Movie 1", "Movie 2", "Movie 3")
        val config = AdInjectionConfig(
            placement = AdPlacement.Fixed(listOf(1)),
            style = NativeAdStyle.Grid,
            sectionTag = "test_tag"
        )

        val injected = items.injectAds(config, isAdsEnabled = true)

        assertThat(injected).hasSize(4)
        assertThat((injected[0] as InjectableContent).item).isEqualTo("Movie 1")
        assertThat((injected[1] as InjectableAd).id).isEqualTo("test_tag_ad_Grid_1")
        assertThat((injected[2] as InjectableContent).item).isEqualTo("Movie 2")
        assertThat((injected[3] as InjectableContent).item).isEqualTo("Movie 3")
    }

    @Test
    fun `updateNativeAd updates matching ad in List and preserves list identity if not found`() {
        val ad1 = InjectableAd(id = "section_ad_Grid_1", style = NativeAdStyle.Grid)
        val content = InjectableContent("Movie 1")
        val list = listOf(content, ad1)

        val updated = list.updateNativeAd(ad1, mockNativeAd)

        assertThat(updated).hasSize(2)
        val updatedAd = updated[1] as InjectableAd
        assertThat(updatedAd.ad).isSameInstanceAs(mockNativeAd)

        // Non-matching ad -> must return exact same reference
        val nonMatchingAd = InjectableAd(id = "other_ad", style = NativeAdStyle.Grid)
        val unchanged = list.updateNativeAd(nonMatchingAd, mockNativeAd)
        assertThat(unchanged).isSameInstanceAs(list)
    }

    @Test
    fun `removeNativeAd removes matching ad from List and preserves list identity if not found`() {
        val ad1 = InjectableAd(id = "section_ad_Grid_1", style = NativeAdStyle.Grid)
        val content = InjectableContent("Movie 1")
        val list = listOf(content, ad1)

        val updated = list.removeNativeAd(ad1)

        assertThat(updated).hasSize(1)
        assertThat((updated[0] as InjectableContent).item).isEqualTo("Movie 1")

        // Non-matching ad -> must return exact same reference
        val nonMatchingAd = InjectableAd(id = "other_ad", style = NativeAdStyle.Grid)
        val unchanged = list.removeNativeAd(nonMatchingAd)
        assertThat(unchanged).isSameInstanceAs(list)
    }

    @Test
    fun `updateNativeAd on UiState updates Success data correctly and ignores Loading or Error`() {
        val ad1 = InjectableAd(id = "section_ad_Grid_1", style = NativeAdStyle.Grid)
        val content = InjectableContent("Movie 1")
        val successState: UiState<List<AdInjectable<String>>, Nothing> =
            UiState.Success(listOf(content, ad1))

        val updatedState = successState.updateNativeAd(ad1, mockNativeAd)

        assertThat(updatedState).isInstanceOf(UiState.Success::class.java)
        val data = (updatedState as UiState.Success).data
        assertThat((data[1] as InjectableAd).ad).isSameInstanceAs(mockNativeAd)

        // Loading state
        val loadingState: UiState<List<AdInjectable<String>>, Nothing> = UiState.Loading
        val updatedLoading = loadingState.updateNativeAd(ad1, mockNativeAd)
        assertThat(updatedLoading).isEqualTo(UiState.Loading)

        // Error state
        val errorFailure = com.ssverma.shared.domain.failure.Failure.CoreFailure.NetworkFailure
        val errorState: UiState<List<AdInjectable<String>>, com.ssverma.shared.domain.failure.Failure.CoreFailure> =
            UiState.Error(errorFailure)
        val updatedError = errorState.updateNativeAd(ad1, mockNativeAd)
        assertThat(updatedError).isEqualTo(UiState.Error(errorFailure))
    }

    @Test
    fun `removeNativeAd on UiState removes ad on Success correctly`() {
        val ad1 = InjectableAd(id = "section_ad_Grid_1", style = NativeAdStyle.Grid)
        val content = InjectableContent("Movie 1")
        val successState: UiState<List<AdInjectable<String>>, String> =
            UiState.Success(listOf(content, ad1))

        val updatedState = successState.removeNativeAd(ad1)

        assertThat(updatedState).isInstanceOf(UiState.Success::class.java)
        val data = (updatedState as UiState.Success).data
        assertThat(data).hasSize(1)
        assertThat((data[0] as InjectableContent).item).isEqualTo("Movie 1")
    }
}

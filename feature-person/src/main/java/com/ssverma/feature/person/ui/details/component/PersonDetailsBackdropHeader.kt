package com.ssverma.feature.person.ui.details.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.ssverma.core.image.NetworkImage
import com.ssverma.shared.ui.TmdbBackdropAspectRatio
import com.ssverma.shared.ui.component.BackdropNavigationAction

@Composable
fun PersonDetailsBackdropHeader(
    backdropImageUrl: String,
    profileImageUrl: String,
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    ConstraintLayout(modifier) {
        val (refBackdrop, refProfile, refRoundedSurface) = createRefs()

        /*Backdrop*/
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(TmdbBackdropAspectRatio)
                .constrainAs(refBackdrop) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {

            /*Backdrop image*/
            NetworkImage(
                url = backdropImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            /*Navigation*/
            BackdropNavigationAction(onIconClick = onBackPress)
        }

        /*Rounded surface*/
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SurfaceCornerRoundSize)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium.copy(
                        topStart = CornerSize(SurfaceCornerRoundSize),
                        topEnd = CornerSize(SurfaceCornerRoundSize),
                        bottomStart = CornerSize(0.dp),
                        bottomEnd = CornerSize(0.dp)
                    ),
                )
                .constrainAs(refRoundedSurface) {
                    bottom.linkTo(refBackdrop.bottom)
                    start.linkTo(refBackdrop.start)
                    end.linkTo(refBackdrop.end)
                }
        )

        /*Profile*/
        Surface(
            modifier = Modifier
                .size(110.dp)
                .constrainAs(refProfile) {
                    top.linkTo(refRoundedSurface.top)
                    bottom.linkTo(refRoundedSurface.bottom)
                    start.linkTo(refRoundedSurface.start)
                    end.linkTo(refRoundedSurface.end)
                },
            shape = CircleShape,
            border = BorderStroke(
                width = 4.dp,
                color = MaterialTheme.colorScheme.background
            ),
            tonalElevation = 4.dp
        ) {
            NetworkImage(
                url = profileImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private val SurfaceCornerRoundSize = 24.dp

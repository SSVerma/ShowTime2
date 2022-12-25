package com.ssverma.shared.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.icon.AppIcons
import com.ssverma.shared.ui.TmdbBackdropAspectRatio

@Composable
fun BackdropHeader(
    modifier: Modifier = Modifier,
    backdropImageUrl: String,
    onCloseIconClick: () -> Unit,
    onTrailerFabClick: () -> Unit,
    secondaryActions: @Composable RowScope.() -> Unit = {}
) {
    ConstraintLayout(modifier) {
        val (refBackdrop, refRoundedSurface, refTrailerFab, refSecondaryActions) = createRefs()

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

            /*Backdrop*/
            NetworkImage(
                url = backdropImageUrl,
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
                modifier = Modifier.fillMaxSize()
            )

            /*Navigation action*/
            BackdropNavigationAction(onIconClick = onCloseIconClick)
        }

        /*Rounded surface*/
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SurfaceCornerRoundSize)
                .background(
                    color = MaterialTheme.colors.background,
                    shape = MaterialTheme.shapes.medium.copy(
                        topStart = CornerSize(SurfaceCornerRoundSize),
                        topEnd = CornerSize(SurfaceCornerRoundSize),
                        bottomStart = CornerSize(0.dp),
                        bottomEnd = CornerSize(0.dp)
                    ),
                )
                .constrainAs(refRoundedSurface) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        /*Trailer Action*/
        FloatingActionButton(
            onClick = onTrailerFabClick,
            modifier = Modifier
                .padding(start = 16.dp)
                .constrainAs(refTrailerFab) {
                    top.linkTo(refRoundedSurface.top)
                    bottom.linkTo(refRoundedSurface.top)
                    start.linkTo(refRoundedSurface.start)
                }
        ) {
            Icon(imageVector = AppIcons.PlayArrow, contentDescription = null)
        }

        /*Secondary actions*/
        Actions(
            secondaryActions = secondaryActions,
            modifier = Modifier
                .padding(end = 16.dp)
                .constrainAs(refSecondaryActions) {
                    top.linkTo(refRoundedSurface.top)
                    bottom.linkTo(refRoundedSurface.top)
                    end.linkTo(refRoundedSurface.end)
                }
        )
    }
}

@Composable
private fun Actions(
    modifier: Modifier = Modifier,
    secondaryActions: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) { secondaryActions() }
}

private val SurfaceCornerRoundSize = 12.dp
val ActionSize = 40.dp
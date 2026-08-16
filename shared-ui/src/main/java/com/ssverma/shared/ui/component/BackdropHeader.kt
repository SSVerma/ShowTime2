package com.ssverma.shared.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.icon.AppIcons
import com.ssverma.shared.ui.R
import com.ssverma.shared.ui.TmdbBackdropAspectRatio

@Composable
fun BackdropHeader(
    modifier: Modifier = Modifier,
    backdropImageUrl: String,
    onCloseIconClick: () -> Unit,
    onTrailerFabClick: () -> Unit,
    showTrailerFab: Boolean = true,
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
                    color = MaterialTheme.colorScheme.background,
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
        if (showTrailerFab) {
            FloatingActionButton(
                onClick = onTrailerFabClick,
                shape = CircleShape,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .height(ActionSize)
                    .constrainAs(refTrailerFab) {
                        top.linkTo(refRoundedSurface.top)
                        bottom.linkTo(refRoundedSurface.top)
                        start.linkTo(refRoundedSurface.start)
                        end.linkTo(refSecondaryActions.start, margin = 8.dp)
                        width = Dimension.preferredWrapContent
                        horizontalBias = 0f
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = AppIcons.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.trailer),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
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

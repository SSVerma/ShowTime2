import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.BuildersKt;
import kotlinx.serialization.json.Json;
import kotlinx.serialization.json.JsonArray;
import kotlinx.serialization.json.JsonElement;
import kotlinx.serialization.json.JsonElementKt;
import kotlinx.serialization.json.JsonObject;

import org.gleee.studio.domain.model.BadgeShapeStyle;
import org.gleee.studio.domain.model.BrandThemePreset;
import org.gleee.studio.domain.model.CanvasTarget;
import org.gleee.studio.domain.model.ChassisBorderStyle;
import org.gleee.studio.domain.model.ChassisPlacement;
import org.gleee.studio.domain.model.ChassisShadowStyle;
import org.gleee.studio.domain.model.ChassisTiltAngle;
import org.gleee.studio.domain.model.ChassisType;
import org.gleee.studio.domain.model.ContentScaleFit;
import org.gleee.studio.domain.model.HeadlineHighlightParser;
import org.gleee.studio.domain.model.HeadlineSegment;
import org.gleee.studio.domain.model.SlideBackgroundStyle;
import org.gleee.studio.domain.model.StudioSlideState;
import org.gleee.studio.domain.model.StudioSlideTemplate;
import org.gleee.studio.domain.model.TypographyAlignment;
import org.gleee.studio.domain.model.TypographyFontFamily;
import org.gleee.studio.domain.model.TypographyPosition;
import org.gleee.studio.export.SkiaSlideExporter;
import org.jetbrains.skia.Canvas;
import org.jetbrains.skia.Data;
import org.jetbrains.skia.EncodedImageFormat;
import org.jetbrains.skia.FilterTileMode;
import org.jetbrains.skia.Font;
import org.jetbrains.skia.FontMgr;
import org.jetbrains.skia.FontStyle;
import org.jetbrains.skia.Image;
import org.jetbrains.skia.ImageFilter;
import org.jetbrains.skia.Paint;
import org.jetbrains.skia.PaintMode;
import org.jetbrains.skia.Point;
import org.jetbrains.skia.RRect;
import org.jetbrains.skia.Rect;
import org.jetbrains.skia.Shader;
import org.jetbrains.skia.Surface;
import org.jetbrains.skia.Typeface;

public final class GleeeBatchRenderer {

    private static final int LILAC_START_ARGB = 0xFFC8B6FF;
    private static final int LILAC_END_ARGB = 0xFFBCA6F6;
    private static final int INK_DARK_ARGB = 0xFF0B0B0E;
    private static final int SUBTITLE_DARK_ARGB = 0xD214141B;
    private static final int WHITE_ARGB = 0xFFFFFFFF;
    private static final int CHASSIS_BODY_ARGB = 0xFF161B22;
    private static final int CHASSIS_STROKE_ARGB = 0xFF334155;
    private static final int DROP_SHADOW_ARGB = 0x6E090B12;

    private static final PrintStream OUT =
            new PrintStream(new FileOutputStream(FileDescriptor.out));

    private GleeeBatchRenderer() {
    }

    public static void main(String[] args) throws Exception {
        Map<String, String> options = parseArgs(args);
        String manifestPath = requireArg(options, "manifest");
        String projectRoot = requireArg(options, "project-root");
        String gleeStudioDir = requireArg(options, "gleeestudio-dir");
        String target = options.getOrDefault("target", "all").toLowerCase(Locale.ROOT);
        String overrideInputDir = options.get("override-input-dir");
        String overrideOutputDir = options.get("override-output-dir");

        String manifestJson = Files.readString(Path.of(manifestPath));
        JsonObject root = JsonElementKt.getJsonObject(Json.Default.parseToJsonElement(manifestJson));
        JsonObject decks = JsonElementKt.getJsonObject(root.get("decks"));

        String themeName = stringField(root, "theme", "EDITORIAL_LILAC");
        BrandThemePreset brandTheme = BrandThemePreset.valueOf(themeName);
        String fontName = stringField(root, "font", "GOOGLE_SANS");
        TypographyFontFamily fontFamily = TypographyFontFamily.valueOf(fontName);

        Typeface boldTypeface = loadTypeface(gleeStudioDir, "google_sans_bold.ttf");
        Typeface regularTypeface = loadTypeface(gleeStudioDir, "google_sans_regular.ttf");

        List<String> deckKeys = resolveTargets(target, decks);
        int totalRendered = 0;
        int totalSkipped = 0;

        for (String deckKey : deckKeys) {
            JsonElement deckElement = decks.get(deckKey);
            if (deckElement == null) {
                continue;
            }
            JsonObject deck = JsonElementKt.getJsonObject(deckElement);
            int widthPx = intField(deck, "widthPx", 1920);
            int heightPx = intField(deck, "heightPx", 1080);
            String deviceKind = stringField(deck, "deviceKind", "DESKTOP");
            String inputDirRel = stringField(deck, "inputDir", "");
            String outputDirRel = stringField(deck, "outputDir", "");

            Path inputDir = resolvePath(
                    projectRoot,
                    overrideInputDir != null ? overrideInputDir : inputDirRel
            );
            Path outputDir = resolvePath(
                    projectRoot,
                    overrideOutputDir != null ? overrideOutputDir : outputDirRel
            );
            Files.createDirectories(outputDir);

            OUT.format("%n--- Deck: %s (%s) ---%n", deckKey, deviceKind);
            OUT.format("Input:  %s%n", inputDir);
            OUT.format("Output: %s%n", outputDir);

            JsonArray slides = JsonElementKt.getJsonArray(deck.get("slides"));
            for (int slideIndex = 0; slideIndex < slides.size(); slideIndex++) {
                JsonObject slideObj = JsonElementKt.getJsonObject(slides.get(slideIndex));
                String inputFile = stringField(slideObj, "inputFile", "");
                String outputFile = stringField(slideObj, "outputFile", inputFile);
                Path inputPath = inputDir.resolve(inputFile);
                if (!Files.isRegularFile(inputPath)) {
                    OUT.format("  [SKIP] Missing raw screenshot: %s%n", inputPath);
                    totalSkipped++;
                    continue;
                }

                byte[] rawBytes = Files.readAllBytes(inputPath);
                byte[] renderedBytes;
                if ("PHONE".equalsIgnoreCase(deviceKind)) {
                    renderedBytes = renderPhoneSlide(
                            deckKey + "_" + slideIndex,
                            slideObj,
                            rawBytes,
                            inputFile,
                            brandTheme,
                            fontFamily
                    );
                } else {
                    renderedBytes = renderAdaptiveFrameSlide(
                            widthPx,
                            heightPx,
                            deviceKind,
                            slideObj,
                            rawBytes,
                            boldTypeface,
                            regularTypeface
                    );
                }

                Path outputPath = outputDir.resolve(outputFile);
                Files.write(outputPath, renderedBytes);
                Image checkImage = Image.Companion.makeFromEncoded(renderedBytes);
                OUT.format(
                        "  [OK] %s (%dx%d px, %d KB)%n",
                        outputPath.getFileName(),
                        checkImage.getWidth(),
                        checkImage.getHeight(),
                        renderedBytes.length / 1024
                );
                totalRendered++;
            }
        }

        OUT.format("%nSummary: %d rendered, %d skipped.%n", totalRendered, totalSkipped);
    }

    private static byte[] renderPhoneSlide(
            String slideId,
            JsonObject slideObj,
            byte[] rawImageBytes,
            String inputFileName,
            BrandThemePreset brandTheme,
            TypographyFontFamily fontFamily
    ) throws Exception {
        String badge = stringField(slideObj, "badge", "SHOWTIME");
        String headline = stringField(slideObj, "headline", "");
        String subtitle = stringField(slideObj, "subtitle", "");
        String placementName = stringField(slideObj, "placement", "BLEED_BOTTOM");
        String tiltName = stringField(slideObj, "tilt", "NONE");

        ChassisPlacement placement = ChassisPlacement.valueOf(placementName);
        ChassisTiltAngle tilt = ChassisTiltAngle.valueOf(tiltName);

        StudioSlideState slideState = new StudioSlideState(
                slideId,
                StudioSlideTemplate.HERO_SHOWCASE,
                CanvasTarget.PLAY_PHONE_20_9,
                badge,
                headline,
                subtitle,
                brandTheme,
                SlideBackgroundStyle.SUBTLE_GRADIENT,
                ChassisType.PHONE_PUNCH_HOLE,
                placement,
                tilt,
                null,
                0f,
                0f,
                1.0f,
                ChassisShadowStyle.ELEVATED,
                ChassisBorderStyle.STANDARD_FRAME,
                ContentScaleFit.FILL_WIDTH_TOP,
                rawImageBytes,
                inputFileName,
                0f,
                0f,
                1.0f,
                false,
                true,
                true,
                BadgeShapeStyle.CAPSULE_PILL,
                false,
                null,
                null,
                TypographyPosition.TOP,
                fontFamily,
                TypographyAlignment.CENTER
        );

        Object result = BuildersKt.runBlocking(
                EmptyCoroutineContext.INSTANCE,
                (scope, continuation) -> SkiaSlideExporter.INSTANCE.exportSlideToPngBytes(
                        slideState,
                        continuation
                )
        );
        return (byte[]) result;
    }

    private static byte[] renderAdaptiveFrameSlide(
            int configuredWidthPx,
            int configuredHeightPx,
            String deviceKind,
            JsonObject slideObj,
            byte[] rawImageBytes,
            Typeface boldTypeface,
            Typeface regularTypeface
    ) {
        Image rawImage = Image.Companion.makeFromEncoded(rawImageBytes);
        float imageAspect = (float) rawImage.getWidth() / (float) rawImage.getHeight();

        int widthPx = configuredWidthPx;
        int heightPx = configuredHeightPx;
        if ("TABLET".equalsIgnoreCase(deviceKind) && rawImage.getHeight() > rawImage.getWidth()) {
            widthPx = Math.min(configuredWidthPx, configuredHeightPx);
            heightPx = Math.max(configuredWidthPx, configuredHeightPx);
        }

        Surface surface = Surface.Companion.makeRasterN32Premul(widthPx, heightPx);
        Canvas canvas = surface.getCanvas();
        drawLilacGradientBackground(canvas, widthPx, heightPx);

        float scale = heightPx / 1080f;
        boolean isPortrait = heightPx > widthPx;
        float topPadding = heightPx * (isPortrait ? 0.042f : 0.046f);
        float contentMaxWidth = widthPx * 0.86f;

        String badge = stringField(slideObj, "badge", "").trim();
        String headline = stringField(slideObj, "headline", "").trim();
        String subtitle = stringField(slideObj, "subtitle", "").trim();

        float headerBottomY = drawCenteredHeader(
                canvas,
                widthPx,
                topPadding,
                contentMaxWidth,
                scale,
                isPortrait,
                badge,
                headline,
                subtitle,
                boldTypeface,
                regularTypeface
        );

        float gapBelowHeader = heightPx * (isPortrait ? 0.028f : 0.024f);
        float bottomPadding = heightPx * (isPortrait ? 0.042f : 0.040f);
        float sidePadding = widthPx * (isPortrait ? 0.06f : 0.048f);

        float maxFrameWidth = widthPx - (2f * sidePadding);
        float maxFrameHeight = heightPx - bottomPadding - (headerBottomY + gapBelowHeader);

        float bezelThickness = (isPortrait ? 14f : 12f) * scale;
        float maxInnerWidth = maxFrameWidth - (2f * bezelThickness);
        float maxInnerHeight = maxFrameHeight - (2f * bezelThickness);

        float innerWidth;
        float innerHeight;
        if (maxInnerWidth / maxInnerHeight > imageAspect) {
            innerHeight = maxInnerHeight;
            innerWidth = innerHeight * imageAspect;
        } else {
            innerWidth = maxInnerWidth;
            innerHeight = innerWidth / imageAspect;
        }

        float frameWidth = innerWidth + (2f * bezelThickness);
        float frameHeight = innerHeight + (2f * bezelThickness);
        float frameX = (widthPx - frameWidth) / 2f;
        float frameY = (headerBottomY + gapBelowHeader) + ((maxFrameHeight - frameHeight) / 2f);
        float outerCornerRadius = 26f * scale;
        float innerCornerRadius = 16f * scale;

        float shadowBlur = 28f * scale;
        float shadowOffsetY = 20f * scale;
        try (Paint shadowPaint = new Paint()) {
            shadowPaint.setColor(DROP_SHADOW_ARGB);
            shadowPaint.setAntiAlias(true);
            shadowPaint.setImageFilter(
                    ImageFilter.Companion.makeBlur(
                            shadowBlur,
                            shadowBlur,
                            FilterTileMode.DECAL,
                            null,
                            null
                    )
            );
            canvas.drawRRect(
                    RRect.Companion.makeXYWH(
                            frameX + (2f * scale),
                            frameY + shadowOffsetY,
                            frameWidth - (4f * scale),
                            frameHeight,
                            outerCornerRadius
                    ),
                    shadowPaint
            );
        }

        try (Paint bodyPaint = new Paint()) {
            bodyPaint.setColor(CHASSIS_BODY_ARGB);
            bodyPaint.setAntiAlias(true);
            canvas.drawRRect(
                    RRect.Companion.makeXYWH(frameX, frameY, frameWidth, frameHeight, outerCornerRadius),
                    bodyPaint
            );
        }

        try (Paint strokePaint = new Paint()) {
            strokePaint.setColor(CHASSIS_STROKE_ARGB);
            strokePaint.setMode(PaintMode.STROKE);
            strokePaint.setStrokeWidth(2.2f * scale);
            strokePaint.setAntiAlias(true);
            canvas.drawRRect(
                    RRect.Companion.makeXYWH(frameX, frameY, frameWidth, frameHeight, outerCornerRadius),
                    strokePaint
            );
        }

        float screenX = frameX + bezelThickness;
        float screenY = frameY + bezelThickness;
        canvas.save();
        canvas.clipRRect(
                RRect.Companion.makeXYWH(screenX, screenY, innerWidth, innerHeight, innerCornerRadius),
                true
        );
        canvas.drawImageRect(
                rawImage,
                Rect.Companion.makeXYWH(screenX, screenY, innerWidth, innerHeight)
        );
        canvas.restore();

        Image snapshot = surface.makeImageSnapshot();
        Data encoded = snapshot.encodeToData(EncodedImageFormat.PNG, 100);
        return encoded != null ? encoded.getBytes() : new byte[0];
    }

    private static float drawCenteredHeader(
            Canvas canvas,
            int widthPx,
            float topY,
            float maxWidth,
            float scale,
            boolean isPortrait,
            String badge,
            String headline,
            String subtitle,
            Typeface boldTypeface,
            Typeface regularTypeface
    ) {
        float currentY = topY;
        float badgeFontSize = (isPortrait ? 19f : 18f) * scale;
        float headlineFontSize = (isPortrait ? 52f : 50f) * scale;
        float subtitleFontSize = (isPortrait ? 24f : 23f) * scale;

        Font badgeFont = createFont(boldTypeface, badgeFontSize);
        Font headlineFont = createFont(boldTypeface, headlineFontSize);
        Font subtitleFont = createFont(regularTypeface, subtitleFontSize);

        if (!badge.isEmpty()) {
            float badgePadX = 20f * scale;
            float badgePadY = 9f * scale;
            float badgeTextWidth = badgeFont.measureTextWidth(badge);
            float capHeight = resolveCapHeight(badgeFont, 0.72f);
            float pillWidth = badgeTextWidth + (2f * badgePadX);
            float pillHeight = capHeight + (2f * badgePadY);
            float pillX = (widthPx - pillWidth) / 2f;

            try (Paint pillPaint = new Paint()) {
                pillPaint.setColor(WHITE_ARGB);
                pillPaint.setAntiAlias(true);
                canvas.drawRRect(
                        RRect.Companion.makeXYWH(pillX, currentY, pillWidth, pillHeight, pillHeight / 2f),
                        pillPaint
                );
            }
            try (Paint badgeTextPaint = new Paint()) {
                badgeTextPaint.setColor(INK_DARK_ARGB);
                badgeTextPaint.setAntiAlias(true);
                float textBaselineY = currentY + (pillHeight / 2f) + (capHeight / 2f) - (1f * scale);
                canvas.drawString(badge, pillX + badgePadX, textBaselineY, badgeFont, badgeTextPaint);
            }
            currentY += pillHeight + (18f * scale);
        }

        if (!headline.isEmpty()) {
            List<HeadlineSegment> segments = HeadlineHighlightParser.INSTANCE.parseSegments(headline);
            List<WordToken> tokens = tokenizeSegments(segments);
            List<List<WordToken>> lines = wrapTokens(tokens, headlineFont, maxWidth);
            float lineHeight = headlineFontSize * 1.18f;
            float capHeight = resolveCapHeight(headlineFont, 0.75f);
            float spaceWidth = headlineFont.measureTextWidth(" ");

            try (Paint darkPaint = new Paint(); Paint accentPaint = new Paint()) {
                darkPaint.setColor(INK_DARK_ARGB);
                darkPaint.setAntiAlias(true);
                accentPaint.setColor(WHITE_ARGB);
                accentPaint.setAntiAlias(true);

                for (List<WordToken> line : lines) {
                    float totalWidth = 0f;
                    for (int index = 0; index < line.size(); index++) {
                        totalWidth += headlineFont.measureTextWidth(line.get(index).text);
                        if (index < line.size() - 1) {
                            totalWidth += spaceWidth;
                        }
                    }
                    float cursorX = (widthPx - totalWidth) / 2f;
                    float baselineY = currentY + capHeight;
                    for (WordToken token : line) {
                        Paint activePaint = token.highlighted ? accentPaint : darkPaint;
                        canvas.drawString(token.text, cursorX, baselineY, headlineFont, activePaint);
                        cursorX += headlineFont.measureTextWidth(token.text) + spaceWidth;
                    }
                    currentY += lineHeight;
                }
            }
            currentY += 10f * scale;
        }

        if (!subtitle.isEmpty()) {
            List<String> subtitleLines = wrapPlainText(subtitle, subtitleFont, maxWidth * 0.9f);
            float lineHeight = subtitleFontSize * 1.30f;
            float capHeight = resolveCapHeight(subtitleFont, 0.72f);

            try (Paint subPaint = new Paint()) {
                subPaint.setColor(SUBTITLE_DARK_ARGB);
                subPaint.setAntiAlias(true);
                for (String line : subtitleLines) {
                    float lineWidth = subtitleFont.measureTextWidth(line);
                    float lineX = (widthPx - lineWidth) / 2f;
                    canvas.drawString(line, lineX, currentY + capHeight, subtitleFont, subPaint);
                    currentY += lineHeight;
                }
            }
        }

        return currentY;
    }

    private static void drawLilacGradientBackground(Canvas canvas, int widthPx, int heightPx) {
        try (Shader shader = Shader.Companion.makeLinearGradient(
                new Point(widthPx / 2f, 0f),
                new Point(widthPx / 2f, (float) heightPx),
                new int[]{LILAC_START_ARGB, LILAC_END_ARGB}
        ); Paint paint = new Paint()) {
            paint.setShader(shader);
            paint.setAntiAlias(true);
            canvas.drawRect(Rect.Companion.makeWH((float) widthPx, (float) heightPx), paint);
        }
    }

    private static Typeface loadTypeface(String gleeStudioDir, String fontFileName) {
        Path fontPath = Path.of(
                gleeStudioDir,
                "composeApp",
                "src",
                "commonMain",
                "composeResources",
                "font",
                fontFileName
        );
        if (Files.isRegularFile(fontPath)) {
            try {
                byte[] fontBytes = Files.readAllBytes(fontPath);
                Typeface loaded = FontMgr.Companion.getDefault().makeFromData(
                        Data.Companion.makeFromBytes(fontBytes, 0, fontBytes.length),
                        0
                );
                if (loaded != null) {
                    return loaded;
                }
            } catch (IOException ignored) {
                // Fallback to system typeface below
            }
        }
        return FontMgr.Companion.getDefault().matchFamilyStyle(
                "sans-serif",
                FontStyle.Companion.getBOLD()
        );
    }

    private static Font createFont(Typeface typeface, float sizePx) {
        if (typeface != null) {
            return new Font(typeface, sizePx);
        }
        Font fallback = new Font();
        fallback.setSize(sizePx);
        return fallback;
    }

    private static float resolveCapHeight(Font font, float fallbackRatio) {
        float capHeight = font.getMetrics().getCapHeight();
        return capHeight > 0f ? capHeight : font.getSize() * fallbackRatio;
    }

    private static List<WordToken> tokenizeSegments(List<HeadlineSegment> segments) {
        List<WordToken> tokens = new ArrayList<>();
        for (HeadlineSegment segment : segments) {
            String[] rawLines = segment.getText().split("\n", -1);
            for (int lineIndex = 0; lineIndex < rawLines.length; lineIndex++) {
                if (lineIndex > 0) {
                    tokens.add(new WordToken("", false, true));
                }
                String[] words = rawLines[lineIndex].trim().split("\\s+");
                for (String word : words) {
                    if (!word.isEmpty()) {
                        tokens.add(new WordToken(word, segment.isHighlighted(), false));
                    }
                }
            }
        }
        return tokens;
    }

    private static List<List<WordToken>> wrapTokens(
            List<WordToken> tokens,
            Font font,
            float maxWidth
    ) {
        List<List<WordToken>> lines = new ArrayList<>();
        List<WordToken> currentLine = new ArrayList<>();
        float currentWidth = 0f;
        float spaceWidth = font.measureTextWidth(" ");

        for (WordToken token : tokens) {
            if (token.newline) {
                if (!currentLine.isEmpty()) {
                    lines.add(new ArrayList<>(currentLine));
                    currentLine.clear();
                    currentWidth = 0f;
                }
                continue;
            }
            float wordWidth = font.measureTextWidth(token.text);
            float needed = currentLine.isEmpty() ? wordWidth : currentWidth + spaceWidth + wordWidth;
            if (needed <= maxWidth || currentLine.isEmpty()) {
                currentLine.add(token);
                currentWidth = needed;
            } else {
                lines.add(new ArrayList<>(currentLine));
                currentLine.clear();
                currentLine.add(token);
                currentWidth = wordWidth;
            }
        }
        if (!currentLine.isEmpty()) {
            lines.add(currentLine);
        }
        return lines;
    }

    private static List<String> wrapPlainText(String text, Font font, float maxWidth) {
        List<String> wrapped = new ArrayList<>();
        for (String paragraph : text.split("\n")) {
            String trimmed = paragraph.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (font.measureTextWidth(trimmed) <= maxWidth) {
                wrapped.add(trimmed);
                continue;
            }
            StringBuilder currentLine = new StringBuilder();
            for (String word : trimmed.split("\\s+")) {
                String candidate = currentLine.length() == 0 ? word : currentLine + " " + word;
                if (font.measureTextWidth(candidate) <= maxWidth || currentLine.length() == 0) {
                    if (currentLine.length() > 0) {
                        currentLine.append(' ');
                    }
                    currentLine.append(word);
                } else {
                    wrapped.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                }
            }
            if (currentLine.length() > 0) {
                wrapped.add(currentLine.toString());
            }
        }
        return wrapped;
    }

    private static List<String> resolveTargets(String target, JsonObject decks) {
        if ("all".equals(target)) {
            return new ArrayList<>(decks.keySet());
        }
        return Arrays.asList(target);
    }

    private static Path resolvePath(String rootDir, String maybeRelative) {
        File file = new File(maybeRelative);
        return file.isAbsolute() ? file.toPath() : Path.of(rootDir, maybeRelative);
    }

    private static String stringField(JsonObject object, String key, String defaultValue) {
        JsonElement element = object.get(key);
        if (element == null) {
            return defaultValue;
        }
        return JsonElementKt.getJsonPrimitive(element).getContent();
    }

    private static int intField(JsonObject object, String key, int defaultValue) {
        JsonElement element = object.get(key);
        if (element == null) {
            return defaultValue;
        }
        return Integer.parseInt(JsonElementKt.getJsonPrimitive(element).getContent());
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String arg : args) {
            if (arg.startsWith("--") && arg.contains("=")) {
                int eqIndex = arg.indexOf('=');
                map.put(arg.substring(2, eqIndex), arg.substring(eqIndex + 1));
            }
        }
        return map;
    }

    private static String requireArg(Map<String, String> map, String key) {
        String value = map.get(key);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Missing required argument: --" + key);
        }
        return value;
    }

    private static final class WordToken {
        final String text;
        final boolean highlighted;
        final boolean newline;

        WordToken(String text, boolean highlighted, boolean newline) {
            this.text = text;
            this.highlighted = highlighted;
            this.newline = newline;
        }
    }
}

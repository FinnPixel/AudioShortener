# AudioShortener

A small Swing desktop app for trimming an audio file down to a selected range.
Open a file, drag the two handles to pick the segment you want to keep and export it.

## Requirements

- Java 21

Gradle does not need to be installed — use the bundled wrapper.

## Build and run

```
./gradlew run
```

To produce a distributable build:

```
./gradlew build
```

## Supported formats

| Direction | Formats |
| --- | --- |
| Import | `.wav`, `.mp3` |
| Export | `.wav` |

mp3 support comes from the [mp3spi](https://github.com/umjammer/mp3spi) service provider
(with jlayer and tritonus-share), which plugs into `javax.sound.sampled` so the standard
`AudioSystem` calls used in `AudioProcessor` can read mp3 directly.

These libraries only provide a *decoder*, so export is always WAV. Cutting an mp3 works by
decoding it to PCM first, then writing the selected range as a WAV file.

## Known limitations

- Export is WAV only — writing mp3 would need a separate encoder (e.g. LAME via JNI).
- There is no waveform display or playback preview; the range is chosen against a
  linear timeline only.
- Trim points have one-second resolution.

# LiteRT model for code analysis

Place a TFLite model file named **`code_analyzer.tflite`** in this directory to
enable on-device LiteRT inference in `LiteRTCodeAnalyzer`.

## Expected model I/O

| Tensor | Shape                          | dtype   | Description                                       |
|--------|--------------------------------|---------|---------------------------------------------------|
| input  | `[1, 320]` (32 steps × 10 types) | float32 | One-hot encoded block sequence (zero-padded)      |
| output | `[1, 2]`                       | float32 | `[activityScore, redundancyScore]` each in [0, 1] |

When the file is absent `LiteRTCodeAnalyzer` falls back automatically to the
rule-based `BehaviorAnalyzer`.

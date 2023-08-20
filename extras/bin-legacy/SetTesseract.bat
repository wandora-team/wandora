REM This bat script is executed automatically by Wandora.bat.
REM You don't need to execute it manually.

REM Path to your tesseract installation.

set TESSERACT_PATH=%TESSDATA_PREFIX%

REM Language used by Tesseract. A list of available languages
REM to Tesseract may be obtained with 'tesseract --list-langs'.
REM The format for the language is ISO 639-2 detailed in
REM https://en.wikipedia.org/wiki/ISO_639-2

set TESSERACT_LANG=eng
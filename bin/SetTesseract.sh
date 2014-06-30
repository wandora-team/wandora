#!/bin/bash
# This bat script is executed automatically by Wandora.sh.
# You don't need to execute it manually.

# Path to your tesseract installation.
export TESSERACT_PATH=$TESSDATA_PREFIX

# Language used by Tesseract. A list of available languages
# to Tesseract may be obtained with 'tesseract --list-langs'.
# The format for the language is ISO 639-2 detailed in
# https://en.wikipedia.org/wiki/ISO_639-2

export TESSERACT_LANG=eng